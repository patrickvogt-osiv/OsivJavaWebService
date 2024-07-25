package ch.osiv.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.aspose.pdf.Border;
import com.aspose.pdf.DefaultAppearance;
import com.aspose.pdf.FreeTextAnnotation;
import com.aspose.pdf.LineEnding;
import com.aspose.pdf.Page;
import com.aspose.pdf.Point;
import com.aspose.pdf.Rectangle;
import com.aspose.pdf.Rotation;

import ch.osiv.Constant;
import ch.osiv.document.DocumentInfo;
import ch.osiv.document.LineAnnotation;
import ch.osiv.document.PageInfo;
import ch.osiv.document.PaginationAnnotation;
import ch.osiv.document.RubberStampAnnotation;
import ch.osiv.document.TiffFileHandler;
import net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel;
import net.sourceforge.tess4j.ITesseract.RenderedFormat;
import net.sourceforge.tess4j.OCRResult;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;

/**
 * The DocumentHandler class provides static methods for downloading files using the Windream
 * Service
 */
public class DocumentHandler {

    /**
     * addFreeTextAnnotationToPdf
     *
     * @param page
     * @param pageInfo
     * @param rubberStampAnnotation
     * @param flatten
     * @param pageOrientation
     * @return Page
     */
    private static Page addFreeTextAnnotationToPdf(Page page,
                                                   PageInfo pageInfo,
                                                   RubberStampAnnotation rubberStampAnnotation,
                                                   Boolean flatten,
                                                   String pageOrientation) {
        Double coefX  = page.getPageInfo().getHeight() / pageInfo.getPageHeight();
        Double coefY  = page.getPageInfo().getWidth() / pageInfo.getPageWidth();
        Double startX = (rubberStampAnnotation.getStartX()) * coefX;
        Double startY = page.getPageInfo().getHeight() -
                        (rubberStampAnnotation.getStartY()) * coefY;
        Double endX   = startX +
                        (rubberStampAnnotation.getFontSize() + rubberStampAnnotation.getEndX()) *
                                 coefX;
        Double endY   = page.getPageInfo().getHeight() -
                        (rubberStampAnnotation.getStartY() + rubberStampAnnotation.getFontSize() +
                         rubberStampAnnotation.getEndY()) * coefY;

        DefaultAppearance defaultAppearance = new DefaultAppearance();
        defaultAppearance.setFontName(rubberStampAnnotation.getFontName());
        defaultAppearance.setFontSize(rubberStampAnnotation.getFontSize());
        if (rubberStampAnnotation.getFontColor().equals("000000")) {
            defaultAppearance.setTextColor(java.awt.Color.black);
        }

        else {
            defaultAppearance.setTextColor(java.awt.Color.RED);
        }
        Rectangle rectangle;
        if (pageOrientation != null && pageOrientation.equals("landscape")) {
            rectangle = new Rectangle(page.getPageInfo().getWidth() + startY -
                                      page.getPageInfo().getHeight(),
                                      page.getPageInfo().getHeight() - startX,
                                      page.getPageInfo().getWidth() + endY -
                                                                               page.getPageInfo()
                                                                                   .getHeight(),
                                      page.getPageInfo()
                                          .getHeight() - endX - rubberStampAnnotation.getFontSize());
        } else {
            rectangle = new Rectangle(startX, startY, endX, endY);
        }

        FreeTextAnnotation freeTextAnnotation = new FreeTextAnnotation(page,
                                                                       rectangle,
                                                                       defaultAppearance);
        freeTextAnnotation.setRichText(rubberStampAnnotation.getTextString());

        if (pageOrientation != null && pageOrientation.equals("landscape")) {
            freeTextAnnotation.setRotate(Rotation.on270);
        }

        if (!rubberStampAnnotation.getFontColor().equals("000000")) {
            Border oBorder = new Border(freeTextAnnotation);
            oBorder.setStyle(4);
            freeTextAnnotation.setBorder(oBorder);

        }
        if (flatten) {
            freeTextAnnotation.flatten();
        }

        page.getAnnotations().add(freeTextAnnotation);

        return page;
    }

    /**
     * addAnnotationToPdf
     *
     * @param pdfFilename
     * @param annotationFilename
     * @param flatten
     * @param allPageOrientation
     * @return String
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws XPathExpressionException
     */
    public static String addAnnotationToPdf(String pdfFilename,
                                            String annotationFilename,
                                            Boolean flatten,
                                            ArrayList<String> allPageOrientation) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        com.aspose.pdf.Document document = null;
        try {
            document = new com.aspose.pdf.Document(pdfFilename);
            DocumentInfo annotationDocument = new DocumentInfo(annotationFilename);

            for (PageInfo PageInfo : annotationDocument.getPageInfoList()) {
                for (PaginationAnnotation paginationAnnotation : PageInfo.getPaginationAnnotationList()) {
                    addFreeTextAnnotationToPdf(document.getPages()
                                                       .get_Item(PageInfo.getPageNumber() + 1),
                                               PageInfo,
                                               paginationAnnotation,
                                               flatten,
                                               null);
                }
            }

            for (PageInfo pageInfo : annotationDocument.getPageInfoList()) {

                for (LineAnnotation lineAnnotation : pageInfo.getLineAnnotationList()) {
                    addLineAnnotationToPdf(document.getPages()
                                                   .get_Item(pageInfo.getPageNumber() + 1),
                                           pageInfo,
                                           lineAnnotation,
                                           flatten,
                                           allPageOrientation.get(pageInfo.getPageNumber()));
                }
                for (RubberStampAnnotation rubberStampAnnotation : pageInfo.getRubberStampAnnotationList()) {
                    addFreeTextAnnotationToPdf(document.getPages()
                                                       .get_Item(pageInfo.getPageNumber() + 1),
                                               pageInfo,
                                               rubberStampAnnotation,
                                               flatten,
                                               allPageOrientation.get(pageInfo.getPageNumber()));
                }

            }
            document.save(pdfFilename);
            return pdfFilename;
        } finally {
            document.close();
        }
    }

    /**
     * addLineAnnotationToPdf
     *
     * @param page
     * @param pageInfo
     * @param pLineAnnotation
     * @param flatten
     * @param pageOrientation
     * @return Page
     */
    private static Page addLineAnnotationToPdf(Page page,
                                               PageInfo pageInfo,
                                               LineAnnotation pLineAnnotation,
                                               Boolean flatten,
                                               String pageOrientation) {

        Double coefX  = page.getPageInfo().getHeight() / pageInfo.getPageHeight();
        Double coefY  = page.getPageInfo().getWidth() / pageInfo.getPageWidth();
        Double startX = pLineAnnotation.getStartX() * coefX;
        Double startY = page.getPageInfo().getHeight() - pLineAnnotation.getStartY() * coefY;
        Double endX   = pLineAnnotation.getEndX() * coefX;
        Double endY   = page.getPageInfo().getHeight() - pLineAnnotation.getEndY() * coefY;

        Point pointStart;
        Point pointEnd;
        if (pageOrientation != null && pageOrientation.equals("landscape")) {
            pointStart = new Point(page.getPageInfo().getWidth() + startY -
                                   page.getPageInfo().getHeight(),
                                   page.getPageInfo().getHeight() - startX);
            pointEnd   = new Point(page.getPageInfo().getWidth() + endY -
                                   page.getPageInfo().getHeight(),
                                   page.getPageInfo().getHeight() - endX);
        } else {
            pointStart = new Point(startX, startY);
            pointEnd   = new Point(endX, endY);
        }

        com.aspose.pdf.LineAnnotation lineAnnotation = new com.aspose.pdf.LineAnnotation(page,
                                                                                         page.getMediaBox(),
                                                                                         pointStart,
                                                                                         pointEnd);

        lineAnnotation.setColor(com.aspose.pdf.Color.getRed());
        lineAnnotation.setWidth(pLineAnnotation.getLineSize());
        lineAnnotation.setStartingStyle(LineEnding.None);
        lineAnnotation.setEndingStyle(LineEnding.None);
        if (flatten) {
            lineAnnotation.flatten();
        }

        return page;

    }

    /**
     * Generate a OCR file from a TIF
     * 
     * @param fullPathName the full path of the OCR file
     * @throws TesseractException
     * @throws IOException
     */
    public static void generateOCRFile(String fullPathName) throws TesseractException, IOException {

        TiffFileHandler tiffHandler = new TiffFileHandler();
        fullPathName = tiffHandler.changeTiffCompression(fullPathName);

        Tesseract tesseract = new Tesseract();

        try {
            tesseract.setDatapath(new File(DocumentHandler.class.getClassLoader()
                                                                .getResource("./ch/osiv/OCR/eng.traineddata")
                                                                .getFile()).getParent());
        } catch (Exception e) {
            tesseract.setDatapath(".\\src\\ch\\osiv\\OCR\\");
        }

        tesseract.setLanguage(Constant.ENG);
        List<RenderedFormat> formats = new ArrayList<RenderedFormat>(Arrays.asList(RenderedFormat.HOCR));

        OCRResult oOCRResult = tesseract.createDocumentsWithResults(fullPathName,
                                                                    fullPathName,
                                                                    formats,
                                                                    TessPageIteratorLevel.RIL_WORD);

        try (FileWriter fstream = new FileWriter(fullPathName + ".ocr.txt");
                BufferedWriter ocrText = new BufferedWriter(fstream)) {
            Double yPos = 0.0;
            if (oOCRResult.getWords().size() > 0) {
                yPos = oOCRResult.getWords().get(0).getBoundingBox().getY();
            }
            for (int i = 0; i < oOCRResult.getWords().size(); i++) {
                Word words = oOCRResult.getWords().get(i);
                if (words.getBoundingBox().height == 0 && words.getBoundingBox().width == 0 &&
                    words.getBoundingBox().x == 0 &&
                    words.getBoundingBox().y == 0) {
                    continue;
                }
                if (yPos >= (words.getBoundingBox().getY() - words.getBoundingBox().getHeight())) {
                    if (i != 0 && i != oOCRResult.getWords().size()) {
                        ocrText.append(" ");
                    }
                } else {
                    ocrText.append(String.format("\n"));
                }
                yPos = words.getBoundingBox().getY();
                ocrText.append(words.getText());
            }

            ocrText.write(String.format("\n "));
            ocrText.flush();
            ocrText.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
