package ch.osiv.document;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jhlabs.image.DiffusionFilter;
import com.jhlabs.image.GrayscaleFilter;

import ch.osiv.Constant;
import ch.osiv.document.convert.DocumentColor;
import ch.osiv.document.tiff.Color3i;
import ch.osiv.document.tiff.parameter.CoordinateParameter;
import ch.osiv.helper.ColorMode;
import ch.osiv.helper.DitheringUtils;
import ch.osiv.helper.FileHelper;
import ch.osiv.helper.FileType;

/**
 * TiffFileHandler class
 *
 * @author Arno van der Ende
 */
public class TiffFileHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TiffFileHandler.class);

    /**
     * Add annotation to one page
     *
     * @param annotationDocumentInfo
     * @param bi
     * @param pageNumber
     * @param sedex
     * @return
     * @throws IOException
     */
    private BufferedImage addAnnotationToOnePage(DocumentInfo annotationDocumentInfo,
                                                 BufferedImage bi,
                                                 int pageNumber,
                                                 boolean sedex) throws IOException {

        List<LineAnnotation>        lineAnnotationList        = new ArrayList<>();
        List<RubberStampAnnotation> rubberStampAnnotationList = new ArrayList<>();
        List<PaginationAnnotation>  paginationAnnotationList  = new ArrayList<>();

        Graphics2D g2d    = bi.createGraphics();
        int        width  = bi.getWidth();
        int        height = bi.getHeight();

        try {

            if (annotationDocumentInfo != null)
                for (PageInfo pageInfo : annotationDocumentInfo.getPageInfoList()) {
                    if (pageInfo.getPageNumber() == pageNumber) {
                        lineAnnotationList.addAll(pageInfo.getLineAnnotationList());
                        // TODO: why create 2 new lists, while pageInfo already has the list
                        rubberStampAnnotationList.addAll(pageInfo.getRubberStampAnnotationList());
                        paginationAnnotationList.addAll(pageInfo.getPaginationAnnotationList());
                    }
                }

            for (LineAnnotation lineAnnotation : lineAnnotationList) {
                bi = addLineAnnotationToTiff(bi, g2d, lineAnnotation);
            }

            for (RubberStampAnnotation rubberStampAnnotation : rubberStampAnnotationList) {
                bi = addFreeTextAnnotationToTiff(bi, g2d, rubberStampAnnotation);
            }

            // In the case we are in Sedex, we don't rotate the page and don't display the pagination
            if (!sedex) {
                // if we are in landscape, we rotate the page
                if (height < width) {

                    try {
                        bi = rotateLandscapePage(bi, false); // counter clockwise
                        g2d.dispose();
                        g2d = bi.createGraphics();
                    } catch (Exception e) {
                        LOGGER.error("{} Error while rotating page {}",
                                     Constant.DATETIME_FORMAT.format(new Date()),
                                     pageNumber);
                        throw e;
                    }
                }
                for (PaginationAnnotation paginationAnnotation : paginationAnnotationList) {
                    addFreeTextAnnotationToTiff(bi, g2d, paginationAnnotation); // pagination is added once the page is in portrait
                }
            }

            return bi;

        } finally {

            if (g2d != null) {
                g2d.dispose();
                g2d = null;
            }

        }

    }

    /**
     * Apply color filter to a buffered image
     *
     * @param buffImage   <a href="#{@link}">{@link BufferedImage} buffImage image
     * @param colorOption <a href="#{@link}">{@link String} colorOption
     * @return BufferedImage the bufferedImage object
     * @throws IOException
     */
    public BufferedImage applyColorFilter(BufferedImage buffImage,
                                          String colorOption) {

        switch (colorOption) {
            case ColorMode.greyscale:
                GrayscaleFilter grayscaleFilter = new GrayscaleFilter();
                return grayscaleFilter.filter(buffImage, null);

            case ColorMode.monochrome:
                DiffusionFilter monochromeFilter = new DiffusionFilter();
                monochromeFilter.setColorDither(false);
                monochromeFilter.setSerpentine(true);
                monochromeFilter.setLevels(16);
                return monochromeFilter.filter(buffImage, null);

            default:
                return buffImage;
        }
    }

    /**
     * add a white border to an BufferedImage
     *
     * @param buffImage  <BufferedImage>
     * @param borderSize <Integer> the size of the border in pixels
     * @return <BufferedImage> BufferedImage
     */
    public BufferedImage addBorder(BufferedImage buffImage,
                                   Integer borderSize) {

        Integer       borderOffset  = 0;
        BufferedImage borderedImage = new BufferedImage(buffImage.getWidth() + borderSize * 2,
                                                        buffImage.getHeight() + borderSize * 2,
                                                        BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = (Graphics2D) borderedImage.getGraphics();

        graphics.setStroke(new BasicStroke(borderSize));
        graphics.setColor(Color.WHITE);

        graphics.drawRect(0, 0, borderedImage.getWidth(), borderedImage.getHeight());
        graphics.fillRect(0, 0, borderedImage.getWidth(), borderedImage.getHeight());

        graphics.drawImage(buffImage,
                           borderSize,
                           borderSize,
                           borderedImage.getWidth() - borderSize,
                           borderedImage.getHeight() - borderSize,
                           borderOffset,
                           borderOffset,
                           buffImage.getWidth() - borderOffset,
                           buffImage.getHeight() - borderOffset,
                           null);

        return borderedImage;
    }

    /**
     * convert tiff to RGB/GreyScale/B&W
     *
     * @param tiffFilename
     * @param annotationFilename
     * @param documentColor
     * @return path of the file
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws XPathExpressionException
     */
    public String convertTiffColor(String tiffFilename,
                                   DocumentColor documentColor) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        return addAnnotationToTiff(tiffFilename, null, documentColor);

    }

    /**
     * Rotate a landscape page
     *
     * @param originalImage   The original image page
     * @param rotateClockwise True to rotate clockwise, otherwise rotate counter clockwise
     * @return The rotated (portrait) Image page
     */
    private BufferedImage rotateLandscapePage(BufferedImage originalImage,
                                              boolean rotateClockwise) {

        int width  = originalImage.getWidth();
        int height = originalImage.getHeight();

        // check if the image is in landscape orientation - if not return
        if (width < height) {
            return originalImage;
        }

        BufferedImage rotatedImage = null;

        // create a graphics object from the new image
        // TODO : try using Graphics2D object from the caller
        Graphics2D g2d = null;
        try {
            // create a new BufferedImage with the adjusted dimensions
            int imageType = getImageType(originalImage);
            rotatedImage = new BufferedImage(height, width, imageType);

            g2d = rotatedImage.createGraphics();

            // set the rotation transformation
            AffineTransform transform = new AffineTransform();

            if (rotateClockwise) {
                // rotate 90 degrees clockwise
                transform.rotate(Math.toRadians(90));
                // apply the transformation to the graphics object
                g2d.setTransform(transform);
                // draw the original image onto the new image
                g2d.drawImage(originalImage, 0, -height, null);
            } else {
                // rotate 90 degrees counter clockwise
                transform.rotate(Math.toRadians(-90));
                // apply the transformation to the graphics object
                g2d.setTransform(transform);
                // draw the original image onto the new image
                g2d.drawImage(originalImage, -width, 0, null);
            }
        } finally {
            // free up resources
            if (g2d != null) {
                g2d.dispose();
                g2d = null;
            }
        }

        return rotatedImage;
    }

    /**
     * Returns the image type. When it's 0, try to determine it
     *
     * @param image BufferedImage
     * @return Imagetype
     */
    private int getImageType(BufferedImage image) {

        int imageType = image.getType();

        // cannot handle image-type 0, so try to get the info from the
        // metadata and choose the correct image-type
        if (imageType == BufferedImage.TYPE_CUSTOM) {
            ColorModel colorModel = image.getColorModel();
            ColorSpace colorSpace = colorModel.getColorSpace();
            switch (colorSpace.getType()) {
                case ColorSpace.TYPE_RGB:
                    imageType = BufferedImage.TYPE_INT_RGB;
                    break;
                case ColorSpace.TYPE_GRAY:
                    imageType = BufferedImage.TYPE_BYTE_GRAY;
                    break;
                default:
                    LOGGER.warn("Unable to determine ImageType. Returning RGB type!");
                    imageType = BufferedImage.TYPE_INT_RGB;
            }

            LOGGER.info("{} BufferedImage has ImageType 0 - determined new ImageType: {}",
                        Constant.DATETIME_FORMAT.format(new Date()),
                        imageType);
        }

        return imageType;
    }

    /**
     * @param imagePath
     * @param modifyParameter Parameter object that contains modifying options (ex. greyscale,
     *                        border)
     * @return HashMap of BufferedImages as keys and their corresponding metadata
     *         the null key contains the whole stream metadata
     * @throws IOException
     */
    public HashMap<BufferedImage, IIOMetadata> getPagesListFromTiffFile(String imagePath,
                                                                        EditTiffParameter modifyParameter) throws IOException {

        File        imageFile      = new File(imagePath);
        String      imageExtension = FilenameUtils.getExtension(imagePath);
        ImageReader reader         = ImageIO.getImageReadersByFormatName(imageExtension).next();

        HashMap<BufferedImage, IIOMetadata> pagesMap = new HashMap<BufferedImage, IIOMetadata>();
        try (ImageInputStream inputStream = ImageIO.createImageInputStream(imageFile);) {

            BufferedImage buffImage;
            IIOMetadata   metadata;
            reader.setInput(inputStream);

            int imageCount = reader.getNumImages(true);
            metadata = reader.getStreamMetadata();

            //Put the whole stream metadata at the null key, as it can be used when writing the Tiff Image
            pagesMap.put(null, metadata);

            for (int i = 0; i < imageCount; i++) {
                buffImage = reader.read(i);
                if (modifyParameter.isAddBorder()) {
                    buffImage = this.addBorder(buffImage, modifyParameter.getBorderSize());
                }
                if (modifyParameter.isAddFilter()) {
                    buffImage = this.applyColorFilter(buffImage, modifyParameter.getColorOption());
                }
                pagesMap.put(buffImage, reader.getImageMetadata(i));
            }

        } finally {
            if (reader != null) {
                reader.dispose();
                reader.reset();
            }
        }

        return pagesMap;
    }

    /**
     * @param pagesMap  imageList
     * @param imagePath - Tiff Image Destination Path
     * @throws IOException
     */
    public void createTiffFileFromImageList(HashMap<BufferedImage, IIOMetadata> pagesMap,
                                            String imagePath) throws IOException {

        File        imageFile      = new File(imagePath);
        String      imageExtension = FilenameUtils.getExtension(imagePath);
        ImageWriter writer         = ImageIO.getImageWritersByFormatName(imageExtension).next();

        try (ImageOutputStream outputStream = ImageIO.createImageOutputStream(imageFile);) {
            writer.setOutput(outputStream);

            ImageWriteParam imageWriteParam = writer.getDefaultWriteParam();
            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_COPY_FROM_METADATA);

            writer.prepareWriteSequence(pagesMap.get(null));

            pagesMap.forEach((bufferedImage,
                              metadata) -> {
                try {
                    if (bufferedImage != null)
                        writer.writeToSequence(new IIOImage(bufferedImage, null, metadata),
                                               imageWriteParam);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } finally {
            if (writer != null) {
                writer.endWriteSequence();
                writer.dispose();
                writer.reset();
            }
        }

    }

    /**
     * @param imageList       BufferedImage
     * @param modifyParameter
     * @return list of buffered image
     */
    public List<BufferedImage> modifyBufferedImagesInList(List<BufferedImage> imageList,
                                                          EditTiffParameter modifyParameter) {

        List<BufferedImage> modifiedImages = new ArrayList<BufferedImage>();

        for (BufferedImage image : imageList) {
            if (modifyParameter.isAddBorder()) {
                image = this.addBorder(image, modifyParameter.getBorderSize());
            }
            if (modifyParameter.isAddFilter()) {
                image = this.applyColorFilter(image, modifyParameter.getColorOption());
            }
            modifiedImages.add(image);
        }

        return modifiedImages;

    }

    /**
     * Add annotations to TIFF file
     *
     * @param tiffFilename
     * @param annotationFilename
     * @param documentColor
     * @return path of the file
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws XPathExpressionException
     */
    public String addAnnotationToTiff(String tiffFilename,
                                      String annotationFilename,
                                      DocumentColor documentColor) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        TiffInfoResponse tiffInfo           = getTiffInfo(tiffFilename);
        DocumentInfo     annotationDocument = null;
        if (annotationFilename != null) {
            annotationDocument = new DocumentInfo(annotationFilename, tiffInfo);
        }

        String                   outputPath  = "";
        ArrayList<BufferedImage> biArray     = new ArrayList<>();
        ArrayList<DocumentColor> targetColor = new ArrayList<>();
        LOGGER.info("ImageIO set the use cache to False");
        ImageIO.setUseCache(false);
        Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(FileType.tiff);
        if (readers.hasNext()) {
            File        fi          = new File(tiffFilename);
            ImageReader imageReader = readers.next();

            // use try-with-resource statement because the ImageInputStream is-a AutoClosable,
            // thus the stream will be closed automatically
            try (ImageInputStream iis = ImageIO.createImageInputStream(fi)) {

                if (imageReader != null) {
                    imageReader.setInput(iis, false);
                    int maxPageNumber = imageReader.getNumImages(true);
                    for (int pageNumber = 0; pageNumber < maxPageNumber; pageNumber++) {

                        IIOMetadata   metadata         = imageReader.getImageMetadata(pageNumber);
                        DocumentColor pageCurrentColor = getDocumentColorType(metadata,
                                                                              imageReader.getRawImageType(pageNumber));
                        if (documentColor == DocumentColor.RGB ||
                            documentColor == DocumentColor.GREY ||
                            documentColor == DocumentColor.JPEG ||
                            documentColor == null) {
                            //burned annotation are always red (only exception is SEDEX where its black and white
                            if (!pageCurrentColor.equals(DocumentColor.RGB) &&
                                annotationDocument != null) {

                                for (PageInfo pageInfo : annotationDocument.getPageInfoList()) {
                                    if (pageInfo.getPageNumber() == pageNumber) {
                                        if (pageInfo.getLineAnnotationList().size() > 0 ||
                                            pageInfo.getRubberStampAnnotationList().size() > 0) {

                                            pageCurrentColor = DocumentColor.RGB;
                                            break;
                                        }
                                    }
                                }

                                biArray.add(addAnnotationToOnePage(annotationDocument,
                                                                   addImageAnnotation(annotationDocument,
                                                                                      pageNumber,
                                                                                      imageReader),
                                                                   pageNumber,
                                                                   false));

                            } else {
                                biArray.add(addAnnotationToOnePage(annotationDocument,
                                                                   imageReader.read(pageNumber),
                                                                   pageNumber,
                                                                   false));
                            }

                            targetColor.add(pageCurrentColor);
                        } else if (documentColor == DocumentColor.BLACK_AND_WHITE) { // the only case where the document should be in black and white is Sedex export
                            targetColor.add(DocumentColor.BLACK_AND_WHITE);
                            IIOMetadataNode root            = (IIOMetadataNode) metadata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
                            String          compressionName = ((IIOMetadataNode) root.getElementsByTagName(Constant.COMPRESSIONTYPENAME)
                                                                                     .item(0)).getAttribute(Constant.VALUE);
                            if (compressionName.equals("CCITT T.6")) {
                                biArray.add(addAnnotationToOnePage(annotationDocument,
                                                                   imageReader.read(pageNumber),
                                                                   pageNumber,
                                                                   true));
                            } else {
                                biArray.add(addAnnotationToOnePage(annotationDocument,
                                                                   DitheringUtils.SierraLiteDithering(convertAndResize(imageReader.read(pageNumber),
                                                                                                                       BufferedImage.TYPE_INT_RGB,
                                                                                                                       imageReader)), // convert is needed to swap between sRGB and RGB otherwise we have an image too bright
                                                                   pageNumber,
                                                                   true));
                            }
                        } else {
                            throw new IllegalArgumentException("Document color '" + documentColor +
                                                               "' is not known!");
                        }
                    }
                }
                outputPath = FilenameUtils.removeExtension(tiffFilename) + "WithAnnotation." +
                             FilenameUtils.getExtension(tiffFilename);
                saveTiff(biArray, outputPath, imageReader, targetColor);

                LOGGER.info("Release all object");

            } finally {

                if (imageReader != null) {
                    imageReader.dispose();
                    imageReader.reset();
                    imageReader = null;
                }

                if (biArray != null) {
                    biArray.clear();
                    biArray = null;
                }

                if (targetColor != null) {
                    targetColor.clear();
                    targetColor = null;
                }
            }
        }

        return outputPath;
    }

    /**
     * Add Annotation to TIFF
     *
     * @param DocumentInfo
     * @param pageNumber
     * @param ImageReader
     * @return BufferedImage
     * @throws IOException
     */
    private BufferedImage addImageAnnotation(DocumentInfo annotationDocument,
                                             int pageNumber,
                                             ImageReader imageReader) throws IOException {

        BufferedImage image = null;
        try {
            image = imageReader.read(pageNumber);
            for (PageInfo pageInfo : annotationDocument.getPageInfoList()) {
                if (pageInfo.getPageNumber() == pageNumber) {
                    if (pageInfo.getLineAnnotationList().size() > 0 ||
                        pageInfo.getRubberStampAnnotationList().size() > 0) {

                        image = convertAndResize(image, BufferedImage.TYPE_INT_RGB, imageReader);

                    }
                }
            }

            return image;
        } catch (IOException e) {

            LOGGER.error("{} Error while add Image Annotation page {}",
                         Constant.DATETIME_FORMAT.format(new Date()),
                         pageNumber);
            throw e;
        }

    }

    /**
     * @param filePath
     * @param rubberStampAnnotation
     * @return path of the file
     * @throws IOException
     */
    public String addFreeTextAnnotationToTiff(String filePath,
                                              RubberStampAnnotation rubberStampAnnotation) throws IOException {
        ArrayList<BufferedImage> biArray     = new ArrayList<BufferedImage>();
        String                   outputPath  = "";
        ArrayList<DocumentColor> targetColor = new ArrayList<>();
        ImageIO.setUseCache(false);
        Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(FileType.tiff);
        if (readers.hasNext()) {

            File          fi          = new File(filePath);
            ImageReader   imageReader = (readers.next());
            BufferedImage bi          = null;
            Graphics2D    g2d         = null;

            try (ImageInputStream iis = javax.imageio.ImageIO.createImageInputStream(fi)) {

                if (imageReader != null) {

                    imageReader.setInput(iis, false);
                    IIOMetadata metadata = imageReader.getImageMetadata(0);

                    int maxPageNumber = imageReader.getNumImages(true);
                    for (int pageNumber = 0; pageNumber < maxPageNumber; pageNumber++) {
                        targetColor.add(getDocumentColorType(metadata,
                                                             imageReader.getRawImageType(pageNumber)));
                        bi  = imageReader.read(pageNumber);
                        g2d = bi.createGraphics();
                        if (pageNumber == 0)
                            biArray.add(addFreeTextAnnotationToTiff(bi,
                                                                    g2d,
                                                                    rubberStampAnnotation));
                        else {
                            biArray.add(bi);
                        }

                    }
                }
                outputPath = FilenameUtils.removeExtension(filePath) + "WithAnnotation." +
                             FilenameUtils.getExtension(filePath);

                saveTiff(biArray, outputPath, imageReader, targetColor);

            } finally {

                if (g2d != null) {
                    g2d.dispose();
                    g2d = null;
                }

                if (bi != null) {
                    bi.flush();
                    bi = null;
                }

                if (imageReader != null) {
                    imageReader.dispose();
                    imageReader.reset();
                    imageReader = null;
                }

                if (biArray != null) {
                    biArray.clear();
                    biArray = null;
                }

                if (targetColor != null) {
                    targetColor.clear();
                    targetColor = null;
                }
            }

        }

        return outputPath;
    }

    /**
     * add a text annotation to a tiff
     *
     * @param imageReader
     * @param rubberStampAnnotation
     * @param outputPath
     * @return the output path
     * @throws IOException
     */
    public String addFreeTextAnnotationToTiff(ImageReader imageReader,
                                              RubberStampAnnotation rubberStampAnnotation,
                                              String outputPath) throws IOException {
        ArrayList<BufferedImage> biArray     = new ArrayList<BufferedImage>();
        ArrayList<DocumentColor> targetColor = new ArrayList<>();

        IIOMetadata metadata = imageReader.getImageMetadata(0);

        int           maxPageNumber = imageReader.getNumImages(true);
        BufferedImage bi            = null;
        Graphics2D    g2d           = null;

        try {
            for (int pageNumber = 0; pageNumber < maxPageNumber; pageNumber++) {
                targetColor.add(getDocumentColorType(metadata,
                                                     imageReader.getRawImageType(pageNumber)));
                bi  = imageReader.read(pageNumber);
                g2d = bi.createGraphics();
                if (pageNumber == 0)
                    biArray.add(addFreeTextAnnotationToTiff(bi, g2d, rubberStampAnnotation));
                else {
                    biArray.add(bi);
                }

            }

            saveTiff(biArray, outputPath, imageReader, targetColor);
        }

        finally {

            if (g2d != null) {
                g2d.dispose();
                g2d = null;
            }

            if (bi != null) {
                bi.flush();
                bi = null;
            }

            if (imageReader != null) {
                imageReader.dispose();
                imageReader.reset();
                imageReader = null;
            }

            if (biArray != null) {
                biArray.clear();
                biArray = null;
            }

            if (targetColor != null) {
                targetColor.clear();
                targetColor = null;
            }

        }

        return outputPath;
    }

    /**
     * Add free text annotation to TIFF file
     *
     * @param bImage
     * @param g2d
     * @param rubberStampAnnotation
     * @return
     * @throws IOException
     */
    private BufferedImage addFreeTextAnnotationToTiff(BufferedImage bImage,
                                                      Graphics2D g2d,
                                                      RubberStampAnnotation rubberStampAnnotation) throws IOException {

        Double startX = rubberStampAnnotation.getStartX();
        Double startY = rubberStampAnnotation.getStartY();
        Double endX   = rubberStampAnnotation.getEndX();
        Double endY   = rubberStampAnnotation.getEndY();
        switch (rubberStampAnnotation.getFontColor()) {
            case "000000":
                Rectangle2D.Double rectangle = new Rectangle2D.Double(startX,
                                                                      startY - 5,
                                                                      endX + rubberStampAnnotation.getFontSize(),
                                                                      endY + 10);
                g2d.setStroke(new BasicStroke(5));
                g2d.setPaint(Color.black);
                g2d.draw(rectangle);
                break;
            case "Bl":
                g2d.setPaint(Color.black);
                break;
            default:
                g2d.setPaint(Color.red);
        }

        Font font;
        if (rubberStampAnnotation.getFontBold().equals("true") &&
            rubberStampAnnotation.getFontItalic().equals("true")) {
            font = new Font(rubberStampAnnotation.getFontName(),
                            Font.BOLD | Font.ITALIC,
                            rubberStampAnnotation.getFontSizeInPixel());
        } else if (rubberStampAnnotation.getFontBold().equals("true")) {
            font = new Font(rubberStampAnnotation.getFontName(),
                            Font.BOLD,
                            rubberStampAnnotation.getFontSizeInPixel());
        } else if (rubberStampAnnotation.getFontItalic().equals("true")) {
            font = new Font(rubberStampAnnotation.getFontName(),
                            Font.ITALIC,
                            rubberStampAnnotation.getFontSizeInPixel());
        } else {
            font = new Font(rubberStampAnnotation.getFontName(),
                            Font.PLAIN,
                            rubberStampAnnotation.getFontSizeInPixel());
        }
        g2d.setFont(font);
        FontMetrics fm           = bImage.getGraphics().getFontMetrics(font);
        String[]    allLine      = rubberStampAnnotation.getTextString().split("\n");
        String      stringToDraw = "";
        for (int i = 0; i < allLine.length; i++) {
            stringToDraw = stringToDraw + formatString(allLine[i], fm, endX);
            stringToDraw = stringToDraw + "\n";
        }

        drawString(g2d,
                   stringToDraw,
                   rubberStampAnnotation.getCenterText(),
                   startX.floatValue() + 10,
                   startY.floatValue() - rubberStampAnnotation.getFontSize() - 3,
                   startY + endY,
                   endX);

        return bImage;
    }

    /**
     * Add line annotation to TIFF file
     *
     * @param bImage
     * @param g2d
     * @param pLineAnnotation
     * @return
     */
    private BufferedImage addLineAnnotationToTiff(BufferedImage bImage,
                                                  Graphics2D g2d,
                                                  LineAnnotation pLineAnnotation) {

        g2d.setStroke(new BasicStroke((float) pLineAnnotation.getLineSize()));
        g2d.setColor(Color.red);
        g2d.draw(new Line2D.Double(pLineAnnotation.getStartX(),
                                   pLineAnnotation.getStartY(),
                                   pLineAnnotation.getEndX(),
                                   pLineAnnotation.getEndY()));

        return bImage;
    }

    /**
     * Change tiff compression
     *
     * @param tiffFilename Tiff filename
     * @return Tiff file
     * @throws IOException
     */
    public String changeTiffCompression(String tiffFilename) throws IOException {
        if (!FileHelper.getFileExtension(tiffFilename, true).equals(FileType.tif) &&
            !FileHelper.getFileExtension(tiffFilename, true).equals(FileType.tiff)) {
            return tiffFilename;
        }

        ArrayList<BufferedImage> biArray     = new ArrayList<BufferedImage>();
        ArrayList<DocumentColor> targetColor = new ArrayList<>();
        String                   outputPath  = "";
        LOGGER.info("ImageIO set the use cache to False");
        ImageIO.setUseCache(false);
        Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(FileType.tiff);
        if (readers.hasNext()) {
            File        fi          = new File(tiffFilename);
            ImageReader imageReader = (readers.next());

            // use try-with-resource statement because the ImageInputStream is-a AutoClosable,
            // thus the stream will be closed automatically
            try (ImageInputStream iis = ImageIO.createImageInputStream(fi)) {

                if (imageReader != null) {

                    imageReader.setInput(iis, false);
                    IIOMetadata     metadata        = imageReader.getImageMetadata(0);
                    IIOMetadataNode root            = (IIOMetadataNode) metadata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
                    String          compressionName = ((IIOMetadataNode) root.getElementsByTagName(Constant.COMPRESSIONTYPENAME)
                                                                             .item(0)).getAttribute(Constant.VALUE);

                    if (!compressionName.equals("Old JPEG")) {
                        return tiffFilename;
                    }

                    int maxPageNumber = imageReader.getNumImages(true);
                    for (int pageNumber = 0; pageNumber < maxPageNumber; pageNumber++) {
                        targetColor.add(DocumentColor.JPEG);
                        biArray.add(imageReader.read(pageNumber));
                    }
                }
                outputPath = FilenameUtils.removeExtension(tiffFilename) + "Compressed." +
                             FilenameUtils.getExtension(tiffFilename);
                saveTiff(biArray, outputPath, imageReader, targetColor);
            }

            finally {
                if (imageReader != null) {
                    imageReader.dispose();
                    imageReader.reset();
                }

                if (biArray != null) {
                    biArray.clear();
                    biArray = null;
                }

                if (targetColor != null) {
                    targetColor.clear();
                    targetColor = null;
                }
            }
        }
        return outputPath;
    }

    /**
     * Convert image type and resize image Format A4 for 300 dpi
     *
     * @param src
     * @param bufImgType
     * @param imageReader
     * @return BufferedImage
     */
    public BufferedImage convertAndResize(BufferedImage src,
                                          int bufImgType,
                                          ImageReader imageReader) throws IOException {
        Graphics2D    g2d = null;
        BufferedImage img = null;

        try {

            int originalWidth  = src.getWidth();
            int originalHeight = src.getHeight();

            int width  = 2480; // Format A4 for 300 dpi
            int height = 3508;

            int xMargin      = 0;
            int yMargin      = 0;
            int targetWidth  = 0;
            int targetHeight = 0;

            // determine landscape or not
            if (originalWidth < originalHeight) {
                targetWidth  = width;
                targetHeight = height;

            } else {
                width        = 3508;
                height       = 2480;
                targetWidth  = width;
                targetHeight = height;
            }

            double coefWidth = 0, coefHeight = 0, coef = 0;

            int dpi = 300;
            if (imageReader != null)
                dpi = getTiffDpi(imageReader);

            // Cases to keep original dpi
            if ((dpi >= 200 && dpi < 300) || dpi > 300) {
                width  = originalWidth;
                height = originalHeight;
            }

            if ((originalWidth < originalHeight)) {

                // Check factor for scaling image
                if (originalWidth > width - 5 && originalWidth < width + 5) {
                    coefWidth = 1;
                    xMargin   = 0;
                    yMargin   = 0;
                } else {
                    coefWidth = (double) width / originalWidth;
                    xMargin   = 100;
                    yMargin   = 100;
                }

                coef = coefWidth;

            } else {

                if (dpi != 300) {
                    width  = originalHeight;
                    height = originalWidth;
                }

                if (originalHeight > height - 5 && originalHeight < height + 5) {
                    coefHeight = 1;
                    xMargin    = 0;
                    yMargin    = 0;
                } else {
                    coefHeight = (double) height / originalHeight;
                    xMargin    = 100;
                    yMargin    = 100;
                }

                coef = coefHeight;
            }

            if (xMargin > 0 && originalWidth < width + 5)
                targetWidth = (int) (originalWidth * coef) - 200;
            else {
                xMargin = 0;
            }

            if (yMargin > 0 && originalHeight < height + 5)
                targetHeight = (int) (originalHeight * coef) - 200;
            else {
                yMargin = 0;
            }

            img = new BufferedImage(width, height, bufImgType);

            g2d = img.createGraphics();

            if (targetWidth == originalWidth && targetHeight == originalHeight)
                g2d.drawImage(src, 0, 0, Color.WHITE, null);
            else {

                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

                g2d.setRenderingHint(RenderingHints.KEY_DITHERING,
                                     RenderingHints.VALUE_DITHER_ENABLE);

                g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                                     RenderingHints.VALUE_COLOR_RENDER_QUALITY);

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_OFF);

                g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                                     RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

                g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                                     RenderingHints.VALUE_FRACTIONALMETRICS_ON);

                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                     RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                                     RenderingHints.VALUE_RENDER_QUALITY);

                g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                                     RenderingHints.VALUE_COLOR_RENDER_QUALITY);

                g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                                     RenderingHints.VALUE_STROKE_NORMALIZE);

                g2d.setBackground(Color.white);
                g2d.clearRect(0, 0, width, height);

                g2d.drawImage(src, xMargin, yMargin, targetWidth, targetHeight, Color.WHITE, null);
            }

            return img;

        } finally {
            if (g2d != null) {
                g2d.dispose();
                g2d = null;
            }
            img = null;
        }
    }

    /**
     * Draw a string
     *
     * @param g          Graphics2D object
     * @param text       The text to draw
     * @param centerText If the text must be centered inside the box
     * @param x          X position
     * @param y          Y position
     * @param height     The height
     * @param width      The width
     */
    private void drawString(Graphics2D g,
                            String text,
                            boolean centerText,
                            float x,
                            float y,
                            double height,
                            double width) {

        for (String line : text.split("\n")) {
            y += (g.getFontMetrics().getHeight());
            if (y < height) {
                if (centerText) {
                    g.drawString(line,
                                 x + (((float) (width - g.getFontMetrics().stringWidth(line)) / 2)),
                                 y);
                } else {
                    g.drawString(line, x, y);
                }
            }
        }
    }

    /**
     * Format string
     *
     * @param str
     * @param fm
     * @param endX
     * @return Formatted string
     */
    private String formatString(String str,
                                FontMetrics fm,
                                Double endX) {
        StringBuilder stringBuilderTextFormated = new StringBuilder();
        StringBuilder stringBuilderTextTemp     = new StringBuilder();
        int           wordPointer               = 1;
        boolean       firstWordOfLine           = true;

        while (str.length() > 0) {
            // Word need to be cut only if they are the first word of a line
            if ((fm.stringWidth(stringBuilderTextTemp.toString()) == 0 ||
                 fm.stringWidth(stringBuilderTextTemp.toString()) + fm.stringWidth(str.substring(0,
                                                                                                 1)) < endX) &&
                firstWordOfLine) {
                stringBuilderTextTemp.append(str.charAt(0));

                if (str.charAt(0) == ' ') {
                    firstWordOfLine = false;
                }
                str = str.substring(1, str.length());
            }
            // if the word is not complet, the wordpointed will be extended by 1 character
            else if (!firstWordOfLine && str.length() != wordPointer &&
                     str.charAt(wordPointer) != ' ') {
                wordPointer = wordPointer + 1;
            }
            // If a word is the second word of a line, its only added when it fit into the line
            else if (!firstWordOfLine &&
                     (str.length() == wordPointer || str.charAt(wordPointer) == ' ') &&
                     fm.stringWidth(stringBuilderTextTemp.toString()) + fm.stringWidth(str.substring(0,
                                                                                                     wordPointer)) < endX) {
                stringBuilderTextTemp.append(str.substring(0,
                                                           Math.min(wordPointer + 1,
                                                                    str.length())));
                str         = str.substring(Math.min(wordPointer + 1, str.length()), str.length());
                wordPointer = 1;
            }
            // word need to be added once the line is full
            else {
                stringBuilderTextFormated.append(stringBuilderTextTemp.toString());
                stringBuilderTextFormated.append("\n");
                stringBuilderTextTemp.setLength(0);
                firstWordOfLine = true;
                wordPointer     = 1;
            }
        }
        stringBuilderTextFormated.append(stringBuilderTextTemp.toString());
        return stringBuilderTextFormated.toString();

    }

    /**
     * Determine document color
     *
     * @param metadata  Document metadata
     * @param imageType Image type specifier
     * @return DocumentColor
     */
    private DocumentColor getDocumentColorType(IIOMetadata metadata,
                                               ImageTypeSpecifier imageType) {

        String metaDocumentColor = metadata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName)
                                           .getChildNodes().item(0).getChildNodes().item(0)
                                           .getAttributes().getNamedItem(Constant.NAME)
                                           .getNodeValue();

        if (DocumentColor.fromString(metaDocumentColor) == DocumentColor.RGB) {
            return DocumentColor.RGB;
        }

        IIOMetadataNode root            = (IIOMetadataNode) metadata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
        String          compressionName = ((IIOMetadataNode) root.getElementsByTagName(Constant.COMPRESSIONTYPENAME)
                                                                 .item(0)).getAttribute(Constant.VALUE);
        if (imageType.getNumBands() == 1 && imageType.getBitsPerBand(0) == 1 &&
            compressionName.equals("CCITT T.6")) {
            return DocumentColor.BLACK_AND_WHITE;
        } else if (compressionName.equals("JPEG")) {
            return DocumentColor.JPEG;
        }

        return DocumentColor.GREY;
    }

    /**
     * Get the number of pages of a TIFF file
     *
     * @param tiffFileName TIFF file
     * @return The number of pages
     * @throws IOException
     */
    public int getNumberOfPages(String tiffFileName) throws IOException {

        File        imageFile = new File(tiffFileName);
        ImageReader reader    = ImageIO.getImageReadersByFormatName(FileType.tiff).next();

        try (ImageInputStream inputStream = ImageIO.createImageInputStream(imageFile)) {

            try {
                reader.setInput(inputStream);
                return reader.getNumImages(true);
            } finally {
                imageFile = null;
                if (reader != null) {
                    reader.dispose();
                    reader.reset();
                }
            }
        }

    }

    /**
     * Get info data out of a TIFF file
     *
     * @param tiffPath The TIFF file
     * @return TiffInfoResponse object
     * @throws IOException
     */
    public TiffInfoResponse getTiffInfo(String tiffPath) throws IOException {

        TiffInfoResponse   tiffInfoResponse = new TiffInfoResponse();
        ArrayList<Integer> pageHeighList    = new ArrayList<>();
        ArrayList<Integer> pageWidthList    = new ArrayList<>();
        File               tiffFile         = new File(tiffPath);

        FileHelper.assertExists(tiffPath);

        // use try-with-resource statement because the ImageInputStream is-a AutoClosable,
        // thus the stream will be closed automatically
        try (ImageInputStream stream = ImageIO.createImageInputStream(tiffFile)) {

            Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(FileType.tiff);

            if (readers.hasNext()) {
                ImageReader reader = readers.next();

                if (reader == null) {
                    throw new NullPointerException("Unable to get ImageReader instance");
                }

                reader.setInput(stream, false);
                tiffInfoResponse.setDpi(getTiffDpi(reader));
                int MaxPageNumber = reader.getNumImages(true);
                for (int pageNumber = 0; pageNumber < MaxPageNumber; pageNumber++) {
                    BufferedImage bi = reader.read(pageNumber);
                    pageHeighList.add(bi.getHeight());
                    pageWidthList.add(bi.getWidth());
                    bi.flush();
                }
                tiffInfoResponse.setPageHeightList(pageHeighList);
                tiffInfoResponse.setPageWidthList(pageWidthList);
                reader.dispose();
                reader.reset();
            } else {

                throw new FileNotFoundException("Could not read: " + tiffPath);
            }

        }

        return tiffInfoResponse;
    }

    /**
     * Get info data out of a TIFF file
     *
     * @param imageReader
     * @return TiffInfoResponse object
     * @throws IOException
     */
    public TiffInfoResponse getTiffInfo(ImageReader imageReader) throws IOException {

        TiffInfoResponse   tiffInfoResponse = new TiffInfoResponse();
        ArrayList<Integer> pageHeighList    = new ArrayList<>();
        ArrayList<Integer> pageWidthList    = new ArrayList<>();

        tiffInfoResponse.setDpi(getTiffDpi(imageReader));
        int MaxPageNumber = imageReader.getNumImages(true);
        for (int pageNumber = 0; pageNumber < MaxPageNumber; pageNumber++) {
            BufferedImage bi = imageReader.read(pageNumber);
            pageHeighList.add(bi.getHeight());
            pageWidthList.add(bi.getWidth());
            bi = null;
        }
        tiffInfoResponse.setPageHeightList(pageHeighList);
        tiffInfoResponse.setPageWidthList(pageWidthList);

        return tiffInfoResponse;
    }

    /**
     * Get the TIFF Dpi value
     *
     * @param reader ImageReader
     * @return
     * @throws IOException
     */
    private int getTiffDpi(ImageReader reader) throws IOException {
        if (reader.getNumImages(true) > 0) {
            BufferedImage bi     = reader.read(0);
            float         height = bi.getHeight();
            float         width  = bi.getWidth();
            float         coef   = 0;
            if (height > width) {
                coef = height / width;
            } else {
                coef = width / height;
            }
            if (coef >= 1.40f && coef <= 1.42f) //A4 Format
            {
                return Math.round(width * 25.4f / 210); // width of a A4 page * 25.4 / width of a page in cm
            }
        }
        return getTiffDpiFromMetadata(reader);
    }

    /**
     * Get the TIFF Dpi value
     * NOTE: it is also possible to get the DPI from format
     * 'com_sun_media_imageio_plugins_tiff_image_1.0'
     * and Node 'TIFFIFD/TIFFField/XResolution/TIFFRationals/TIFFRational/value' (and YResolution)
     *
     * @param reader ImageReader
     * @return
     * @throws IOException
     */
    private int getTiffDpiFromMetadata(ImageReader reader) throws IOException {
        int xDPI = 0;
        int yDPI = 0;

        IIOMetadata meta = reader.getImageMetadata(0);

        Node imageioNode = meta.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName); // "javax_imageio_1.0"
        imageioNode = imageioNode.getFirstChild();

        while (imageioNode != null) {
            if (imageioNode.getNodeName().equals("Dimension")) {
                Node dimensionChildNode = imageioNode.getFirstChild();
                while (dimensionChildNode != null) {
                    if (dimensionChildNode.getNodeName().equals("HorizontalPixelSize")) {
                        float dotsPerMillimeter = Float.parseFloat(dimensionChildNode.getAttributes()
                                                                                     .getNamedItem("value")
                                                                                     .getNodeValue());
                        xDPI = Math.round(25.4f / dotsPerMillimeter);
                    }
                    if (dimensionChildNode.getNodeName().equals("VerticalPixelSize")) {
                        float dotsPerMillimeter = Float.parseFloat(dimensionChildNode.getAttributes()
                                                                                     .getNamedItem("value")
                                                                                     .getNodeValue());
                        yDPI = Math.round(25.4f / dotsPerMillimeter);
                    }
                    dimensionChildNode = dimensionChildNode.getNextSibling();
                }
            }
            imageioNode = imageioNode.getNextSibling();
        }

        if (xDPI == yDPI) {
            return xDPI;
        }

        return 0;
    }

    /**
     * save TIFF file with Write to sequence
     *
     * @param biArray            Bits array of the document
     * @param outputFilePath     Output file path
     * @param imageReader        The Image reader
     * @param targetColor
     * @param documentTagetColor Document color
     * @throws IOException
     */
    public void saveTiff(ArrayList<BufferedImage> biArray,
                         String outputFilePath,
                         ImageReader imageReader,
                         ArrayList<DocumentColor> targetColor) throws IOException {

        LOGGER.info("{} Start saving TIFF file: {}",
                    Constant.DATETIME_FORMAT.format(new Date()),
                    outputFilePath);

        ImageWriter     writer     = ImageIO.getImageWritersByFormatName(FileType.tiff).next();
        File            tiffFile   = FileHelper.createNewFile(outputFilePath);
        BufferedImage   image      = null;
        IIOImage        iIOImage   = null;
        IIOMetadata     metadata   = null;
        ImageWriteParam params     = null;
        ColorModel      colorModel = null;

        try (ImageOutputStream output = ImageIO.createImageOutputStream(tiffFile)) {
            writer.setOutput(output);

            params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

            writer.prepareWriteSequence(null);

            for (BufferedImage imageArray : biArray) {

                DocumentColor pageTargetColor;
                DocumentColor pageCurrentColor = null;
                int           pageNr           = biArray.indexOf(imageArray);

                if (imageReader != null) {
                    metadata = imageReader.getImageMetadata(pageNr);

                    pageTargetColor  = null;
                    pageCurrentColor = getDocumentColorType(metadata,
                                                            imageReader.getRawImageType(pageNr));
                }

                boolean colorChanged;
                int     colorModelConvert = 0;

                if (targetColor.get(pageNr) == null) {
                    // when no (document) target-color is requested, don't change the page-color and use the existing page color
                    pageTargetColor = pageCurrentColor;
                    colorChanged    = false;
                } else {
                    // page color should be the requested target color
                    pageTargetColor = targetColor.get(pageNr);
                    colorChanged    = (!pageTargetColor.equals(pageCurrentColor));
                }

                // Compression: None, PackBits, ZLib, Deflate, LZW, JPEG and CCITT variants allowed
                // (different plugins may use a different set of compression type names)
                switch (pageTargetColor) {
                    case RGB:
                        if (colorChanged)
                            colorModelConvert = BufferedImage.TYPE_INT_RGB;
                        params.setCompressionType("LZW");
                        break;
                    case GREY:
                        if (colorChanged)
                            colorModelConvert = BufferedImage.TYPE_BYTE_GRAY;
                        params.setCompressionType("LZW");
                        break;
                    case BLACK_AND_WHITE:
                        if (colorChanged)
                            colorModelConvert = BufferedImage.TYPE_BYTE_BINARY;
                        params.setCompressionType("CCITT T.6");
                        break;
                    case JPEG:
                        if (colorChanged)
                            colorModelConvert = BufferedImage.TYPE_INT_RGB;
                        params.setCompressionType("JPEG");
                        break;
                    default:
                        if (colorChanged)
                            colorModelConvert = BufferedImage.TYPE_INT_RGB;
                        params.setCompressionType("LZW");
                }

                if (colorChanged)
                    image = convertAndResize(imageArray, colorModelConvert, imageReader);
                else
                    image = imageArray;

                colorModel = image.getColorModel();
                params.setDestinationType(new ImageTypeSpecifier(colorModel,
                                                                 colorModel.createCompatibleSampleModel(32,
                                                                                                        32)));

                iIOImage = new IIOImage(image, null, null);
                writer.writeToSequence(iIOImage, params);
                image.flush();
            }

            output.flush();

        } finally {
            if (writer != null) {
                writer.endWriteSequence();
                writer.dispose();
                writer.reset();
            }

            image      = null;
            iIOImage   = null;
            metadata   = null;
            params     = null;
            colorModel = null;

            LOGGER.info("{} Finished saving TIFF file",
                        Constant.DATETIME_FORMAT.format(new Date()));
        }

    }

    /**
     * Split a TIFF file: each page is saved as a seperate file to a subfolder where
     * the original TIFF file is located. The subfolder will be named 'split' when the
     * 2nd parameter is null or empty
     *
     * @param tiffFilename    The TIFF file to split
     * @param documentColor   Document color
     * @param splitFolderName Optional parameter for the subfolder name, otherwise it is 'split'
     * @return String of the full split folder
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public String splitTiff(String tiffFilename,
                            DocumentColor documentColor,
                            String splitFolderName) throws ParserConfigurationException, SAXException, IOException {

        tiffFilename = changeTiffCompression(tiffFilename);
        ArrayList<DocumentColor> targetColor = new ArrayList<>();

        LOGGER.info("ImageIO set the use cache to False");
        ImageIO.setUseCache(false);
        Iterator<ImageReader> readers        = ImageIO.getImageReadersBySuffix(FileType.tiff);
        Path                  splitSubFolder = Path.of(FileHelper.getDirPath(tiffFilename),
                                                       (splitFolderName == null ||
                                                        splitFolderName.isBlank()) ? "split"
                                                                                   : splitFolderName);
        // first (re)create the splitfolder
        FileHelper.deleteWholeDirectoryIfExists(splitSubFolder.toFile());
        if (!FileHelper.ensureDirectoryExist(splitSubFolder))
            throw new NoSuchFileException(splitSubFolder.toString());

        if (readers.hasNext()) {
            File                     fi          = new File(tiffFilename);
            ImageReader              imageReader = readers.next();
            ArrayList<BufferedImage> images      = null;

            // use try-with-resource statement because the ImageInputStream is-a AutoClosable,
            // thus the stream will be closed automatically
            try (ImageInputStream iis = ImageIO.createImageInputStream(fi)) {

                if (imageReader != null) {

                    imageReader.setInput(iis, false);
                    int numImages = imageReader.getNumImages(true);

                    for (int pageNumber = 0; pageNumber < numImages; pageNumber++) {
                        targetColor.add(documentColor);
                        images = new ArrayList<BufferedImage>();
                        images.add(imageReader.read(pageNumber));
                        Path splittedTifFile = Path.of(splitSubFolder.toString(),
                                                       String.format("%04d.%s",
                                                                     pageNumber + 1,
                                                                     FileHelper.getFileExtension(tiffFilename,
                                                                                                 true)));
                        saveTiff(images, splittedTifFile.toString(), imageReader, targetColor);

                    }
                }

            } finally {
                LOGGER.info("Release all object");
                if (imageReader != null) {
                    imageReader.dispose();
                    imageReader.reset();
                }
                fi.delete();
                fi     = null;
                images = null;
            }
        }
        return splitSubFolder.toString();
    }

    /**
     * Edit a tiff image file and save the file
     *
     * @param imagePath       <a href="#{@link}">{@link BufferedImage} File Tiff image file path
     * @param modifyParameter <a href="#{@link}">{@link EditTiffParameter} modifyParameter
     *                        containing
     *                        edit options
     * @throws IOException
     */
    public void editTiff(String imagePath,
                         EditTiffParameter modifyParameter) throws IOException {

        if (!modifyParameter.isAddBorder() && !modifyParameter.isAddFilter()) {
            return;
        }

        Date dateStart = new Date();
        LOGGER.info("{} Started applying image modifications for Tiff file: {}",
                    Constant.DATETIME_FORMAT.format(dateStart),
                    imagePath);

        HashMap<BufferedImage, IIOMetadata> pagesMap = getPagesListFromTiffFile(imagePath,
                                                                                modifyParameter);

        createTiffFileFromImageList(pagesMap, imagePath);

        Date dateFinish = new Date();
        LOGGER.info("{} Tiff editing completed {} milliseconds",
                    Constant.DATETIME_FORMAT.format(dateFinish),
                    (dateFinish.getTime() - dateStart.getTime()));
    }

    /**
     * Get the TIFF DpiX value
     * NOTE: it is also possible to get the DPI from format
     * 'com_sun_media_imageio_plugins_tiff_image_1.0'
     * and Node 'TIFFIFD/TIFFField/XResolution/TIFFRationals/TIFFRational/value' (and YResolution)
     *
     * @param reader ImageReader
     * @return
     * @throws IOException
     */
    private int getTiffDpiX(ImageReader reader) throws IOException {
        int xDPI = 0;

        IIOMetadata meta = reader.getImageMetadata(0);

        Node imageioNode = meta.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName); // "javax_imageio_1.0"
        imageioNode = imageioNode.getFirstChild();

        while (imageioNode != null) {
            if (imageioNode.getNodeName().equals("Dimension")) {
                Node dimensionChildNode = imageioNode.getFirstChild();
                while (dimensionChildNode != null) {
                    if (dimensionChildNode.getNodeName().equals("HorizontalPixelSize")) {
                        float dotsPerMillimeter = Float.parseFloat(dimensionChildNode.getAttributes()
                                                                                     .getNamedItem("value")
                                                                                     .getNodeValue());
                        xDPI = Math.round(25.4f / dotsPerMillimeter);
                    }
                    dimensionChildNode = dimensionChildNode.getNextSibling();
                }
            }
            imageioNode = imageioNode.getNextSibling();
        }

        return xDPI;
    }

    /**
     * Get the TIFF DpiY value
     * NOTE: it is also possible to get the DPI from format
     * 'com_sun_media_imageio_plugins_tiff_image_1.0'
     * and Node 'TIFFIFD/TIFFField/XResolution/TIFFRationals/TIFFRational/value' (and YResolution)
     *
     * @param reader ImageReader
     * @return
     * @throws IOException
     */
    private int getTiffDpiY(ImageReader reader) throws IOException {
        int yDPI = 0;

        IIOMetadata meta = reader.getImageMetadata(0);

        Node imageioNode = meta.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName); // "javax_imageio_1.0"
        imageioNode = imageioNode.getFirstChild();

        while (imageioNode != null) {
            if (imageioNode.getNodeName().equals("Dimension")) {
                Node dimensionChildNode = imageioNode.getFirstChild();
                while (dimensionChildNode != null) {
                    if (dimensionChildNode.getNodeName().equals("VerticalPixelSize")) {
                        float dotsPerMillimeter = Float.parseFloat(dimensionChildNode.getAttributes()
                                                                                     .getNamedItem("value")
                                                                                     .getNodeValue());
                        yDPI = Math.round(25.4f / dotsPerMillimeter);
                    }
                    dimensionChildNode = dimensionChildNode.getNextSibling();
                }
            }
            imageioNode = imageioNode.getNextSibling();
        }

        return yDPI;
    }

    /**
     * calculation note : DPI = 25.4 * number_of_pixel / size_of_picture ---- size_of_picture =
     * 210mm x 297mm for A4 format
     * 1 dpi = 1 dot per inch ---- 1 inch = 25.4mm
     *
     * @param width
     * @return dpiX
     * @throws IOException
     */
    private int getDpiXFormatA4(int width) throws IOException {
        return Math.round((float) 25.4 * width / 210);
    }

    /**
     * @param height
     * @return dpiY
     * @throws IOException
     */
    private int getDpiYFormatA4(int height) throws IOException {
        return Math.round((float) 25.4 * height / 297);
    }

    /**
     * convert mm to pixel
     *
     * @param dpi
     * @param distance
     * @return
     * @throws IOException
     */
    private int mmToPix(int dpi,
                        int distance) throws IOException {
        return Math.round((float) (dpi / 25.4) * distance);

    }

    /**
     * get the coordinate where the location is empty
     *
     * @param imageReader
     * @param coordinateList
     * @param fontBold
     * @param fontItalic
     * @param fontName
     * @param fontSize
     * @param textString
     * @return the first coordinate where the location is empty
     * @throws IOException
     */
    public CoordinateParameter getBestCoordinateForAnnotation(ImageReader imageReader,
                                                              String coordinateList,
                                                              String fontBold,
                                                              String fontItalic,
                                                              String fontName,
                                                              String fontSize,
                                                              String textString) throws IOException {

        int dpiX = getTiffDpiX(imageReader);
        int dpiY = getTiffDpiY(imageReader);

        BufferedImage bi = imageReader.read(0);

        // if the dpi cannot be get from the metadata, we need to calculate it
        // for this, we consider that the document is always a portrait A4 (21cm x 29.7cm)
        if (!(dpiX > 0)) {
            dpiX = getDpiXFormatA4(bi.getWidth());
        }
        if (!(dpiY > 0)) {
            dpiY = getDpiYFormatA4(bi.getHeight());
        }
        CoordinateParameter oCoordinateParameter = new CoordinateParameter();

        int      widthAnnotation  = mmToPix(dpiX, 24);
        int      heightAnnotation = mmToPix(dpiY, 14);
        String[] coordinateArray  = coordinateList.split("\\r?\\n|\\r");
        int      posX             = 0;
        int      posY             = 0;
        for (String coordinate : coordinateArray) {

            String[] positionArray = coordinate.split(",");
            if (positionArray.length == 2) {

                posX = mmToPix(dpiX, Integer.valueOf(positionArray[0].trim()));
                posY = mmToPix(dpiY, Integer.valueOf(positionArray[1].trim()));

                oCoordinateParameter = getBestCoordinateForAnnotation(bi,
                                                                      oCoordinateParameter,
                                                                      posX,
                                                                      widthAnnotation,
                                                                      posY,
                                                                      heightAnnotation);
                if (oCoordinateParameter.getHeight() != null) {
                    break;
                }
            }
        }
        // if no perfect coordinate are found, the first one is a bit twisted in order to see if it fit
        if (oCoordinateParameter.getHeight() == null) {
            String[] positionArray = coordinateArray[0].split(",");
            posX = mmToPix(dpiX, Integer.valueOf(positionArray[0]));
            posY = mmToPix(dpiY, Integer.valueOf(positionArray[1]));

            oCoordinateParameter = getBestCoordinateForAnnotation(bi,
                                                                  oCoordinateParameter,
                                                                  posX - 20,
                                                                  widthAnnotation,
                                                                  posY,
                                                                  heightAnnotation);

        }
        if (oCoordinateParameter.getHeight() == null) {
            String[] positionArray = coordinateArray[0].split(",");
            posX = mmToPix(dpiX, Integer.valueOf(positionArray[0]));
            posY = mmToPix(dpiY, Integer.valueOf(positionArray[1]));

            oCoordinateParameter = getBestCoordinateForAnnotation(bi,
                                                                  oCoordinateParameter,
                                                                  posX,
                                                                  widthAnnotation,
                                                                  posY - 20,
                                                                  heightAnnotation);

        }
        if (oCoordinateParameter.getHeight() == null) {
            String[] positionArray = coordinateArray[0].split(",");
            posX = mmToPix(dpiX, Integer.valueOf(positionArray[0]));
            posY = mmToPix(dpiY, Integer.valueOf(positionArray[1]));

            oCoordinateParameter = getBestCoordinateForAnnotation(bi,
                                                                  oCoordinateParameter,
                                                                  posX + 20,
                                                                  widthAnnotation,
                                                                  posY,
                                                                  heightAnnotation);

        }
        if (oCoordinateParameter.getHeight() == null) {
            String[] positionArray = coordinateArray[0].split(",");
            posX = mmToPix(dpiX, Integer.valueOf(positionArray[0]));
            posY = mmToPix(dpiY, Integer.valueOf(positionArray[1]));

            oCoordinateParameter = getBestCoordinateForAnnotation(bi,
                                                                  oCoordinateParameter,
                                                                  posX,
                                                                  widthAnnotation,
                                                                  posY + 20,
                                                                  heightAnnotation);

        }
        if (oCoordinateParameter.getHeight() == null) {
            String[] positionArray = coordinateArray[0].split(",");
            posX = mmToPix(dpiX, Integer.valueOf(positionArray[0]));
            posY = mmToPix(dpiY, Integer.valueOf(positionArray[1]));

            int annStartX = Math.max(posX - widthAnnotation / 2, 0);
            int annStartY = Math.max(posY - heightAnnotation / 2, 0);

            int minX = annStartX;
            if (bi.getWidth() < minX) {
                minX = bi.getWidth();
            }
            int maxX = annStartX + widthAnnotation;
            if (bi.getWidth() < maxX) {
                maxX = bi.getWidth();
            }

            int minY = annStartY;
            if (bi.getWidth() < minX) {
                minY = bi.getWidth();
            }
            int maxY = annStartY + heightAnnotation;
            if (bi.getWidth() < maxY) {
                maxY = bi.getWidth();
            }

            oCoordinateParameter.setHeight((double) heightAnnotation);
            oCoordinateParameter.setStartX((double) minX);
            oCoordinateParameter.setStartY((double) minY);
            oCoordinateParameter.setWidth((double) widthAnnotation);
        }

        return oCoordinateParameter;

    }

    private CoordinateParameter getBestCoordinateForAnnotation(BufferedImage bi,
                                                               CoordinateParameter oCoordinateParameter,
                                                               int posX,
                                                               int width,
                                                               int posY,
                                                               int height) {

        int annStartX = Math.max(posX - (width / 2), 0);
        int annStartY = Math.max(posY - (height / 2), 0);

        int biWidth  = bi.getWidth() - 30;
        int biHeight = bi.getHeight() - 30;

        int minX = annStartX;
        if (biWidth < minX) {
            minX = biWidth - width;
        }
        int maxX = annStartX + width;
        if (biWidth < maxX) {
            maxX = biWidth;
        }

        int minY = annStartY;
        if (biHeight < minY) {
            minY = biHeight - height;
        }
        int maxY = annStartY + height;
        if (biHeight < maxY) {
            maxY = biHeight;
        }
        minX = Math.max(minX, 0);
        maxX = Math.max(maxX, 0);
        minY = Math.max(minY, 0);
        maxY = Math.max(maxY, 0);

        if (isAreaWhite(bi, minX, maxX, minY, maxY)) {
            oCoordinateParameter.setHeight((double) height);
            oCoordinateParameter.setStartX((double) minX);
            oCoordinateParameter.setStartY((double) minY);
            oCoordinateParameter.setWidth((double) width);
        }

        return oCoordinateParameter;
    }

    private boolean isAreaWhite(BufferedImage bi,
                                int minX,
                                int maxX,
                                int minY,
                                int maxY) {
        int width  = maxX - minX;
        int height = maxY - minY;

        Color3i[][] buffer = new Color3i[width][height];

        Color3i[] palette = new Color3i[] {
                new Color3i(0, 0, 0), new Color3i(255, 255, 255)
        };

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                buffer[x - minX][y - minY] = new Color3i(bi.getRGB(x, y));
            }
        }
        Double pixelMoy = 0.0;
        int    cmpt     = 0;
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < minY + height; y++) {
                cmpt = cmpt + 1;

                pixelMoy = pixelMoy + Math.abs(findClosestPaletteColor(new Color3i(bi.getRGB(x, y)),
                                                                       palette).toColor().getRGB());

            }
        }
        pixelMoy = pixelMoy / (width * height);
        if (pixelMoy < 600000 /* treshhold */) {
            return true;
        } else {
            return false;
        }
    }

    private static Color3i findClosestPaletteColor(Color3i match,
                                                   Color3i[] palette) {
        Color3i closest = palette[0];

        for (Color3i color : palette) {
            if (color.diff(match) < closest.diff(match)) {
                closest = color;
            }
        }

        return closest;
    }

    /**
     * get the image reader from an image
     *
     * @param imagePath
     * @return image reader
     * @throws IOException
     */
    public ImageReader getImageReader(String imagePath) throws IOException {
        ImageInputStream iis = null;

        ImageIO.setUseCache(false);
        ImageReader           imageReader = null;
        Iterator<ImageReader> readers     = ImageIO.getImageReadersBySuffix(FileType.tiff);
        if (readers.hasNext()) {
            File fi = new File(imagePath);
            iis         = javax.imageio.ImageIO.createImageInputStream(fi);
            imageReader = (readers.next());
            if (imageReader != null) {

                imageReader.setInput(iis, false);
            }
        }
        return imageReader;

    }

}
