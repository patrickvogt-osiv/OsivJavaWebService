package ch.osiv.document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspose.imaging.fileformats.tiff.enums.TiffExpectedFormat;
import com.aspose.pdf.Document;
import com.aspose.pdf.GoToAction;
import com.aspose.pdf.OutlineItemCollection;

import ch.osiv.Constant;
import ch.osiv.document.convert.CombinePdfFilesComperator;
import ch.osiv.document.convert.ConversionDetails;
import ch.osiv.document.convert.ConvertMethod;
import ch.osiv.document.convert.DocumentColor;
import ch.osiv.document.dms.DmsDocument;
import ch.osiv.document.dms.DownloadFileResponse;
import ch.osiv.document.dms.Fault;
import ch.osiv.document.dms.KineticServiceSettings;
import ch.osiv.document.dms.LoadDocumentMethod;
import ch.osiv.document.dms.StreamService;
import ch.osiv.document.dms.WMObjectInfo;
import ch.osiv.document.dms.WMObjectService;
import ch.osiv.helper.FileConverterHelper;
import ch.osiv.helper.FileHelper;
import ch.osiv.helper.FileType;
import ch.osiv.helper.JsonConversionResponse;
import ch.osiv.webservices.HttpClientException;

/**
 * DocumentExporter class
 *
 * @author Arno van der Ende
 */
public class DocumentExporter {

    private KineticServiceSettings kineticSettings;
    private WMObjectService        dmsObjectService;
    private StreamService          dmsStreamService;

    private static final String ANNOTATION_FILE_EXTENSION = "Eingebrannte Annotationen.ann";
    private static final Logger LOGGER                    = LoggerFactory.getLogger(DocumentExporter.class);

    /**
     * Constructor
     *
     * @param kineticSettings Kinetic service settings
     */
    public DocumentExporter(KineticServiceSettings kineticSettings) {
        super();

        if (kineticSettings == null) {
            throw new NullPointerException("Parameter 'kineticServiceSettings' cannot be null");
        }

        this.kineticSettings  = kineticSettings;
        this.dmsObjectService = WMObjectService.getInstance(kineticSettings.getKineticServiceUrl());

        if (kineticSettings.getLoadDocumentMethod() == LoadDocumentMethod.LOAD_FROM_STREAM) {
            this.dmsStreamService = StreamService.getInstance(kineticSettings.getKineticServiceUrl());
        }
    }

    /**
     * Download the document with a webservice call
     *
     * @param wmSessionId Kinetic session id
     * @param document    The document object
     * @param saveDir     The directory where to save the file
     * @return All the document info, including 'document file', 'session file id' and 'version
     *         number'
     * @throws Exception
     */
    protected ExportedDocumentInfo downloadDocument(String wmSessionId,
                                                    DmsDocument document,
                                                    String saveDir) throws Exception {

        ExportedDocumentInfo docInfo = exportDocument(wmSessionId, document);

        DownloadFileResponse downloadResponse = dmsStreamService.downloadFile(docInfo.getFileId());

        File documentFile = new File(saveDir, downloadResponse.getFileName());

        // use try-with-resource statement because the ImageInputStream is-a AutoClosable,
        // thus the stream will be closed automatically
        try (FileOutputStream fileOutputStream = new FileOutputStream(documentFile)) {
            // write the byte array to file
            fileOutputStream.write(downloadResponse.getContent());
        }

        docInfo.setDocumentFile(Path.of(documentFile.getAbsolutePath()));

        return docInfo;
    }

    /**
     * Load the document into the Kinetic session and returns the document session fileId
     *
     * @param wmSessionId Kinetic session id
     * @param document    The document object
     * @return All the document info, including 'session file id' and 'version number'
     * @throws Exception
     */
    private ExportedDocumentInfo exportDocument(String wmSessionId,
                                                DmsDocument document) throws Exception {

        ExportedDocumentInfo exportedDocInfo = null;

        // load the document in the Kinetic session
        try {
            dmsObjectService.loadByVersionId(wmSessionId, document.getVersionId());

            // physically export the document from the Kinetic session into the Kinetic temp-dir
            String fileId = dmsObjectService.exportDocument(wmSessionId);

            // fileId is mandatory in order to proceed
            if (fileId == null || fileId.isEmpty()) {
                throw new RuntimeException("Unable to load document '" + document.getVersionId() +
                                           "'");
            }

            exportedDocInfo = new ExportedDocumentInfo(fileId);

            // get the object information and assign it to the return parameter
            WMObjectInfo objectInfo = dmsObjectService.getObjectInfo(wmSessionId);
            exportedDocInfo.setObjectInfo(objectInfo);
        } catch (HttpClientException e) {
            // It's possible a WM call returns an error response, with a XML body containing the error
            // Try to get the error-text from the body and throw that as an error
            // If not, throw the original error
            Fault fault = this.dmsObjectService.parseResponseExceptionBody(e.getResponseBody());
            if (fault != null) {
                throw new RuntimeException(fault.getFaultstring(), e); // add 'e' as inner-exception so it see show up in the logfile
            }
            throw e;
        }

        return exportedDocInfo;
    }

    /**
     * Export one or many documents with the given conversion method
     *
     * @param sessionSettings   The settings to use for this call
     * @param conversionDetails Details of the conversion
     * @return ExportDocumentsResponse
     * @throws Exception
     */
    public ExportDocumentsResponse exportDocuments(SessionSettings sessionSettings,
                                                   ConversionDetails conversionDetails) throws Exception {

        // first validate the request parameters
        validateRequest(sessionSettings, conversionDetails);

        // determine number of threads: never more then 10
        // when threads is disabled, use 1 thread
        int numDocuments = conversionDetails.getDocuments().size();
        int numThreads   = (sessionSettings.getUseThreads()) ? Integer.min(numDocuments, 10)
                                                             : 1;

        // initialize vars
        ExportDocumentsResponse response           = new ExportDocumentsResponse();
        String                  dmsCacheSessionDir = sessionSettings.getDmsCacheSessionDir();
        Path                    targetDir          = Path.of(dmsCacheSessionDir,
                                                             sessionSettings.getTargetDirName()); // NOTE: when targetDirName is empty, then dmsCacheSessionDir is the resultPath

        FileHelper.ensureDirectoryExist(targetDir);

        // @formatter:off
        LOGGER.info("{} Using {} {} for {} {} with conversion mode: {}",
                    Constant.DATETIME_FORMAT.format(new Date()),
                    numThreads,
                    (numThreads == 1) ? "thread" : "threads",
                    numDocuments,
                    (numDocuments == 1) ? "document" : "documents",
                    conversionDetails.getConvertMethod());
        // @formatter:on

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Exception> exceptions      = new ArrayList<Exception>();              // use a List here, because with lamba expression the variable should be final

        // iterate over all documents
        List<DmsDocument> documents = conversionDetails.getDocuments();
        for (DmsDocument document : documents) {
            // lamba expression to start the Thread
            executorService.submit(() -> {
                try {
                    // Steps 1 - 6 : All the tasks which can run simultaneous
                    processDocumentForExport(document,
                                             dmsCacheSessionDir,
                                             targetDir,
                                             conversionDetails);
                } catch (Exception e) {
                    //LOGGER.error("{} Error processing document {}", Constant.DATETIME_FORMAT.format(new Date()), document.getVersionId());
                    e.printStackTrace();
                    exceptions.add(e);
                    executorService.shutdownNow(); // when an error occurs, stop all threads immediately
                }
            });
        } // for DmsDocument

        // shutdown the executor and wait for all tasks to complete
        // it's important not to set the wait-time too low, since then the rest of
        // the code is already executed before all Threads are possibly finished
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw e;
        }

        // here all threads are finished (or interrupted)

        // did one thread produce an Exception, throw it here (there can only be 1)
        if (!exceptions.isEmpty()) {
            throw exceptions.get(0);
        }

        String sourceFolder = targetDir.toString().replace("\\", "/").replace("\n", "");
        sourceFolder = sourceFolder + "/";
        sourceFolder = URLDecoder.decode(sourceFolder, "UTF-8");
        List<Path> fileArray = new ArrayList<Path>();

        fileArray = FileHelper.retrieveFilesListFromFolder(sourceFolder, FileType.pdf);

        for (int j = 0; j < fileArray.size(); j++) {
            Path path = fileArray.get(j);

            if (path.toString().contains("Aktenverzeichnis")) {
                addBookMarkToFirstPage(path.toString(), "Aktenverzeichnis");
            }
            if (path.toString().contains("Inhaltsverzeichnis")) {
                addBookMarkToFirstPage(path.toString(), "Dossier Index");
            }

        }

        for (int i = 0; i < documents.size(); i++) {
            DmsDocument dmsDocument = documents.get(i);

            if (dmsDocument.getBookmarkName() != null &&
                !dmsDocument.getBookmarkName().equals("")) {
                addBookMarkToFirstPage(sourceFolder + dmsDocument.getDocumentName(),
                                       dmsDocument.getBookmarkName());
            }

        }

        // Step 7 : Merge PDF files
        if (conversionDetails.getConvertMethod() == ConvertMethod.TO_PDF &&
            conversionDetails.getCombinePdfFiles()) {
            String mergedPdfFile = mergePdfFiles(targetDir, documents);
            response.setResultFile(mergedPdfFile);
        }

        // Step 8 : Merge TIFF files
        if (conversionDetails.getConvertMethod() == ConvertMethod.MERGE_TIFF) {
            int tiffFormat = (conversionDetails.getDocumentColor() == DocumentColor.BLACK_AND_WHITE) ? TiffExpectedFormat.TiffCcittFax4
                                                                                                     : TiffExpectedFormat.TiffLzwRgb;
            this.mergeTiffFiles(targetDir,
                                documents,
                                tiffFormat,
                                sessionSettings.getUseThreads(),
                                response);
        }

        // Step 9 : Assign response fields (Step 8-MERGE_TIFF sets the response fields itself)
        else {
            // with EXPORT mode it's possible the documentName field contains the error message
            if (conversionDetails.getConvertMethod() == ConvertMethod.EXPORT) {
                String errors = "";
                for (DmsDocument document : documents) {
                    if (document.getDocumentName().startsWith("ERROR: ")) {
                        errors += ((!errors.isEmpty()) ? ", "
                                                       : "") +
                                  document.getDocumentName().substring(7); // "ERROR: " is 7 chars
                        document.setDocumentName(null);
                    }
                }
                if (!errors.isEmpty()) {
                    response.setErrorMessage(errors);
                }
            }

            response.setResultPath(targetDir.toString());
            response.setDocuments(conversionDetails.getDocuments());
        }

        return response;
    }

    /**
     * Add a bookMark to the first page
     *
     * @param pathFile     of the PDF
     * @param bookmarkName name of the bookmark
     */
    private void addBookMarkToFirstPage(String pathFile,
                                        String bookmarkName) {
        Document              pdfDocument = new Document(pathFile);
        OutlineItemCollection pdfOutline  = new OutlineItemCollection(pdfDocument.getOutlines());
        pdfOutline.setTitle(bookmarkName);

        // Set the destination page number
        pdfOutline.setAction(new GoToAction(pdfDocument.getPages().get_Item(1)));

        // Add a bookmark in the document's outline collection.
        pdfDocument.getOutlines().add(pdfOutline);
        pdfDocument.save(pathFile);
    }

    /**
     * Task to merge pdf files
     *
     * @param targetDir Target dir where the PDF files are
     * @param documents List of documents to merge - this also determines the order to merge
     * @return The merged PDF file
     * @throws Exception
     */
    private String mergePdfFiles(Path targetDir,
                                 List<DmsDocument> documents) throws Exception {

        Comparator<Path>       comperator    = new CombinePdfFilesComperator(documents);
        JsonConversionResponse mergeResponse = FileConverterHelper.mergePdfFilesIntoOne(targetDir.toString(),
                                                                                        comperator);
        // Handle the response error
        if (mergeResponse.isErrorStatus()) {
            throw new RuntimeException("Unable to combine/merge pdf files: " +
                                       mergeResponse.getErrorMessage());
        }
        return mergeResponse.getDestFile();
    }

    /**
     * Task to merge tiff files multi-threaded
     *
     * @param targetDir  The dir which contains the TIF files
     * @param documents  List of documents to merge
     * @param tiffFormat The output format (color)
     * @throws Exception
     */
    private void mergeTiffFiles(Path targetDir,
                                List<DmsDocument> allDocuments,
                                int tiffFormat,
                                boolean useThreads,
                                ExportDocumentsResponse response) throws Exception {

        // group by groupId in a new Map object
        Map<Integer, List<DmsDocument>> documentsPerGroup = allDocuments.stream()
                                                                        .collect(Collectors.groupingBy(DmsDocument::getGroupId));

        List<DmsDocument> mergedDocumentList = new ArrayList<DmsDocument>();

        // determine number of threads: never more then 10
        // when threads is disabled, use 1 thread
        int numGroups  = documentsPerGroup.size();
        int numThreads = (useThreads) ? Integer.min(numGroups, 10)
                                      : 1;

        // @formatter:off
        LOGGER.info("{} Merge TIFF files, by using {} {} for {} {}",
                    Constant.DATETIME_FORMAT.format(new Date()),
                    numThreads, (numThreads == 1) ? "thread" : "threads",
                    numGroups,  (numGroups  == 1) ? "group"  : "groups");
        // @formatter:on

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Exception> exceptions      = new ArrayList<Exception>();              // use a List here, because with lamba expression the variable should be final

        for (Map.Entry<Integer, List<DmsDocument>> entry : documentsPerGroup.entrySet()) {
            // lamba expression to start the Thread
            executorService.submit(() -> {
                try {
                    int               groupId        = entry.getKey();
                    List<DmsDocument> documents      = entry.getValue();
                    DmsDocument       mergedDocument = processMergeTiffFilesForGroup(targetDir,
                                                                                     documents,
                                                                                     groupId,
                                                                                     tiffFormat);
                    mergedDocumentList.add(mergedDocument);
                } catch (Exception e) {
                    //LOGGER.error("{} Error merging documents of group {}", Constant.DATETIME_FORMAT.format(new Date()), groupId);
                    e.printStackTrace();
                    exceptions.add(e);
                    executorService.shutdownNow(); // when an error occurs, stop all threads immediately
                }
            });
        }

        // shutdown the executor and wait for all tasks to complete
        // it's important not to set the wait-time too low, since then the rest of
        // the code is already executed before all Threads are possibly finished
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw e;
        }

        // here all threads are finished (or interrupted)

        // did one thread produce an Exception, throw it here (there can only be 1)
        if (!exceptions.isEmpty()) {
            throw exceptions.get(0);
        }

        response.setResultPath(targetDir.toString());
        response.setDocuments(mergedDocumentList);
    }

    /**
     * Load the document from disk: use the temporary folder of Kinetic
     *
     * @param wmSession Kinetic session id
     * @param document  The document object
     * @return All the document info, including 'document file', 'session file id' and 'version
     *         number'
     * @throws Exception
     */
    protected ExportedDocumentInfo loadDocumentFromDisk(String wmSessionId,
                                                        DmsDocument document) throws Exception {

        ExportedDocumentInfo docInfo = exportDocument(wmSessionId, document);

        Path fullPath = Path.of(kineticSettings.getKineticTempDir(),
                                docInfo.getFileId(),
                                docInfo.getObjectInfo().getName());

        docInfo.setDocumentFile(fullPath);

        return docInfo;
    }

    /**
     * Moves and renames the exported document file
     *
     * @param documentFile    The exported file
     * @param documentVersion The document version number
     * @param destinationDir  The destination directory
     * @return The full path of the new file
     * @throws IOException
     */
    private Path moveDocumentFile(Path documentFile,
                                  int documentVersion,
                                  Path destinationDir) throws IOException {

        String newFileName = String.format("%s-%s.%s",
                                           FileHelper.getBaseName(documentFile, false),
                                           documentVersion,
                                           FileHelper.getFileExtension(documentFile));

        Path newFullFile = Path.of(destinationDir.toString(), newFileName);

        FileHelper.ensureDirectoryExist(destinationDir);

        return FileHelper.moveFileTo(documentFile, newFullFile);
    }

    /**
     * Process documents: tasks to perform inside the thread
     *
     * @param document           The DMS document
     * @param dmsCacheSessionDir DMS cache session directory
     * @param targetDir          The directory where to save all the files
     * @param conversionDetails  The conversion details to use
     * @param wmSessionId        DMS session id
     * @throws Exception
     */
    private void processDocumentForExport(DmsDocument document,
                                          String dmsCacheSessionDir,
                                          Path targetDir,
                                          ConversionDetails conversionDetails) throws Exception {

        ExportedDocumentInfo documentInfo = null;
        Path                 documentFile = null;
        String               wmSessionId  = null;
        TiffFileHandler      tiffHandler  = new TiffFileHandler();

        LOGGER.info("{} Start processing document {}",
                    Constant.DATETIME_FORMAT.format(new Date()),
                    document.getVersionId());

        try {
            // Step 1 : Create WMSession
            wmSessionId = dmsObjectService.createWMSession();
            if (wmSessionId == null || wmSessionId.isEmpty()) {
                throw new RuntimeException("Could not create a WMSession");
            }

            // Step 2a : Load from disk
            if (kineticSettings.getLoadDocumentMethod() == LoadDocumentMethod.LOAD_FROM_DISK) {
                documentInfo = loadDocumentFromDisk(wmSessionId, document);
                documentFile = documentInfo.getDocumentFile();
                if (Files.notExists(documentFile)) {
                    LOGGER.warn("{} File '{}' not found, try to download it instead!",
                                Constant.DATETIME_FORMAT.format(new Date()),
                                document.getVersionId());
                    documentFile = null; // set to 'null' so second try is with LOAD_FROM_STEAM
                    // now we need dmsStreamService, but that's only instantiated in the constructor when method is LOAD_FROM_STREAM,
                    // that's why we need to create the instance here
                    if (this.dmsStreamService == null) {
                        this.dmsStreamService = StreamService.getInstance(kineticSettings.getKineticServiceUrl());
                    }

                }
            }

            // Step 2b : Download file
            if (kineticSettings.getLoadDocumentMethod() == LoadDocumentMethod.LOAD_FROM_STREAM ||
                documentFile == null) {

                documentInfo = downloadDocument(wmSessionId, document, dmsCacheSessionDir);
                documentFile = documentInfo.getDocumentFile();
            }

            // Error when not succeeded
            if (documentFile == null || Files.notExists(documentFile)) {
                throw new RuntimeException("Unable to get document: " + document.getVersionId());
            }

            // Step 3 : Move and rename the file
            documentFile = moveDocumentFile(documentFile,
                                            documentInfo.getObjectInfo().getVersionNumber(),
                                            Path.of(dmsCacheSessionDir));
            String docName = FileHelper.getBaseName(documentFile, false); // save finalName, because annotations might change the filename

            // Step 4 : Burn annotations
            if (conversionDetails.getBurnAnnotations()) {
                Path annotationFile = Path.of(dmsCacheSessionDir,
                                              String.format("%s.%s",
                                                            document.getVersionId(),
                                                            ANNOTATION_FILE_EXTENSION));
                if (Files.exists(annotationFile)) {
                    String documentWithAnnFile = tiffHandler.addAnnotationToTiff(documentFile.toString(),
                                                                                 annotationFile.toString(),
                                                                                 conversionDetails.getDocumentColor());
                    // set the new documentFile, which is someting like: "<docname>WithAnnotation.TIF"
                    documentFile = Path.of(documentWithAnnFile);
                }
            }

            // Step 5 : Convert document
            switch (conversionDetails.getConvertMethod()) {
                // Step 5a : Only Export
                case EXPORT:
                    // file is already exported, so only move to target_dir (when needed) and only set the filename
                    // move to targetDir (only when targetDir differs from dmsCacheSessionDir)
                    if (!targetDir.equals(Path.of(dmsCacheSessionDir))) {
                        documentFile = FileHelper.moveFileTo(documentFile, targetDir);
                    }
                    document.setDocumentName(FileHelper.getBaseName(documentFile));
                    document.setNrPages(tiffHandler.getNumberOfPages(documentFile.toString()));
                    break;

                // Step 5b : Convert to PDF
                case TO_PDF:
                    JsonConversionResponse convResponse = FileConverterHelper.convertFileTo(documentFile.toString(),
                                                                                            FileType.pdf);
                    // Handle the response error
                    if (convResponse.isErrorStatus()) {
                        throw new RuntimeException("Unable to convert document to PDF: " +
                                                   convResponse.getErrorMessage());
                    }
                    // targetDir can also be dmsCacheSessionDir
                    Path targetFile = Path.of(targetDir.toString(), docName + "." + FileType.pdf);
                    // only move/rename when source & target are different
                    if (!Path.of(convResponse.getDestFile()).equals(targetFile)) {
                        FileHelper.moveFileTo(Path.of(convResponse.getDestFile()), targetFile);
                    }
                    document.setNrPages(convResponse.getPageCount());
                    document.setDocumentName(FileHelper.getBaseName(targetFile));
                    break;

                // Step 5c : Split TIFF pages to seperate files
                case SPLIT_TIFF:
                    // move to targetDir (only when targetDir differs from dmsCacheSessionDir)
                    if (!targetDir.equals(Path.of(dmsCacheSessionDir))) {
                        documentFile = FileHelper.moveFileTo(documentFile, targetDir);
                    }
                    // use the document name as folder for the splitted TIF files
                    String splitDir = tiffHandler.splitTiff(documentFile.toString(),
                                                            conversionDetails.getDocumentColor(),
                                                            docName); // docName as subfolder
                    document.setNrPages(FileHelper.getFiles(splitDir).length);
                    document.setDocumentName(docName);
                    break;

                // Step 5d : Merge tiff can only be done when all files are exported. This only saves the documentName and nrPages
                case MERGE_TIFF:
                    // move to targetDir (only when targetDir differs from dmsCacheSessionDir)
                    if (!targetDir.equals(Path.of(dmsCacheSessionDir))) {
                        documentFile = FileHelper.moveFileTo(documentFile, targetDir);
                    }
                    document.setDocumentName(FileHelper.getBaseName(documentFile));
                    break;
            }

            LOGGER.info("{} Finished processing document {}",
                        Constant.DATETIME_FORMAT.format(new Date()),
                        document.getVersionId());
        } catch (Exception e) {
            // with conversionMode EXPORT, the process may continue getting other documents
            // that's why it temporary saves the error in the DmsDocument object and
            // writes that later back in the errorMessage response field
            if (conversionDetails.getConvertMethod() == ConvertMethod.EXPORT) {
                LOGGER.warn("{} Error occurred processing document {} and it will be skipped, because convertMethod is EXPORT (Error: {})",
                            Constant.DATETIME_FORMAT.format(new Date()),
                            document.getVersionId(),
                            e.getMessage());
                document.setDocumentName("ERROR: " + e.getMessage());
            } else {
                LOGGER.error("{} Error occurred processing document {}",
                             Constant.DATETIME_FORMAT.format(new Date()),
                             document.getVersionId());
                throw e;
            }
        } finally {
            // Step 6 : Terminate the session
            if (wmSessionId != null) {
                dmsObjectService.terminateWMSession(wmSessionId);
            }
        }
    }

    /**
     * Task to merge tiff files (in a multi-threaded context)
     *
     * @param targetDir  The target directory where to save the merged file
     * @param documents  List of documents to merge
     * @param groupId    The group ID
     * @param tiffFormat The tiff format (color)
     * @return DmsDocument
     * @throws IOException
     */
    private DmsDocument processMergeTiffFilesForGroup(Path targetDir,
                                                      List<DmsDocument> documents,
                                                      int groupId,
                                                      int tiffFormat) throws IOException {

        List<Path>      tiffFiles   = new ArrayList<>();
        TiffMerger      merger      = null;
        TiffFileHandler tiffHandler = new TiffFileHandler();

        for (DmsDocument document : documents) {
            Path tiffFile = Path.of(targetDir.toString(), document.getDocumentName());
            tiffFiles.add(tiffFile);
        }

        try {
            merger = new TiffMerger();
            Path mergedFile = merger.mergeTiffFiles(tiffFiles, targetDir, tiffFormat);

            DmsDocument newDoc = new DmsDocument();
            newDoc.setGroupId(groupId);
            newDoc.setDocumentName(mergedFile.getFileName().toString());
            newDoc.setNrPages(tiffHandler.getNumberOfPages(mergedFile.toString()));

            return newDoc;

        } finally {
            merger = null;
            System.gc();
        }

    }

    /**
     * Validate the incoming parameters
     *
     * @param sessionSettings   The settings to use for this call
     * @param conversionDetails Details of the conversion
     * @throws Exception
     */
    private void validateRequest(SessionSettings sessionSettings,
                                 ConversionDetails conversionDetails) throws Exception {

        if (kineticSettings.getLoadDocumentMethod() == null) {
            throw new NullPointerException("Kinetic setting 'loadDocumentMethod' cannot be null");
        }

        if (sessionSettings == null) {
            throw new NullPointerException("Session settings cannot be null");
        }

        String dmsCacheSessionDir = sessionSettings.getDmsCacheSessionDir();

        if (dmsCacheSessionDir == null) {
            throw new NullPointerException("Parameter 'dmsCacheSessionDir' cannot be null");
        }

        if (dmsCacheSessionDir.isBlank()) {
            throw new IllegalArgumentException("Parameter 'dmsCacheSessionDir' cannot be empty");
        }

        if (Files.notExists(Path.of(dmsCacheSessionDir))) {
            throw new FileNotFoundException("DMS cache session directory '" + dmsCacheSessionDir +
                                            "' does not exist");
        }

        if (conversionDetails == null) {
            throw new NullPointerException("Parameter 'conversionDetails' cannot be null");
        }

        if (conversionDetails.getDocuments() == null ||
            conversionDetails.getDocuments().size() == 0) {
            throw new IllegalArgumentException("Conversion detail 'documents' cannot be empty");
        }

        if (conversionDetails.getConvertMethod() == null) {
            throw new NullPointerException("Conversion detail 'convertMethod' cannot be null");
        }

        if (conversionDetails.getConvertMethod() != ConvertMethod.TO_PDF &&
            conversionDetails.getConvertMethod() != ConvertMethod.SPLIT_TIFF &&
            conversionDetails.getConvertMethod() != ConvertMethod.MERGE_TIFF &&
            conversionDetails.getConvertMethod() != ConvertMethod.EXPORT) {
            throw new UnsupportedOperationException("ConvertMethod '" +
                                                    conversionDetails.getConvertMethod().name() +
                                                    "' is not supported");
        }

        if (conversionDetails.getConvertMethod() != ConvertMethod.TO_PDF &&
            conversionDetails.getCombinePdfFiles()) {
            throw new IllegalArgumentException("Conversion detail 'combinePdfFiles' cannot be 'true' with convert method '" +
                                               conversionDetails.getConvertMethod().name() + "'");
        }

        if (kineticSettings.getLoadDocumentMethod() != LoadDocumentMethod.LOAD_FROM_DISK &&
            kineticSettings.getLoadDocumentMethod() != LoadDocumentMethod.LOAD_FROM_STREAM) {
            throw new UnsupportedOperationException("LoadDocumentMethod '" +
                                                    kineticSettings.getLoadDocumentMethod().name() +
                                                    "' is not supported");
        }

        if (kineticSettings.getLoadDocumentMethod() == LoadDocumentMethod.LOAD_FROM_DISK) {
            if (kineticSettings.getKineticTempDir() == null) {
                throw new NullPointerException("Kinetic setting 'kineticTempDir' cannot be null");
            }
            if (kineticSettings.getKineticTempDir().isBlank()) {
                throw new IllegalArgumentException("Kinetic setting 'kineticTempDir' cannot be empty");
            }
            if (Files.notExists(Path.of(kineticSettings.getKineticTempDir()))) {
                throw new FileNotFoundException("Kinetic setting 'kineticTempDir' does not exist: " +
                                                kineticSettings.getKineticTempDir());
            }
        }

    }

}
