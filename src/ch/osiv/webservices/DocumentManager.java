package ch.osiv.webservices;

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;

import javax.imageio.ImageReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.aspose.imaging.fileformats.tiff.enums.TiffExpectedFormat;

import ch.osiv.Constant;
import ch.osiv.document.DocumentExporter;
import ch.osiv.document.EditTiffParameter;
import ch.osiv.document.ExportDocumentsRequest;
import ch.osiv.document.ExportDocumentsResponse;
import ch.osiv.document.RubberStampAnnotation;
import ch.osiv.document.TiffFileHandler;
import ch.osiv.document.TiffInfoResponse;
import ch.osiv.document.TiffMerger;
import ch.osiv.document.convert.DocumentColor;
import ch.osiv.document.tiff.parameter.CoordinateParameter;
import ch.osiv.helper.DocumentHandler;
import ch.osiv.helper.FileConverterHelper;
import ch.osiv.helper.FileHelper;
import ch.osiv.helper.FileOptions;
import ch.osiv.helper.FileType;
import ch.osiv.helper.HocrToJsonConverterHelper;
import ch.osiv.helper.JsonConversionResponse;
import ch.osiv.helper.JsonPrintJobResponse;
import ch.osiv.helper.LicenseHelper;
import ch.osiv.helper.OcrResponse;
import ch.osiv.helper.OsivPrintJob;
import ch.osiv.helper.PrinterHelper;
import ch.osiv.io.FileCreationDateComperator;
import net.sourceforge.tess4j.TesseractException;

@Path("DocumentService")
public class DocumentManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentManager.class);

    private static final String              ERROR_MERGE_PDF    = "Error merging PDF files: ";
    private static final String              ERROR_APPEND_FILES = "Error appending files: ";
    private static final String              ERROR_MERGE_TIFF   = "Error merging Tiff files: ";
    private static Hashtable<String, String> knownPrinterNames  = new Hashtable<String, String>();
    private static int                       refreshCounter     = 0;
    private static Date                      lastAccess         = new Date();

    static {
        try {
            LicenseHelper.LoadAsposeLicences();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getAvailablePrintersFromCacheObject(String printerName) throws Exception {
        refreshCounter++;

        if (refreshCounter > 99 || (((new Date().getTime() - lastAccess.getTime()) / 1000) > 300)) {
            LOGGER.info("Available printer cache is too old. Will create a new one.");
            knownPrinterNames = new Hashtable<String, String>();
            lastAccess        = new Date();
            refreshCounter    = 0;
        }

        if (knownPrinterNames.containsKey(printerName)) {
            LOGGER.info("Reading available printers from cache.");
            return knownPrinterNames.get(printerName);
        } else {
            knownPrinterNames.put(printerName, PrinterHelper.GetAvailablePrinters(printerName));
            return knownPrinterNames.get(printerName);
        }
    }

    // Web Services definitions
    @GET
    @Path("GetAvailablePrinters")
    @Produces(MediaType.APPLICATION_JSON)
    public String GetAvailablePrinters(// @formatter:off
                                       @QueryParam("printerName") String printerName) throws Exception {
                                       // @formatter:on

        return this.getAvailablePrintersFromCacheObject((printerName == null) ? ""
                                                                              : printerName);
    }

    @GET
    @Path("appendDocument")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse appendDocument(// @formatter:off
                                                 @QueryParam("firstDocumentName")  String firstDocumentName,
                                                 @QueryParam("secondDocumentName") String secondDocumentName) throws Exception {
                                                 // @formatter:on

        try {
            return FileConverterHelper.appendDocument(firstDocumentName, secondDocumentName);
        } catch (Exception e) {
            JsonConversionResponse response = new JsonConversionResponse();
            response.setErrorStatus(true);
            response.setErrorMessage(ERROR_APPEND_FILES + e.getMessage());
            return response;
        }
    }

    @GET
    @Path("editTiff")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse editTiff(// @formatter:off
                                              @QueryParam("sourceFile")  String imagePath,
                                              @QueryParam("colorOption") String colorOption,
                                              @QueryParam("borderSize") Integer borderSize) throws Exception {
                                              // @formatter:on

        JsonConversionResponse response = new JsonConversionResponse();
        try {
            EditTiffParameter editParameter   = new EditTiffParameter(borderSize, colorOption);
            TiffFileHandler   tiffFileHandler = new TiffFileHandler();
            tiffFileHandler.editTiff(imagePath, editParameter);
            response.setDestFile(imagePath);
            response.setErrorStatus(false);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            response.setErrorStatus(true);
            response.setErrorMessage(e.getMessage());
            return response;
        }
    }

    @GET
    @Path("convertToTiff")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse convertToTiff(// @formatter:off
                                                @QueryParam("sourceFile")  String sourceFile,
                                                @QueryParam("colorOption") String colorOption) throws Exception {
                                                // @formatter:on

        FileOptions options = new FileOptions();
        options.setColorOption(colorOption);

        return FileConverterHelper.convertFileTo(sourceFile, FileType.tiff, options);
    }

    @GET
    @Path("convertToPdf")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse convertToPdf(// @formatter:off
                                               @QueryParam("sourceFile") String sourceFile) throws Exception {
                                               // @formatter:on

        return FileConverterHelper.convertFileTo(sourceFile, FileType.pdf);
    }

    @GET
    @Path("generateDataMatrix")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse generateDataMatrix(// @formatter:off
                                                     @QueryParam("fileName")   String fileName,
                                                     @QueryParam("code")       String codeString,
                                                     @QueryParam("resolution") String resolutionString) throws Exception {
                                                     //@formatter:on

        try {
            return FileConverterHelper.generateDataMatrix(fileName, codeString, resolutionString);
        } catch (Exception e) {
            JsonConversionResponse response = new JsonConversionResponse();
            response.setErrorStatus(true);
            response.setErrorMessage(e.getMessage());
            return response;
        }
    }

    @GET
    @Path("getFileInformation")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse getFileInformation(// @formatter:off
                                                     @QueryParam("fileName") String fileName) throws Exception {
                                                     // @formatter:on

        return FileConverterHelper.getFileInformation(fileName);
    }

    @GET
    @Path("htmlToRtf")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse htmlToRtf(// @formatter:off
                                            @QueryParam("sourceFile") String sourceFile) throws Exception {
                                            // @formatter:on

        return FileConverterHelper.convertFileTo(sourceFile, FileType.rtf);
    }

    @GET
    @Path("rtfToHtml")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse rtfToHtml(// @formatter:off
                                            @QueryParam("sourceFile") String sourceFile) throws Exception {
                                            // @formatter:on

        return FileConverterHelper.convertFileTo(sourceFile, FileType.html);
    }

    @GET
    @Path("pdfPrint")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonPrintJobResponse pdfPrint(// @formatter:off
                                         @QueryParam("fileName")        String fileName,
                                         @QueryParam("printerName")     String printerName,
                                         @QueryParam("landscape")       boolean landscape,
                                         @QueryParam("iNumberOfCopies") short iNumberOfCopies,
                                         @QueryParam("printJobName")    String printJobName,
                                         @QueryParam("printWithAspose") Boolean printWithAspose) throws Exception {
                                         // @formatter:on

        LOGGER.info("FileName         : " + fileName);
        LOGGER.info("Printer Name     : " + printerName);
        LOGGER.info("Landscape        : " + landscape);
        LOGGER.info("iNumberOfCopies  : " + iNumberOfCopies);
        LOGGER.info("printJobName     : " + printJobName);

        OsivPrintJob osivPrintJob = new OsivPrintJob();
        osivPrintJob.setFileName(fileName);
        osivPrintJob.setPrinterName(printerName);
        osivPrintJob.setLandscape(landscape);
        osivPrintJob.setNumberOfCopies(iNumberOfCopies);
        osivPrintJob.setPrintJobName(printJobName);
        osivPrintJob.setPrintWithAspose(printWithAspose);

        return PrinterHelper.pdfPrint(osivPrintJob);
    }

    @GET
    @Path("mergePdfFiles")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse mergePdfFiles(// @formatter:off
                                                @QueryParam("folderName") String folderName) {
                                                // @formatter:on

        try {
            return FileConverterHelper.mergePdfFilesIntoOne(folderName,
                                                            new FileCreationDateComperator());
        } catch (Exception e) {
            JsonConversionResponse response = new JsonConversionResponse();
            response.setErrorStatus(true);
            response.setErrorMessage(ERROR_MERGE_PDF + e.getMessage());
            e.printStackTrace();
            return response;
        }
    }

    @GET
    @Path("mergeTiffFiles")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse mergeTiffFiles(// @formatter:off
                                                 @QueryParam("folderName")     String folderName,
                                                 @QueryParam("resultFileName") String resultFileName) {
                                                 // @formatter:on

        TiffMerger merger = null;

        try {
            merger = new TiffMerger();
            return merger.mergeTiffFiles(folderName, resultFileName, TiffExpectedFormat.TiffLzwRgb);
        } catch (Exception e) {
            JsonConversionResponse response = new JsonConversionResponse();
            response.setErrorStatus(true);
            response.setErrorMessage(ERROR_MERGE_TIFF + e.getMessage());
            e.printStackTrace();
            return response;
        } finally {
            merger = null;
            System.gc();
        }
    }

    @GET
    @Path("addImage")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse appendImage(// @formatter:off
                                              @QueryParam("documentName")  String documentName,
                                              @QueryParam("imageDocument") String imageDocument) {
                                              // @formatter:on

        try {
            return FileConverterHelper.appendImage(documentName, imageDocument);
        } catch (Exception e) {
            JsonConversionResponse response = new JsonConversionResponse();
            response.setErrorStatus(true);
            response.setErrorMessage(ERROR_APPEND_FILES + e.getMessage());
            e.printStackTrace();
            return response;
        }
    }

    @GET
    @Path("zipFolder")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse zipFolder(// @formatter:off
                                            @QueryParam("folderName") String folderName,
                                            @QueryParam("password")   String password) {
                                            // @formatter:on

        try {
            if (password != null) {
                return FileConverterHelper.zipFolder(folderName, password);
            } else {
                return FileConverterHelper.zipFolder(folderName);
            }
        } catch (Exception e) {
            JsonConversionResponse response = new JsonConversionResponse();
            response.setErrorStatus(true);
            response.setErrorMessage(e.getMessage());
            e.printStackTrace();
            return response;
        }
    }

    @GET
    @Path("zipFile")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse zipFile(// @formatter:off
                                          @QueryParam("fileName") String fileName,
                                          @QueryParam("password") String password) {
                                          // @formatter:on

        try {
            if (password != null) {
                return FileConverterHelper.zipFile(fileName, password);
            } else {
                return FileConverterHelper.zipFile(fileName);
            }
        } catch (Exception e) {
            JsonConversionResponse response = new JsonConversionResponse();
            response.setErrorStatus(true);
            response.setErrorMessage(e.getMessage());
            e.printStackTrace();
            return response;
        }
    }

    @GET
    @Path("BurnAnnotationInDocument")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse burnAnnotationInDocument(// @formatter:off
                                                           @QueryParam("filename")           String filename,
                                                           @QueryParam("annotationfilename") String annotationfilename,
                                                           @QueryParam("documentColor")      DocumentColor documentColor) {
                                                           // @formatter:on

        try {
            String DestFileName  = null;
            String fileExtension = FileHelper.getFileExtension(filename, true);

            switch (fileExtension) {
                case FileType.tif:
                    TiffFileHandler tiffHandler = new TiffFileHandler();
                    tiffHandler.addAnnotationToTiff(filename, annotationfilename, documentColor);
                    break;
                case FileType.pdf:
                    DestFileName = DocumentHandler.addAnnotationToPdf(filename,
                                                                      annotationfilename,
                                                                      true,
                                                                      null);
                    break;
                default:
                    throw new NotSupportedException("File type '" + fileExtension +
                                                    "' is not supported!");
            }

            JsonConversionResponse JsonResponse = new JsonConversionResponse();
            JsonResponse.setSourceFile(filename);
            JsonResponse.setDestFile(DestFileName);
            return JsonResponse;
        } catch (Exception e) {
            e.printStackTrace();
            JsonConversionResponse jsonResponse = new JsonConversionResponse();
            jsonResponse.setSourceFile("Error");
            jsonResponse.setDestFile("Error");
            return jsonResponse;
        }
    }

    @GET
    @Path("createOCRandParseToJSON")
    @Produces(MediaType.APPLICATION_JSON)
    public OcrResponse createOcrAndParseToJson(// @formatter:off
                                               @QueryParam("FullPathName") String fullPathName) throws IOException, ParserConfigurationException, SAXException, TesseractException {
                                               // @formatter:on

        String path                = FileHelper.getDirPath(fullPathName);
        String fullPathFileNameOCR = path + "/" + FileHelper.getBaseName(fullPathName) + ".hocr";

        DocumentHandler.generateOCRFile(fullPathName);
        String      contentFormat = HocrToJsonConverterHelper.getContentFormatFromHocr(fullPathFileNameOCR);
        OcrResponse ocrResponse   = new OcrResponse();

        ocrResponse.setOcrFile(HocrToJsonConverterHelper.generateJsonFileFromHocr(fullPathFileNameOCR));
        ocrResponse.setOcrTextFile(fullPathName + ".ocr.txt");
        ocrResponse.setSourceFile(fullPathFileNameOCR);
        ocrResponse.setContentFormat(contentFormat);

        return ocrResponse;
    }

    @GET
    @Path("changeTiffCompression")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse changeTiffCompression(// @formatter:off
                                                        @QueryParam("FullPathName") String fullPathName) {
                                                        // @formatter:on
        try {
            JsonConversionResponse jsonResponse = new JsonConversionResponse();
            TiffFileHandler        tiffHandler  = new TiffFileHandler();
            jsonResponse.setDestFile(tiffHandler.changeTiffCompression(fullPathName));
            return jsonResponse;
        } catch (Exception e) {
            e.printStackTrace();
            JsonConversionResponse jsonResponse = new JsonConversionResponse();
            jsonResponse.setSourceFile("Error");
            jsonResponse.setDestFile("Error");
            return jsonResponse;
        }
    }

    @GET
    @Path("getTiffInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public TiffInfoResponse getTiffInfo(// @formatter:off
                                        @QueryParam("tiffPath") String tiffPath) {
                                        // @formatter:on

        try {
            TiffFileHandler  tiffHandler      = new TiffFileHandler();
            TiffInfoResponse tiffInfoResponse = tiffHandler.getTiffInfo(tiffPath);

            return tiffInfoResponse;
        } catch (Exception e) {
            e.printStackTrace();
            TiffInfoResponse tiffInfoResponse = new TiffInfoResponse();
            tiffInfoResponse.setErrorMessage(e.getMessage());
            tiffInfoResponse.setErrorStatus(true);
            return tiffInfoResponse;
        }
    }

    @GET
    @Path("splitTiff")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse splitTiff(// @formatter:off
                                            @QueryParam("filePath")      String filePath,
                                            @QueryParam("documentColor") DocumentColor documentColor) {
                                            // @formatter:on

        try {
            JsonConversionResponse jsonResponse = new JsonConversionResponse();
            TiffFileHandler        tiffHandler  = new TiffFileHandler();
            jsonResponse.setDestFile(tiffHandler.splitTiff(filePath, documentColor, null));
            return jsonResponse;
        } catch (Exception e) {
            e.printStackTrace();
            JsonConversionResponse jsonResponse = new JsonConversionResponse();
            jsonResponse.setSourceFile("Error");
            jsonResponse.setDestFile("Error");
            return jsonResponse;
        }
    }

    /**
     * Webservice method for ExportDocuments
     *
     * @param request
     */
    @POST
    @Path("ExportDocuments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response exportDocuments(ExportDocumentsRequest request) {

        ExportDocumentsResponse exportResponse;
        Response                httpResponse;

        long uuidTime = System.currentTimeMillis(); // used for duration calculation and uuid identifier
        LOGGER.info("{} Webservice request 'ExportDocuments' received ({})",
                    Constant.DATETIME_FORMAT.format(new Date()),
                    uuidTime);
        LOGGER.info(request.serialize());

        try {
            DocumentExporter exporter = new DocumentExporter(request.getKineticSettings());
            exportResponse = exporter.exportDocuments(request.getSessionSettings(),
                                                      request.getConversionDetails());

            httpResponse = Response.ok(exportResponse.serialize()).build();
        } catch (Exception e) {
            e.printStackTrace();

            String errorMessage = (e.getMessage() != null &&
                                   !e.getMessage().isBlank() ? e.getMessage()
                                                             : e.toString());
            exportResponse = new ExportDocumentsResponse();
            exportResponse.setErrorMessage(errorMessage);

            httpResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                   .entity(exportResponse).build();
        } finally {
            double executionTime = (System.currentTimeMillis() - uuidTime) / 1000.0;
            LOGGER.info("{} Webservice request 'ExportDocuments' took {} seconds ({})",
                        Constant.DATETIME_FORMAT.format(new Date()),
                        executionTime,
                        uuidTime);
        }

        return httpResponse;
    }

    @GET
    @Path("burnOneAnnotationInDocument")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonConversionResponse burnOneAnnotationInDocument(// @formatter:off
                                                              @QueryParam("filePath")   String filePath,
                                                              @QueryParam("fontBold")   String fontBold,
                                                              @QueryParam("fontColor")  String fontColor,
                                                              @QueryParam("fontItalic") String fontItalic,
                                                              @QueryParam("fontName")   String fontName,
                                                              @QueryParam("fontSize")   String fontSize,
                                                              @QueryParam("textString") String textString,
                                                              @QueryParam("coordinate") String coordinate) throws IOException {
                                                              // @formatter:on
        ImageReader imageReader = null;
        try {
            TiffFileHandler tiffFileHandler = new TiffFileHandler();
            imageReader = tiffFileHandler.getImageReader(filePath);
            CoordinateParameter oCoordinateParameter = tiffFileHandler.getBestCoordinateForAnnotation(imageReader,
                                                                                                      coordinate,
                                                                                                      fontBold,
                                                                                                      fontItalic,
                                                                                                      fontName,
                                                                                                      fontSize,
                                                                                                      textString);

            RubberStampAnnotation rubberStampAnnotation = new RubberStampAnnotation(fontColor,
                                                                                    fontName,
                                                                                    fontItalic,
                                                                                    fontSize,
                                                                                    fontBold,
                                                                                    Double.valueOf(oCoordinateParameter.getStartX()),
                                                                                    Double.valueOf(oCoordinateParameter.getStartY()),
                                                                                    Double.valueOf(oCoordinateParameter.getWidth()),
                                                                                    Double.valueOf(oCoordinateParameter.getHeight()),
                                                                                    textString,
                                                                                    true);

            JsonConversionResponse jsonConversionResponse = new JsonConversionResponse();

            String outputPath = FilenameUtils.removeExtension(filePath) + "WithAnnotation." +
                                FilenameUtils.getExtension(filePath);
            jsonConversionResponse.setDestFile(tiffFileHandler.addFreeTextAnnotationToTiff(imageReader,
                                                                                           rubberStampAnnotation,
                                                                                           outputPath));

            return jsonConversionResponse;
        } finally {
            imageReader.dispose();
        }
    }

}
