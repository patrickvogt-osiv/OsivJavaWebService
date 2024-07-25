package ch.osiv.helper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspose.barcode.generation.BarcodeGenerator;
import com.aspose.barcode.generation.CodeLocation;
import com.aspose.barcode.generation.DataMatrixEncodeMode;
import com.aspose.barcode.generation.EncodeTypes;
import com.aspose.cells.ImageOrPrintOptions;
import com.aspose.cells.ImageType;
import com.aspose.cells.Workbook;
import com.aspose.cells.WorkbookRender;
import com.aspose.email.MailMessage;
import com.aspose.email.SaveOptions;
import com.aspose.imaging.Image;
import com.aspose.imaging.fileformats.tiff.TiffImage;
import com.aspose.pdf.PageCollection;
import com.aspose.words.BreakType;
import com.aspose.words.CssStyleSheetType;
import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.HorizontalAlignment;
import com.aspose.words.HtmlSaveOptions;
import com.aspose.words.ImageSaveOptions;
import com.aspose.words.ImportFormatMode;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.RelativeHorizontalPosition;
import com.aspose.words.SaveFormat;
import com.aspose.words.Shape;
import com.aspose.words.TiffCompression;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.image.TiffImageData;
import com.itextpdf.io.source.IRandomAccessSource;
import com.itextpdf.io.source.RandomAccessFileOrArray;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import ch.osiv.Constant;
import ch.osiv.document.EditTiffParameter;
import ch.osiv.document.TiffFileHandler;
import ch.osiv.document.convert.DocumentColor;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;

/**
 * FileConverterHelper
 */
public class FileConverterHelper {

    private static final String           CONVERSION_NOT_SUPPORTED = "Source type '%s' and destination type '%s' combination not supported.";
    private static final String           ERROR                    = "Error: ";
    private static final String           ERROR_DELETE             = "Error deleting the pipe file: ";
    private static final String           ERROR_IMAGE_FILE         = "Image document not supported";
    private static final String           ERROR_MERGE_DOCUMENTS    = "Error merging documents: ";
    private static final String           EXTENSION_NOT_SUPPORTED  = "File extension '%s' not supported";
    private static final String           SOURCE_FILE_EMPTY_ERROR  = "sourceFile cannot be empty";
    private static final String           SOURCE_FILE_HIDDEN       = "sourceFile cannot be hidden";
    private static final String           SOURCE_NOT_FOLDER        = "sourceFile '%s' must be a folder";
    private static final Logger           logger                   = LoggerFactory.getLogger(FileConverterHelper.class);
    private static final SimpleDateFormat dateFormat               = Constant.DATETIME_FORMAT;

    static {
        logger.info("{} Static FileConverterHelper loaded", dateFormat.format(new Date()));
    }

    /**
     * Appends images to a word processing document
     *
     * @param documentName  Name of the word processing document
     * @param imageDocument Name of the image document - can be a stand-alone image or a document
     *                      containing images
     * @return JsonConversionResponse JSON response object
     * @throws Exception
     */
    public static JsonConversionResponse appendImage(String documentName,
                                                     String imageDocument) throws Exception {

        logger.info("appendImage start");

        if (isImage(imageDocument)) {
            return appendSingleImage(documentName, imageDocument);
        }

        if (isDoc(imageDocument)) {
            return appendImagesFromDocument(documentName, imageDocument);
        }

        throw new RuntimeException(ERROR_IMAGE_FILE);
    }

    /**
     * Appends a doc/docx document to another doc/docx word document
     *
     * @param firstDocumentName
     * @param secondDocumentName
     * @return JsonConversionResponse JSON response object
     * @throws Exception
     */
    public static JsonConversionResponse appendDocument(String firstDocumentName,
                                                        String secondDocumentName) throws Exception {

        logger.info("Starting appending document to document");

        JsonConversionResponse response       = new JsonConversionResponse();
        Document               firstDocument  = new Document(firstDocumentName);
        Document               secondDocument = new Document(secondDocumentName);

        DocumentBuilder builder = new DocumentBuilder(firstDocument);
        builder.moveToDocumentEnd();
        builder.insertBreak(BreakType.LINE_BREAK);

        builder.insertDocument(secondDocument, ImportFormatMode.KEEP_SOURCE_FORMATTING);

        FileHelper.trimBlankLinesFromDocument(firstDocument);
        builder.getDocument().save(firstDocumentName, SaveFormat.DOCX);

        response.setDestFile(firstDocumentName);
        response.setPageCount(firstDocument.getPageCount());

        builder = null;

        logger.info("Appending Doc to doc process ended");
        return response;
    }

    /**
     * Parses a word document and appends all images to the end of another word document
     *
     * @param documentName      Name of the destination word document
     * @param imageDocumentName Name of the document containing the images
     * @return JsonConversionResponse JSON response object
     * @throws Exception
     */
    public static JsonConversionResponse appendImagesFromDocument(String documentName,
                                                                  String imageDocumentName) throws Exception {

        logger.info("Starting appending images from document");

        JsonConversionResponse response  = new JsonConversionResponse();
        Document               doc       = new Document(documentName);
        Document               imagesDoc = new Document(imageDocumentName);
        DocumentBuilder        builder   = new DocumentBuilder(doc);
        @SuppressWarnings("unchecked")
        NodeCollection<Shape>  shapes    = imagesDoc.getChildNodes(NodeType.SHAPE, true);

        builder.moveToDocumentEnd();
        builder.insertBreak(BreakType.LINE_BREAK);

        for (Shape shape : (Iterable<Shape>) shapes) {
            if (shape.hasImage()) {
                shape.setRelativeHorizontalPosition(RelativeHorizontalPosition.PAGE);
                shape.setHorizontalAlignment(HorizontalAlignment.LEFT);
                builder.insertImage(shape.getImageData().toImage());
                builder.insertBreak(BreakType.LINE_BREAK);
            }
        }

        doc.save(documentName, SaveFormat.DOCX);

        response.setDestFile(documentName);
        response.setPageCount(doc.getPageCount());

        builder   = null;
        imagesDoc = null;
        doc       = null;

        logger.info("Appending from Doc file process ended");
        return response;

    }

    /**
     * Appends a single Image to the end of a word document
     *
     * @param documentName Name of the destination word document
     * @param image        Name of the image
     * @return JsonConversionResponse JSON response object
     * @throws Exception
     */
    public static JsonConversionResponse appendSingleImage(String documentName,
                                                           String image) throws Exception {

        logger.info("Starting appending single image");

        JsonConversionResponse response = new JsonConversionResponse();
        Document               doc      = new Document(documentName);
        DocumentBuilder        builder  = new DocumentBuilder(doc);

        builder.moveToDocumentEnd();
        builder.insertBreak(BreakType.LINE_BREAK);

        Shape shape = builder.insertImage(image);
        shape.setRelativeHorizontalPosition(RelativeHorizontalPosition.PAGE);
        shape.setHorizontalAlignment(HorizontalAlignment.LEFT);

        doc.save(documentName, SaveFormat.DOCX);

        response.setDestFile(documentName);
        response.setPageCount(doc.getPageCount());

        doc     = null;
        builder = null;

        logger.info("Appending single image process ended");
        return response;
    }

    /**
     * Convert a file to a new file type
     *
     * @param sourceFile   String Source file path
     * @param destFileType String Destination extension type
     * @return JsonConversionResponse JSON response object containing new file name
     * @throws Exception
     */
    public static JsonConversionResponse convertFileTo(String sourceFile,
                                                       String destFileType) throws Exception {

        FileOptions options = new FileOptions();
        options.setColorOption("");

        return convertFileTo(sourceFile, destFileType, options);
    }

    /**
     * Convert a file to a new file type
     *
     * @param sourceFile   String Source file path
     * @param destFileType String Destination extension type
     * @param options      <a href="#{@link}">{@link FileOptions}
     * @return JsonConversionResponse JSON response object containing new file name
     * @throws Exception
     */
    public static JsonConversionResponse convertFileTo(String sourceFile,
                                                       String destFileType,
                                                       FileOptions options) throws Exception {

        JsonConversionResponse ret = new JsonConversionResponse();

        try {
            sourceFile = URLDecoder.decode(sourceFile, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            ret.setErrorStatus(true);
            ret.setSourceFile(sourceFile);
            ret.setErrorMessage(Constant.ERROR_DECODING + e1.getMessage());
            logger.info(Constant.ERROR_DECODING + e1.getMessage());
            return ret;
        }
        String sourceFileType = FileHelper.getFileExtension(sourceFile, true);

        try {
            if (sourceFile == null || sourceFile.isEmpty()) {
                throw new RuntimeException(SOURCE_FILE_EMPTY_ERROR);
            }

            if (!FileHelper.fileExist(sourceFile)) {
                throw new RuntimeException(String.format(Constant.SOURCE_NOT_FOUND, sourceFile));
            }

            switch (sourceFileType) {
                case FileType.doc:
                case FileType.docx:
                    switch (destFileType) {
                        case FileType.pdf:
                            return convertDocToPdf(sourceFile);
                        case FileType.tiff:
                        case FileType.tif:
                            return convertDocToTiff(sourceFile, options);
                        case FileType.rtf:
                            return convertDocToRtf(sourceFile);
                        default:
                            throw new RuntimeException(String.format(CONVERSION_NOT_SUPPORTED,
                                                                     sourceFileType,
                                                                     destFileType));
                    }
                case FileType.pdf:
                    switch (destFileType) {
                        case FileType.tiff:
                        case FileType.tif:
                            return convertPdfToTiff(sourceFile, options);
                        default:
                            throw new RuntimeException(String.format(CONVERSION_NOT_SUPPORTED,
                                                                     sourceFileType,
                                                                     destFileType));
                    }
                case FileType.tiff:
                case FileType.tif:
                    switch (destFileType) {
                        case FileType.pdf:
                            return convertTiffToPdf(sourceFile);
                        default:
                            throw new RuntimeException(String.format(CONVERSION_NOT_SUPPORTED,
                                                                     sourceFileType,
                                                                     destFileType));
                    }
                case FileType.png:
                case FileType.jpeg:
                case FileType.jpg:
                    switch (destFileType) {
                        case FileType.tiff:
                            return convertImageToTiff(sourceFile, options);
                        default:
                            throw new RuntimeException(String.format(CONVERSION_NOT_SUPPORTED,
                                                                     sourceFileType,
                                                                     destFileType));
                    }
                case FileType.html:
                    switch (destFileType) {
                        case FileType.rtf:
                            return convertDocToRtf(sourceFile);
                        default:
                            throw new RuntimeException(String.format(CONVERSION_NOT_SUPPORTED,
                                                                     sourceFileType,
                                                                     destFileType));
                    }
                case FileType.rtf:
                    switch (destFileType) {
                        case FileType.html:
                            return convertDocToHtml(sourceFile);
                        default:
                            throw new RuntimeException(String.format(CONVERSION_NOT_SUPPORTED,
                                                                     sourceFileType,
                                                                     destFileType));
                    }
                case FileType.xlsx:
                case FileType.xls:
                    switch (destFileType) {
                        case FileType.tiff:
                            return convertXlsxtoTiff(sourceFile, options);
                        default:
                            throw new RuntimeException(String.format(CONVERSION_NOT_SUPPORTED,
                                                                     sourceFileType,
                                                                     destFileType));
                    }
                case FileType.msg:
                    switch (destFileType) {
                        case FileType.tiff:
                            return convertMsgtoTiff(sourceFile, options);
                        default:
                            throw new RuntimeException(String.format(CONVERSION_NOT_SUPPORTED,
                                                                     sourceFileType,
                                                                     destFileType));
                    }
                default:
                    throw new RuntimeException(String.format(CONVERSION_NOT_SUPPORTED,
                                                             sourceFileType,
                                                             destFileType));
            }
        } catch (Exception e) {
            ret.setErrorStatus(true);
            ret.setSourceFile(sourceFile);
            ret.setErrorMessage(ERROR + e.getMessage());
            e.printStackTrace();
            logger.info(ERROR + e.getMessage());

            return ret;
        }
    }

    /**
     * Merge all PDF files from a folder into one single file
     *
     * @param sourceFolder       Folder path
     * @param docOrderComperator The Comperator for the sorting (can be null)
     * @return JsonConversionResponse JSON response object containing the resulting PDF file name
     * @throws Exception
     */
    public static synchronized JsonConversionResponse mergePdfFilesIntoOne(String sourceFolder,
                                                                           Comparator<Path> docOrderComperator) throws Exception {

        Date dateStart = new Date();
        logger.info("{} Merge PDF started for: {}", dateFormat.format(dateStart), sourceFolder);

        sourceFolder = sourceFolder.replace("\\", "/").replace("\n", "");
        sourceFolder = URLDecoder.decode(sourceFolder, "UTF-8");

        if (Files.notExists(Path.of(sourceFolder))) {
            throw new IllegalArgumentException("Source folder '" + sourceFolder +
                                               "' does not exist!");
        }

        List<Path>             fileArray = new ArrayList<Path>();
        JsonConversionResponse ret       = new JsonConversionResponse();

        File resultFile = new File(sourceFolder,
                                   (FileHelper.getBaseName(sourceFolder) + "." + FileType.pdf));

        // delete old merge result if it already exists to prevent it to grow on each subsequent call for the same folder
        if (resultFile.exists()) {
            logger.info("Deleting existing file: {}", resultFile.toString());
            resultFile.delete();
        }

        fileArray = FileHelper.retrieveFilesListFromFolder(sourceFolder,
                                                           FileType.pdf,
                                                           docOrderComperator);
        if (fileArray.size() == 0) {
            ret.setErrorMessage(ERROR_MERGE_DOCUMENTS + "No pdf documents found");
            ret.setErrorStatus(true);
            return ret;
        }

        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        pdfMerger.setDestinationFileName(resultFile.toString());

        for (Path path : fileArray) {
            pdfMerger.addSource(path.toString());
        }

        pdfMerger.mergeDocuments(null);

        if (resultFile.exists()) {
            PDDocument mergedPdfFile = PDDocument.load(resultFile,
                                                       MemoryUsageSetting.setupTempFileOnly());
            ret.setSourceFile(sourceFolder);
            ret.setDestFile(resultFile.toString());
            ret.setPageCount(mergedPdfFile.getNumberOfPages());
            mergedPdfFile.close();
        } else {
            ret.setErrorMessage(ERROR_MERGE_DOCUMENTS + "");
            ret.setErrorStatus(true);
        }

        fileArray.clear();
        fileArray = null;

        Date dateFinish = new Date();
        logger.info("{} Merge PDF completed in {} milliseconds",
                    dateFormat.format(dateFinish),
                    (dateFinish.getTime() - dateStart.getTime()));

        return ret;
    }

    /**
     * Convert Doc file to PDF
     *
     * @param sourceFile Doc file Name
     * @return JsonConversionResponse JSON response object containing the resulting PDF file name
     * @throws Exception
     */
    private static JsonConversionResponse convertDocToPdf(String sourceFile) throws Exception {

        Date dateStart = new Date();
        logger.info("{} DOC to PDF conversion started for: {}",
                    dateFormat.format(dateStart),
                    sourceFile);

        // load the document
        Document doc = new Document(sourceFile);

        // save the new document
        String destFile = FileHelper.replaceFileExtensionWith(sourceFile, FileType.pdf);
        doc.save(destFile, SaveFormat.PDF);

        // build response object
        JsonConversionResponse ret = new JsonConversionResponse();
        ret.setSourceFile(sourceFile);
        ret.setDestFile(destFile);
        ret.setPageCount(doc.getPageCount());

        doc.cleanup();

        Date dateFinish = new Date();
        logger.info("{} DOC to PDF Conversion completed in {} milliseconds",
                    dateFormat.format(dateFinish),
                    (dateFinish.getTime() - dateStart.getTime()));

        doc = null;

        return ret;
    }

    /**
     * Convert TIFF file to PDF (with iText8)
     *
     * @param sourceFile TIFF file Name
     * @return JsonConversionResponse JSON response object containing the resulting PDF file name
     * @throws Exception
     */
    private static JsonConversionResponse convertTiffToPdf(String sourceFile) throws Exception {

        String pdfFileName = FileHelper.replaceFileExtensionWith(sourceFile, FileType.pdf);

        PdfDocument             pdfDoc          = null;
        IRandomAccessSource     accessSource    = null;
        RandomAccessFileOrArray accessFileArray = null;
        JsonConversionResponse  response        = new JsonConversionResponse();

        response.setSourceFile(sourceFile);

        Date dateStart = new Date();
        logger.info("{} TIFF to PDF (iText8) conversion started for: {}",
                    dateFormat.format(dateStart),
                    sourceFile);

        try {
            pdfDoc          = new PdfDocument(new PdfWriter(pdfFileName));
            accessSource    = new RandomAccessSourceFactory().createBestSource(sourceFile);
            accessFileArray = new RandomAccessFileOrArray(accessSource);

            int  numPages = TiffImageData.getNumberOfPages(accessFileArray);
            Path tiffFile = Paths.get(sourceFile);

            for (int i = 1; i <= numPages; i++) {
                ImageData imageData = ImageDataFactory.createTiff(tiffFile.toUri().toURL(),
                                                                  false,
                                                                  i,
                                                                  false);
                Rectangle rect      = PageSize.A4;
                PdfPage   page      = pdfDoc.addNewPage(new PageSize(rect));
                PdfCanvas canvas    = new PdfCanvas(page);

                canvas.addImageFittedIntoRectangle(imageData, rect, false);
            }

            response.setDestFile(pdfFileName);
            response.setPageCount(numPages);

            Date dateFinish = new Date();
            logger.info("{} TIFF to PDF (iText8) conversion completed in {} milliseconds",
                        dateFormat.format(dateFinish),
                        (dateFinish.getTime() - dateStart.getTime()));
        } catch (Exception ex) {
            logger.error("{} Error while converting TIFF to PDF: {}",
                         dateFormat.format(new Date()),
                         ex.getMessage());
            ex.printStackTrace();
        } finally {
            accessFileArray.close();
            accessSource.close();
            pdfDoc.close();
        }

        return response;
    }

    /**
     * Convert Image file to TIFF
     *
     * @param sourceFile Image (jpg, jpeg, png) file Name
     * @param options    <a href="#{@link}">{@link FileOptions}
     * @return JsonConversionResponse JSON response object containing the resulting PDF file name
     * @throws Exception
     */
    public static JsonConversionResponse convertImageToTiff(String sourceFile,
                                                            FileOptions options) throws Exception {

        Date dateStart = new Date();
        logger.info("{} Image to TIFF conversion started for: {}",
                    dateFormat.format(dateStart),
                    sourceFile);
        logger.info("Color: {} ", options.getColorOption());

        JsonConversionResponse ret = new JsonConversionResponse();

        String destFile = FileHelper.replaceFileExtensionWith(sourceFile, FileType.tiff);
        Files.deleteIfExists(Paths.get(destFile));

        BufferedImage   buffImage       = ImageIO.read(new File(sourceFile));
        TiffFileHandler tiffFileHandler = new TiffFileHandler();
        if (options.getColorOption().equals(ColorMode.monochrome)) {
            buffImage = DitheringUtils.SierraLiteDithering(buffImage);

            buffImage = tiffFileHandler.convertAndResize(buffImage,
                                                         BufferedImage.TYPE_BYTE_BINARY,
                                                         null);
        } else {
            buffImage = tiffFileHandler.applyColorFilter(buffImage, options.getColorOption());
        }

        ImageIO.write(buffImage, FileType.tiff, new File(destFile));

        buffImage.flush();
        buffImage = null;
        ret.setSourceFile(sourceFile);
        ret.setDestFile(destFile);
        ret.setPageCount(1);

        Date dateFinish = new Date();
        logger.info("{} Image conversion completed in {} milliseconds",
                    dateFormat.format(dateFinish),
                    (dateFinish.getTime() - dateStart.getTime()));
        return ret;

    }

    /**
     * Convert DOC file to TIFF
     *
     * @param sourceFile DOC file Name
     * @param options    <a href="#{@link}">{@link FileOptions}
     * @return JsonConversionResponse JSON response object containing the resulting TIFF file name
     * @throws Exception
     */
    private static JsonConversionResponse convertDocToTiff(String sourceFile,
                                                           FileOptions options) throws Exception {

        Date dateStart = new Date();
        logger.info("{} DOC to TIFF conversion started for: {}",
                    dateFormat.format(dateStart),
                    sourceFile);

        JsonConversionResponse ret      = new JsonConversionResponse();
        String                 destFile = FileHelper.replaceFileExtensionWith(sourceFile,
                                                                              FileType.tiff);
        Document               doc      = new Document(sourceFile);

        ImageSaveOptions tiffOptions = new ImageSaveOptions(SaveFormat.TIFF);
        tiffOptions.setResolution(options.getResolution());
        tiffOptions.setTiffCompression(TiffCompression.LZW);

        // Use a different algorithm for monochrome filtering to match OSIV5
        if (!options.getColorOption().equals(ColorMode.monochrome)) {
            tiffOptions.setImageColorMode(ColorMode.toImageColorMode(options.getColorOption()));
        }

        doc.save(destFile, tiffOptions);

        TiffFileHandler tiffFileHandler = new TiffFileHandler();
        if (options.getColorOption().equals(ColorMode.monochrome)) {
            String tempFile;
            tempFile = tiffFileHandler.convertTiffColor(destFile, DocumentColor.BLACK_AND_WHITE);
            rename(tempFile, destFile);
        }

        ret.setSourceFile(sourceFile);
        ret.setDestFile(destFile);
        ret.setPageCount(doc.getBuiltInDocumentProperties().getPages());

        doc.cleanup();
        doc = null;

        Date dateFinish = new Date();
        logger.info("{} DOC to TIFF Conversion completed in {} milliseconds",
                    dateFormat.format(dateFinish),
                    (dateFinish.getTime() - dateStart.getTime()));

        return ret;
    }

    /**
     * Generate a DataMatrix object and save it to a PNG file
     *
     * @param fileName         <a href="#{@link}">{@link String} path + name of the generated PNG
     *                         file
     * @param codeText         <a href="#{@link}">{@link String} text to be encoded in the
     *                         DataMatrix
     * @param resolutionString <a href="#{@link}">{@link String} resolution of the generated PNG
     *                         file
     * @return <a href="#{@link}">{@link JsonConversionResponse}
     * @throws Exception
     */
    public static JsonConversionResponse generateDataMatrix(String fileName,
                                                            String codeText,
                                                            String resolutionString) throws Exception {

        logger.info("{} DataMatrix generation started for: {}",
                    dateFormat.format(new Date()),
                    fileName);

        JsonConversionResponse ret       = new JsonConversionResponse();
        BarcodeGenerator       generator = new BarcodeGenerator(EncodeTypes.DATA_MATRIX);
        Float                  resolution;
        String                 decodedCodeText;
        try {
            resolution = Float.parseFloat(resolutionString);
        } catch (NumberFormatException | NullPointerException e) {
            resolution = (float) 400;
            logger.warn("Invalid resolution, set to default value (400): " + e.getMessage());
        }

        try {
            decodedCodeText = URLDecoder.decode(codeText, StandardCharsets.UTF_8);
        } catch (NullPointerException | IllegalArgumentException e) {
            ret.setErrorMessage("Generate DataMatrix error: Invalid code text");
            ret.setErrorStatus(true);
            return ret;
        }

        generator.setCodeText(decodedCodeText);
        generator.getParameters().setResolution(resolution);
        generator.getParameters().getBarcode().getDataMatrix()
                 .setDataMatrixEncodeMode(DataMatrixEncodeMode.AUTO);
        generator.getParameters().getBarcode().getCodeTextParameters()
                 .setLocation(CodeLocation.NONE);
        generator.save(fileName);

        ret.setSourceFile(fileName);
        ret.setDestFile(fileName);

        logger.info("{} Generation completed", dateFormat.format(new Date()));
        return ret;
    }

    /**
     * Convert PDF file to TIFF
     *
     * @param sourceFile PDF file Name
     * @param options    <a href="#{@link}">{@link FileOptions}
     * @return JsonConversionResponse JSON response object containing the resulting TIFF file name
     * @throws Exception
     */
    private static JsonConversionResponse convertPdfToTiff(String sourceFile,
                                                           FileOptions options) throws Exception {

        if (System.getProperty("sun.java2d.cmm") != "Dorg.apache.pdfbox.rendering.UsePureJavaCMYKConversion=true")
            System.setProperty("sun.java2d.cmm",
                               "Dorg.apache.pdfbox.rendering.UsePureJavaCMYKConversion=true");

        JsonConversionResponse ret      = new JsonConversionResponse();
        String                 destFile = FileHelper.replaceFileExtensionWith(sourceFile,
                                                                              FileType.tiff);

        Date                     dateStart   = new Date();
        ArrayList<DocumentColor> targetColor = new ArrayList<>();
        ArrayList<BufferedImage> biArray     = new ArrayList<>();
        logger.info("{} PDF to TIFF conversion started for: {}",
                    dateFormat.format(dateStart),
                    sourceFile);

        com.aspose.pdf.Document pdfDocument          = null;
        PageCollection          pageCollection       = null;
        TiffFileHandler         oTiffFileHandler     = null;
        BufferedImage           bufferImage          = null;
        BufferedImage           bufferImageDithering = null;

        File   sourceFileToLoad = new File(sourceFile);
        String optionsColor     = options.getColorOption();

        // load Pdf with PDFbox
        try (PDDocument document = PDDocument.load(sourceFileToLoad,
                                                   MemoryUsageSetting.setupTempFileOnly())) {

            int                                   nbPageTiffDocument = document.getNumberOfPages();
            PDFRenderer                           pdfRenderer        = new PDFRenderer(document);
            org.apache.pdfbox.rendering.ImageType imgPdfBoxType      = null;
            pdfDocument    = new com.aspose.pdf.Document(sourceFile);
            pageCollection = pdfDocument.getPages();

            for (int i = 0; i < nbPageTiffDocument; i++) {

                bufferImageDithering = null;
                PDPage page = document.getPage(i);
                if (page.hasContents()) {

                    //set color options for each page
                    //The conversion depend on the output color and the color of the page
                    switch (optionsColor) {
                        case ColorMode.color:
                            switch (pageCollection.get_Item(i + 1).getColorType()) { // the pageCollection start at i = 1
                                case 0:
                                    imgPdfBoxType = org.apache.pdfbox.rendering.ImageType.RGB;
                                    targetColor.add(DocumentColor.JPEG);
                                    break;
                                case 1:
                                    imgPdfBoxType = org.apache.pdfbox.rendering.ImageType.GRAY;
                                    targetColor.add(DocumentColor.JPEG);
                                    break;
                                case 2:
                                    imgPdfBoxType = org.apache.pdfbox.rendering.ImageType.BINARY;
                                    targetColor.add(DocumentColor.BLACK_AND_WHITE);
                                    break;
                                default:
                                    imgPdfBoxType = org.apache.pdfbox.rendering.ImageType.RGB;
                                    targetColor.add(DocumentColor.JPEG);
                                    break;
                            }
                            break;
                        case ColorMode.monochrome:
                            switch (pageCollection.get_Item(i + 1).getColorType()) {
                                case 0:
                                    imgPdfBoxType = org.apache.pdfbox.rendering.ImageType.RGB;
                                    targetColor.add(DocumentColor.BLACK_AND_WHITE);
                                    bufferImage = pdfRenderer.renderImageWithDPI(i,
                                                                                 300,
                                                                                 imgPdfBoxType);
                                    bufferImageDithering = DitheringUtils.SierraLiteDithering(bufferImage);
                                    break;
                                case 1:
                                    imgPdfBoxType = org.apache.pdfbox.rendering.ImageType.GRAY;
                                    targetColor.add(DocumentColor.BLACK_AND_WHITE);
                                    bufferImage = pdfRenderer.renderImageWithDPI(i,
                                                                                 300,
                                                                                 imgPdfBoxType);
                                    bufferImageDithering = DitheringUtils.SierraLiteDithering(bufferImage);
                                    break;
                                case 2:
                                    imgPdfBoxType = org.apache.pdfbox.rendering.ImageType.BINARY;
                                    targetColor.add(DocumentColor.BLACK_AND_WHITE);
                                    break;
                                default:
                                    imgPdfBoxType = org.apache.pdfbox.rendering.ImageType.RGB;
                                    targetColor.add(DocumentColor.BLACK_AND_WHITE);
                                    bufferImage = pdfRenderer.renderImageWithDPI(i,
                                                                                 300,
                                                                                 imgPdfBoxType);
                                    bufferImageDithering = DitheringUtils.SierraLiteDithering(bufferImage);
                                    break;
                            }
                            break;
                        case ColorMode.greyscale:
                            switch (pageCollection.get_Item(i + 1).getColorType()) {
                                case 0:
                                case 1:
                                    imgPdfBoxType = org.apache.pdfbox.rendering.ImageType.GRAY;
                                    targetColor.add(DocumentColor.JPEG);
                                    break;
                                case 2:
                                    imgPdfBoxType = org.apache.pdfbox.rendering.ImageType.BINARY;
                                    targetColor.add(DocumentColor.BLACK_AND_WHITE);
                                    break;
                                default:
                                    imgPdfBoxType = org.apache.pdfbox.rendering.ImageType.GRAY;
                                    targetColor.add(DocumentColor.JPEG);
                                    break;
                            }
                            break;
                    }

                    if (bufferImageDithering != null)
                        biArray.add(bufferImageDithering);
                    else {
                        bufferImage = pdfRenderer.renderImageWithDPI(i, 300, imgPdfBoxType);
                        biArray.add(bufferImage);
                    }

                    bufferImage.flush();
                }
            }

            oTiffFileHandler = new TiffFileHandler();
            oTiffFileHandler.saveTiff(biArray, destFile, null, targetColor);

            ret.setPageCount(nbPageTiffDocument);
            ret.setSourceFile(sourceFile);
            ret.setDestFile(destFile);
        } finally {

            bufferImage          = null;
            bufferImageDithering = null;

            pageCollection.clear();
            pdfDocument.close();
            oTiffFileHandler = null;

            if (biArray != null) {
                biArray.clear();
                biArray = null;
            }

            if (targetColor != null) {
                targetColor.clear();
                targetColor = null;
            }
        }

        Date dateFinish = new Date();
        logger.info("{} PDF to TIFF Conversion completed in {} milliseconds",
                    dateFormat.format(dateFinish),
                    (dateFinish.getTime() - dateStart.getTime()));

        return ret;
    }

    /**
     * rename a file. If the new file already exist, it will be override
     *
     * @param oldFileName
     * @param newFileName
     */
    public static void rename(String oldFileName,
                              String newFileName) {
        new File(newFileName).delete();
        File oldFile = new File(oldFileName);
        oldFile.renameTo(new File(newFileName));
    }

    /**
     * Convert DOC file to RTF
     *
     * @param sourceFile DOC file Name
     * @return JsonConversionResponse JSON response object containing the resulting RTF file name
     * @throws Exception
     */
    private static JsonConversionResponse convertDocToRtf(String sourceFile) throws Exception {

        logger.info("{} DOC to RTF conversion started for: {}",
                    dateFormat.format(new Date()),
                    sourceFile);

        JsonConversionResponse ret = new JsonConversionResponse();

        String destFile = FileHelper.replaceFileExtensionWith(sourceFile, FileType.rtf);

        Document doc = new Document(sourceFile);
        doc.save(destFile, SaveFormat.RTF);

        ret.setSourceFile(sourceFile);
        ret.setDestFile(destFile);
        ret.setPageCount(doc.getBuiltInDocumentProperties().getPages());

        logger.info("{} DOC to RTF Conversion completed", dateFormat.format(new Date()));

        return ret;
    }

    /**
     * Convert DOC file to HTML
     *
     * @param sourceFile DOC file Name
     * @return JsonConversionResponse JSON response object containing the resulting HTML file name
     * @throws Exception
     */
    private static JsonConversionResponse convertDocToHtml(String sourceFile) throws Exception {

        logger.info("{} DOC to HTML conversion started for: {}",
                    dateFormat.format(new Date()),
                    sourceFile);

        JsonConversionResponse ret = new JsonConversionResponse();

        String destFile = FileHelper.replaceFileExtensionWith(sourceFile, FileType.html);

        Document doc = new Document(sourceFile);

        HtmlSaveOptions opts = new HtmlSaveOptions(SaveFormat.HTML);
        opts.setCssStyleSheetType(CssStyleSheetType.EMBEDDED);
        opts.setPrettyFormat(true);

        doc.save(destFile, opts);

        ret.setSourceFile(sourceFile);
        ret.setDestFile(destFile);

        logger.info("{} DOC to HTML Conversion completed", dateFormat.format(new Date()));

        return ret;
    }

    private static String deleteFile(File fileToDelete) {
        try {
            logger.info("Try to delete the logfile");
            if (fileToDelete.delete()) {
                logger.info(fileToDelete.getName() + " is deleted!");
            } else {
                logger.info("Delete operation is failed.");
            }
            return "";
        } catch (Exception e) {

            logger.info(ERROR_DELETE + e.getLocalizedMessage());
            return ERROR_DELETE + e.getLocalizedMessage();
        }
    }

    /**
     * Convert MSG file to TIFF
     *
     * @param sourceFile MSG file Name
     * @param options    <a href="#{@link}">{@link FileOptions}
     * @return JsonConversionResponse JSON response object containing the resulting TIFF file name
     * @throws Exception
     */
    private static JsonConversionResponse convertMsgtoTiff(String sourceFile,
                                                           FileOptions options) throws Exception {

        Date dateStart = new Date();
        logger.info("{} MSG to TIFF conversion started for: {}",
                    dateFormat.format(dateStart),
                    sourceFile);

        JsonConversionResponse ret = new JsonConversionResponse();

        String destFile = FileHelper.replaceFileExtensionWith(sourceFile, FileType.tiff);

        MailMessage message = MailMessage.load(sourceFile);
        message.save("HTMLTempOutput.html", SaveOptions.getDefaultHtml());

        Document htmlTempDocument = new Document("HTMLTempOutput.html");

        ImageSaveOptions tiffOptions = new ImageSaveOptions(SaveFormat.TIFF);

        htmlTempDocument.save(destFile, tiffOptions);

        //Delete temporary HTML file in case Aspose didn't remove it
        File htmlTempFile = new File("HTMLTempOutput.html");
        deleteFile(htmlTempFile);

        TiffFileHandler tiffFileHandler = new TiffFileHandler();
        if (options.getColorOption().equals(ColorMode.monochrome)) {
            String tempFile;
            tempFile = tiffFileHandler.convertTiffColor(destFile, DocumentColor.BLACK_AND_WHITE);
            rename(tempFile, destFile);
        } else {
            tiffFileHandler.editTiff(destFile, new EditTiffParameter(0, options.getColorOption()));
        }

        htmlTempDocument.cleanup();
        htmlTempDocument = null;

        ret.setSourceFile(sourceFile);
        ret.setDestFile(destFile);

        Date dateFinish = new Date();
        logger.info("{} MSG to TIFF Conversion completed in {} milliseconds",
                    dateFormat.format(dateFinish),
                    (dateFinish.getTime() - dateStart.getTime()));

        return ret;

    }

    /**
     * Convert XLSX/XLS file to TIFF
     *
     * @param sourceFile XLSX/XLS file Name
     * @param options    <a href="#{@link}">{@link FileOptions}
     * @return JsonConversionResponse JSON response object containing the resulting TIFF file name
     * @throws Exception
     */
    private static JsonConversionResponse convertXlsxtoTiff(String sourceFile,
                                                            FileOptions options) throws Exception {

        Date dateStart = new Date();
        logger.info("{} XLSX to TIFF conversion started for: {}",
                    dateFormat.format(dateStart),
                    sourceFile);

        JsonConversionResponse ret = new JsonConversionResponse();

        String destFile = FileHelper.replaceFileExtensionWith(sourceFile, FileType.tiff);

        // load the XLSX file to be rendered
        Workbook workbook = new Workbook(sourceFile);

        // Set the Image file options
        ImageOrPrintOptions tiffOptions = new ImageOrPrintOptions();

        tiffOptions.setOnePagePerSheet(false);
        tiffOptions.setImageType(ImageType.TIFF);
        tiffOptions.setTiffCompression(TiffCompression.LZW);
        tiffOptions.setDesiredSize(2480, 3508, false);
        tiffOptions.setHorizontalResolution(300);
        tiffOptions.setVerticalResolution(300);

        WorkbookRender renderer = new WorkbookRender(workbook, tiffOptions);
        renderer.toImage(destFile);

        TiffFileHandler tiffFileHandler = new TiffFileHandler();
        if (options.getColorOption().equals(ColorMode.monochrome)) {
            String tempFile;
            tempFile = tiffFileHandler.convertTiffColor(destFile, DocumentColor.BLACK_AND_WHITE);
            rename(tempFile, destFile);
        } else {
            tiffFileHandler.editTiff(destFile, new EditTiffParameter(0, options.getColorOption()));
        }

        renderer        = null;
        tiffFileHandler = null;
        tiffOptions     = null;

        ret.setSourceFile(sourceFile);
        ret.setDestFile(destFile);

        Date dateFinish = new Date();
        logger.info("{} XLSX to TIFF Conversion completed in {} milliseconds",
                    dateFormat.format(dateFinish),
                    (dateFinish.getTime() - dateStart.getTime()));

        return ret;
    }

    /**
     * @param logfileName
     * @return logFile
     */
    public static String readLogfile(String logfileName) {

        FileInputStream lofFileStream  = null;
        FileChannel     logFileChannel = null;
        int             fileSize       = 0;
        String          fileContent    = "";
        ByteBuffer      bBuff          = null;
        byte[]          tabByte        = null;
        File            logFile        = null;

        long time = System.currentTimeMillis();
        time = System.currentTimeMillis();

        // Waiting until the file in filled
        while (fileSize == 0 && System.currentTimeMillis() - time < 20000) {
            try {
                logFile       = new File(logfileName);
                lofFileStream = new FileInputStream(logFile);

                logFileChannel = lofFileStream.getChannel();

                fileSize = (int) logFileChannel.size();
                bBuff    = ByteBuffer.allocate(fileSize);
                logFileChannel.read(bBuff);
                bBuff.flip();
                tabByte     = bBuff.array();
                fileContent = new String(tabByte, "UTF-8");
            } catch (Exception e) {}
        }

        if (logFile != null) {
            try {
                logFileChannel.close();
                lofFileStream.close();
            } catch (Exception e) {}

            fileContent = fileContent + deleteFile(logFile);
        }

        lofFileStream  = null;
        logFileChannel = null;
        fileContent    = null;
        logFile        = null;
        bBuff          = null;
        tabByte        = null;

        return fileContent;

    }

    /**
     * Retrieve information about the file, including the number of pages
     *
     * @param sourceFile Path to the file <a HREF="#{@link}">{@link String}
     * @return JsonConversionResponse
     * @throws Exception
     */
    public static JsonConversionResponse getFileInformation(String sourceFile) throws Exception {

        JsonConversionResponse ret = new JsonConversionResponse();

        try {
            sourceFile = URLDecoder.decode(sourceFile, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            ret.setErrorStatus(true);
            ret.setSourceFile(sourceFile);
            ret.setErrorMessage(Constant.ERROR_DECODING + e1.getMessage());
            logger.info(Constant.ERROR_DECODING + e1.getMessage());
            return ret;
        }
        String sourceFileType = FilenameUtils.getExtension(sourceFile);

        try {
            if (sourceFile == null || sourceFile.isEmpty()) {
                throw new RuntimeException(SOURCE_FILE_EMPTY_ERROR);
            }

            if (!FileHelper.fileExist(sourceFile)) {
                throw new RuntimeException(String.format(Constant.SOURCE_NOT_FOUND, sourceFile));
            }

            switch (sourceFileType) {
                case FileType.doc:
                case FileType.docx:
                case FileType.html:
                case FileType.rtf:
                    return getDocumentFileInformation(sourceFile);

                case FileType.pdf:
                    return getPDFFileInformation(sourceFile);

                case FileType.tiff:
                case FileType.tif:
                    return getTiffFileInformation(sourceFile);

                case FileType.xlsx:
                case FileType.xls:
                    return getExcelFileInformation(sourceFile);

                default:
                    throw new RuntimeException(String.format(EXTENSION_NOT_SUPPORTED,
                                                             sourceFileType));
            }
        } catch (Exception e) {
            ret.setErrorStatus(true);
            ret.setSourceFile(sourceFile);
            ret.setErrorMessage(ERROR + e.getMessage());
            logger.info(ERROR + e.getMessage());
            return ret;
        }
    }

    /**
     * Retrieve information about the DOC file, including the number of pages
     *
     * @param sourceFile Path to the DOC file <a HREF="#{@link}">{@link String}
     * @return JsonConversionResponse
     * @throws Exception
     */
    private static JsonConversionResponse getDocumentFileInformation(String sourceFile) throws Exception {

        JsonConversionResponse ret = new JsonConversionResponse();

        Document doc = new Document(sourceFile);

        ret.setSourceFile(sourceFile);
        ret.setPageCount(doc.getPageCount());

        doc = null;

        return ret;
    }

    /**
     * Retrieve information about the XLSX file, including the number of pages
     *
     * @param sourceFile Path to the XLSX file <a HREF="#{@link}">{@link String}
     * @return JsonConversionResponse
     * @throws Exception
     */
    private static JsonConversionResponse getExcelFileInformation(String sourceFile) throws Exception {

        JsonConversionResponse ret = new JsonConversionResponse();

        Workbook doc = new Workbook(sourceFile);

        ret.setSourceFile(sourceFile);
        ret.setPageCount(doc.getWorksheets().getCount());

        doc = null;

        return ret;

    }

    /**
     * Retrieve information about the TIFF file, including the number of pages
     *
     * @param sourceFile Path to the TIFF file <a HREF="#{@link}">{@link String}
     * @return JsonConversionResponse
     * @throws Exception
     */
    private static JsonConversionResponse getTiffFileInformation(String sourceFile) throws Exception {
        try (TiffImage tiffImage = (TiffImage) Image.load(sourceFile)) {

            JsonConversionResponse ret = new JsonConversionResponse();

            ret.setSourceFile(sourceFile);
            ret.setPageCount(tiffImage.getPageCount());
            return ret;

        }
    }

    /**
     * Retrieve information about the PDF file, including the number of pages
     *
     * @param sourceFile Path to the PDF file <a HREF="#{@link}">{@link String}
     * @return JsonConversionResponse
     * @throws Exception
     */
    private static JsonConversionResponse getPDFFileInformation(String sourceFile) throws Exception {

        JsonConversionResponse ret = new JsonConversionResponse();

        PDDocument pdfFile = PDDocument.load(new File(sourceFile),
                                             MemoryUsageSetting.setupTempFileOnly());

        ret.setSourceFile(sourceFile);
        ret.setPageCount(pdfFile.getNumberOfPages());

        pdfFile.close();

        pdfFile = null;

        return ret;
    }

    /**
     * Archives a file into a zip and places it in the same folder as the source file
     *
     * @param fileName fileToZip <a href="#{@link}">{@link File}
     * @param password
     * @return JsonConversionResponse
     * @throws ZipException
     * @throws UnsupportedEncodingException
     */
    public static JsonConversionResponse zipFile(String fileName,
                                                 String password) throws ZipException, UnsupportedEncodingException {

        fileName = fileName.replace("\\", "/").replace("\n", "");
        fileName = URLDecoder.decode(fileName, "UTF-8");

        File fileToZip = new File(fileName);

        logger.info("{} Zipping File started for: {}", dateFormat.format(new Date()), fileName);

        JsonConversionResponse response = new JsonConversionResponse();

        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);

        String zipName = FileHelper.replaceFileExtensionWith(fileToZip.getAbsolutePath(),
                                                             FileType.zip);
        System.out.println(fileToZip.getAbsolutePath());

        new ZipFile(zipName, password.toCharArray()).addFile(fileToZip.getAbsolutePath(),
                                                             zipParameters);

        response.setSourceFile(fileToZip.getAbsolutePath());
        response.setDestFile(zipName);

        logger.info("{} Zip File creation completed", dateFormat.format(new Date()));

        return response;

    }

    /**
     * Archives a file into a zip and places it in the same folder as the source file
     *
     * @param fileName fileToZip <a href="#{@link}">{@link File}
     * @return JsonConversionResponse
     * @throws ZipException
     * @throws UnsupportedEncodingException
     */
    public static JsonConversionResponse zipFile(String fileName) throws ZipException, UnsupportedEncodingException {

        fileName = fileName.replace("\\", "/").replace("\n", "");
        fileName = URLDecoder.decode(fileName, "UTF-8");

        File fileToZip = new File(fileName);

        logger.info("{} Zipping File started for: {}", dateFormat.format(new Date()), fileName);

        JsonConversionResponse response = new JsonConversionResponse();

        String zipName = FileHelper.replaceFileExtensionWith(fileToZip.getAbsolutePath(),
                                                             FileType.zip);

        new ZipFile(zipName).addFile(fileToZip.getAbsolutePath());

        response.setSourceFile(fileToZip.getAbsolutePath());
        response.setDestFile(zipName);

        fileToZip = null;

        logger.info("{} Zip File creation completed", dateFormat.format(new Date()));

        return response;
    }

    /**
     * Archives a folder into a zip and places it in the same folder as the source folder
     * and encrypts it with a given password
     *
     * @param folderName folderToZip <a href="#{@link}">{@link File}
     * @param password   <code>String</code>
     * @return JsonConversionResponse
     * @throws FileNotFoundException
     * @throws ZipException
     * @throws UnsupportedEncodingException
     */
    public static JsonConversionResponse zipFolder(String folderName,
                                                   String password) throws FileNotFoundException, ZipException, UnsupportedEncodingException {

        folderName = folderName.replace("\\", "/").replace("\n", "");
        folderName = URLDecoder.decode(folderName, "UTF-8");

        logger.info("{} Password Protected Zipping Folder started for: {}",
                    dateFormat.format(new Date()),
                    folderName);
        File folderToZip = new File(folderName);

        JsonConversionResponse response = new JsonConversionResponse();

        if (folderToZip.isHidden()) {
            logger.info(SOURCE_FILE_HIDDEN);
            response.setErrorMessage(SOURCE_FILE_HIDDEN);
            response.setErrorStatus(true);
            return response;
        }

        if (!folderToZip.isDirectory()) {
            logger.info(String.format(SOURCE_NOT_FOLDER, folderName));
            response.setErrorStatus(true);
            response.setErrorMessage(String.format(SOURCE_NOT_FOLDER, folderName));
            return response;

        }

        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);

        String zipName = folderToZip.getAbsolutePath() + "." + FileType.zip;

        ZipFile zipFile = new ZipFile(zipName, password.toCharArray());
        zipFile.addFolder(new File(folderToZip.getAbsolutePath()), zipParameters);

        response.setSourceFile(folderToZip.getAbsolutePath());
        response.setDestFile(zipName);

        zipParameters = null;

        logger.info("{} Password Protected Zip Folder creation completed",
                    dateFormat.format(new Date()));

        return response;
    }

    /**
     * Archives a folder into a zip and places it in the same folder as the source folder
     *
     * @param folderName folderToZip <a href="#{@link}">{@link File}
     * @return JsonConversionResponse
     * @throws FileNotFoundException
     * @throws ZipException
     * @throws UnsupportedEncodingException
     */
    public static JsonConversionResponse zipFolder(String folderName) throws FileNotFoundException, ZipException, UnsupportedEncodingException {

        folderName = folderName.replace("\\", "/").replace("\n", "");
        folderName = URLDecoder.decode(folderName, "UTF-8");

        logger.info("{} Zipping Folder Started for: {}", dateFormat.format(new Date()), folderName);

        File folderToZip = new File(folderName);

        JsonConversionResponse response = new JsonConversionResponse();

        if (folderToZip.isHidden()) {
            logger.info(SOURCE_FILE_HIDDEN);
            response.setErrorMessage(SOURCE_FILE_HIDDEN);
            response.setErrorStatus(true);
            return response;
        }

        if (!folderToZip.isDirectory()) {
            logger.info(String.format(SOURCE_NOT_FOLDER, folderName));
            response.setErrorMessage(String.format(SOURCE_NOT_FOLDER, folderName));
            response.setErrorStatus(true);
            return response;

        }

        String zipName = folderToZip.getAbsolutePath() + "." + FileType.zip;

        new ZipFile(zipName).addFolder(new File(folderToZip.getAbsolutePath()));

        response.setSourceFile(folderToZip.getAbsolutePath());
        response.setDestFile(zipName);

        folderToZip = null;

        logger.info("{} Zip Folder creation completed", dateFormat.format(new Date()));

        return response;
    }

    /**
     * Check if the file is an image, by verifying the file extension
     *
     * @param filename the file name + extension
     * @return true if image, false otherwise
     */
    private static boolean isImage(String filename) {

        String sourceFileType = FileHelper.getFileExtension(filename, true);

        switch (sourceFileType) {
            case FileType.tiff:
            case FileType.png:
            case FileType.jpg:
            case FileType.tif:
            case FileType.jpeg:
                return true;
            default:
                return false;
        }

    }

    /**
     * Check if the file is a doc, by verifying the file extension
     *
     * @param filename the file name + extension
     * @return true if doc, false otherwise
     */
    private static boolean isDoc(String filename) {

        String sourceFileType = FileHelper.getFileExtension(filename, true);

        switch (sourceFileType) {
            case FileType.doc:
            case FileType.docx:
            case FileType.docm:
            case FileType.odt:
                return true;
            default:
                return false;
        }

    }

}
