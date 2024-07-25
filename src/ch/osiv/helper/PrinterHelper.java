package ch.osiv.helper;

import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.ws.rs.QueryParam;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspose.pdf.FontRepository;
import com.aspose.pdf.RenderingOptions;
import com.aspose.pdf.printing.PdfPrinterResolution;
import com.aspose.pdf.text.SubstitutionFontCategories;
import com.aspose.pdf.text.SystemFontsSubstitution;
import ch.osiv.webservices.DocumentManager;

public class PrinterHelper {

    private static final Logger logger = LoggerFactory.getLogger(DocumentManager.class);

    public static synchronized String GetAvailablePrinters(@QueryParam("printerName")
    String printerName) throws Exception {

        HashPrintRequestAttributeSet printJobAttributeSet = new HashPrintRequestAttributeSet();

        PrintService[] printerArray = PrintServiceLookup.lookupPrintServices(null,
                                                                             printJobAttributeSet);

        String cAvailablePrinters = "getname.tolowercase;getname;printerArray;equals;printerName\n";

        for (int i = 0; i < printerArray.length; i++) {

            printerName        = printerName.replace("\"", "").trim().toLowerCase();
            cAvailablePrinters = cAvailablePrinters +
                                 printerArray[i].getName().trim().toLowerCase() + ";" +
                                 printerArray[i].getName() + ";" + printerArray[i] + ";" +
                                 printerArray[i].getName().equalsIgnoreCase(printerName) + ";" +
                                 printerName + "\n";

        }

        logger.info(cAvailablePrinters);
        return cAvailablePrinters;

    }

    public static JsonPrintJobResponse pdfPrint(OsivPrintJob osivPrintJob) throws Exception {

        JsonPrintJobResponse   pjAnswer                = new JsonPrintJobResponse();
        JsonConversionResponse oJsonConversionResponse = new JsonConversionResponse();

        String string = null;
        pjAnswer.setPrinted(false);
        pjAnswer.setMessage("Unknown error");

        if (osivPrintJob.getServicePrinterName().isEmpty()) {
            string = ("Printer not found: " + osivPrintJob.getPrinterName() + "\0");
            logger.info(string);
            pjAnswer.setPrinted(false);
            pjAnswer.setMessage(string);
            return pjAnswer;
        }

        if ((!osivPrintJob.getFileFormat().equalsIgnoreCase("pdf")) &&
            (!osivPrintJob.getFileFormat().equalsIgnoreCase("tiff")) &&
            (!osivPrintJob.getFileFormat().equalsIgnoreCase("tif"))) {
            pjAnswer.setMessage("Invalid file format: File must be either pdf, tiff or tif ");
            pjAnswer.setPrinted(false);
            return pjAnswer;
        }

        if (osivPrintJob.getFileName().isEmpty()) {
            pjAnswer.setMessage("No file name set");
            pjAnswer.setPrinted(false);
            return pjAnswer;
        }

        if (osivPrintJob.getPrinterName().isEmpty()) {
            pjAnswer.setMessage("no printer name set");
            pjAnswer.setPrinted(false);
            return pjAnswer;
        }

        //If the file is in tiff format convert it to pdf
        if (osivPrintJob.getFileFormat().equalsIgnoreCase("tiff") ||
            osivPrintJob.getFileFormat().equalsIgnoreCase("tif")) {
            try {
                oJsonConversionResponse = FileConverterHelper.convertFileTo(osivPrintJob.getFileName(),
                                                                            FileType.pdf);

                if (oJsonConversionResponse.isErrorStatus()) {
                    pjAnswer.setMessage("Error converting from tiff to pdf");
                    pjAnswer.setPrinted(false);
                    return pjAnswer;
                }

                osivPrintJob.setFileName(oJsonConversionResponse.getDestFile());

            } catch (Exception t) {
                pjAnswer.setMessage("Error converting from tiff to pdf");
                pjAnswer.setPrinted(false);
                return pjAnswer;
            }
        }

        try {

            // Print the job with no impersonation

            logger.info("Print Job started with no impersonation");

            if (osivPrintJob.getPrintWithAspose()) {
                pjAnswer = printFileToPrinterWithAspose(osivPrintJob);

            } else {
                pjAnswer = printFileToPrinterWithJavax(osivPrintJob);
            }

            if (!pjAnswer.isPrinted()) {
                throw new Exception(pjAnswer.getMessage());
            }

        } catch (Exception e) {
            logger.info("Print process finished with following error: " + e.getMessage());
        } finally {
            if (pjAnswer.isPrinted()) {
                logger.info("Print process finished with success");
            }
        }

        return pjAnswer;

    }

    @SuppressWarnings("deprecation")
    private static JsonPrintJobResponse printFileToPrinterWithAspose(OsivPrintJob osivPrintJob) {

        String               printResult = "";
        JsonPrintJobResponse pjAnswer    = new JsonPrintJobResponse();

        try {
            logger.info("printFileToPrinterWithAspose");

            com.aspose.pdf.facades.PdfViewer viewer = new com.aspose.pdf.facades.PdfViewer();

            File    tempFile = new File(osivPrintJob.getFileName());
            boolean exists   = tempFile.exists();

            logger.info("Check if file : " + osivPrintJob.getFileName() + " exists ! ");
            logger.info("File (" + osivPrintJob.getFileName() + ") exists: " + exists);

            if (!exists) {

                viewer.close();
                viewer.dispose();
                viewer = null;

                throw new Exception("File not found or not accessable : " +
                                    osivPrintJob.getFileName());
            }

            // Load the PDF file						
            viewer.bindPdf(osivPrintJob.getFileName());

            /* Set auto resize to true to the printed document page so it can fit.
             * if true print page with scaling to fit to printable area. */

            viewer.setAutoResize(true);
            viewer.setScaleFactor(1);
            viewer.setShowHiddenAreas(false);

            /* try to print as pdf not as image */
            viewer.setPrintAsImage(false);

            /* make sure are used native fonts or the ones that are embedded */

            FontRepository.getSubstitutions()
                          .add((new SystemFontsSubstitution(SubstitutionFontCategories.TheSameNamedEmbeddedFonts)));

            RenderingOptions renderingOptions = new com.aspose.pdf.RenderingOptions();
            renderingOptions.setSystemFontsNativeRendering(true);
            renderingOptions.setInterpolationHighQuality(true);
            renderingOptions.setOptimizeDimensions(true);

            viewer.setRenderingOptions(renderingOptions);

            // Set the printer and page settings

            com.aspose.pdf.printing.PdfPrinterSettings printerSettings = new com.aspose.pdf.printing.PdfPrinterSettings();

            printerSettings.setPrinterName(osivPrintJob.getServicePrinterName());

            printerSettings.setCopies(osivPrintJob.getNumberOfCopies());

            // page settings are the same as the printer settings
            //com.aspose.pdf.printing.PrintPageSettings pageSettings = printerSettings.getDefaultPageSettings(); 			
            com.aspose.pdf.printing.PrintPageSettings pageSettings    = new com.aspose.pdf.printing.PrintPageSettings();
            com.aspose.pdf.printing.PrinterMargins    printerMargings = new com.aspose.pdf.printing.PrinterMargins();

            printerMargings.setBottom(0);
            printerMargings.setTop(0);
            printerMargings.setLeft(0);
            printerMargings.setRight(0);

            pageSettings.setPrinterSettings(printerSettings);
            pageSettings.setMargins(printerMargings);

            // set page resolution

            // Printer resolution set to the highest existing level			
            PdfPrinterResolution printerResolution = pageSettings.getPrinterResolution();

            printerResolution.setKind(com.aspose.pdf.printing.PdfPrinterResolutionKind.High);

            pageSettings.setPrinterResolution(printerResolution);

            pageSettings.setLandscape(osivPrintJob.getLandscape());

            viewer.setPrinterJobName(osivPrintJob.getPrintJobName());
            viewer.printDocumentWithSettings(pageSettings, printerSettings);

            viewer.closePdfFile();

            viewer.close();

            viewer.dispose();

            viewer = null;

            printResult = ("OK\0");

            pjAnswer.setPrinted(true);
            pjAnswer.setMessage(printResult);
            return pjAnswer;

        } catch (Exception pe) {
            logger.error(pe.getMessage());
            printResult = ("Error when printing the document: " + pe.getMessage() + "\0");
            pjAnswer.setPrinted(false);
            pjAnswer.setMessage(printResult);
            return pjAnswer;
        } finally {
            System.gc();
        }
    }

    public static PrintService searchForPrinter(String printerName) {

        HashPrintRequestAttributeSet jobAttrSet = new HashPrintRequestAttributeSet();

        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, jobAttrSet);

        PrintService selectedPrinter = null;

        String svcName = null;

        for (int i = 0; i < services.length; i++) {

            svcName = services[i].getName().trim().toLowerCase();

            if (svcName != null && svcName.equalsIgnoreCase(printerName)) {

                selectedPrinter = services[i];
                logger.info("Printer #" + i + " " + svcName + "  matches to " + printerName);

                break;
            } else {
                logger.info("Printer #" + i + " : " + svcName + " does not match " + printerName);
            }
        }

        return selectedPrinter;

    }

    @SuppressWarnings("unused")
    private static JsonPrintJobResponse printFileToPrinterWithJavax(OsivPrintJob osivPrintJob) {

        String                       string               = "";
        JobName                      printJobName         = null;
        HashPrintRequestAttributeSet printJobAttributeSet = null;
        JsonPrintJobResponse         jsAnswer             = new JsonPrintJobResponse();

        try {
            logger.info("printFileToPrinterWithJavax");

            printJobAttributeSet = new HashPrintRequestAttributeSet();

            try {
                /* Print the document with Apache's PDFBox library
                 * The following code was changed after getting some both sides printing issues
                 * when printing with legacy javax's document print method */

                logger.info("Printing document on " + osivPrintJob.getPrinterName());

                FileInputStream psStream = null;
                try {
                    psStream = new FileInputStream(osivPrintJob.getFileName());
                } catch (Exception ffne) {
                    string = ("file not found: " + ffne.getMessage() + "\0");
                    jsAnswer.setPrinted(false);
                    jsAnswer.setMessage(string);

                    psStream.close();

                    return jsAnswer;
                }

                // Load the document to print 			 				 
                PDDocument pdDocument = PDDocument.load(psStream);

                try {
                    PDFPrintable printable = new PDFPrintable(pdDocument, Scaling.SHRINK_TO_FIT);
                    // Set the print Job
                    PrinterJob job = PrinterJob.getPrinterJob();
                    job.setPrintable(printable);
                    job.setPageable(new PDFPageable(pdDocument));
                    // Set the number of copies
                    logger.info("Number of Copies : " + osivPrintJob.getNumberOfCopies());
                    printJobAttributeSet.add(new Copies(osivPrintJob.getNumberOfCopies()));
                    // Select the output printer
                    job.setPrintService(osivPrintJob.getPrintService());
                    // Set the Job Name
                    job.setJobName(osivPrintJob.getPrintJobName());
                    // Print the job with the Apache's PDFBox library 
                    job.print(printJobAttributeSet);
                    psStream.close();
                    string   = ("OK\0");
                    psStream = null;
                    job      = null;
                    System.gc();
                    jsAnswer.setPrinted(true);
                    jsAnswer.setMessage(string);
                    return jsAnswer;
                } finally {
                    pdDocument.close();
                }

            } catch (Exception pe) {
                logger.info("Error" + pe.getMessage());
                string = ("Error when printing the document: " + pe.getMessage() + "\0");
                jsAnswer.setPrinted(false);
                jsAnswer.setMessage(string);
                return jsAnswer;
            }

        } catch (Exception e) {
            jsAnswer.setPrinted(false);
            jsAnswer.setMessage("Error starting the print process: " + e.getMessage());
            System.gc();
            return jsAnswer;

        } finally {
            printJobName         = null;
            string               = null;
            printJobAttributeSet = null;
            System.gc();
        }
    }
}
