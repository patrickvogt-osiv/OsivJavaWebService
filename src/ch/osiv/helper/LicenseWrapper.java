package ch.osiv.helper;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.osiv.webservices.DocumentManager;

public class LicenseWrapper {

    private static final Logger logger = LoggerFactory.getLogger(DocumentManager.class);

    public LicenseWrapper(String modus) throws Exception {

        try {
            switch (modus.toLowerCase()) {
                case "word":
                    LoadAsposeWordLicence();
                    break;
                case "pdf":
                    LoadAsposePdfLicence();
                    break;
                case "imaging":
                    LoadAsposeImagingLicence();
                    break;
                case "email":
                    LoadAsposeMailLicence();
                    break;
                case "cells":
                    LoadAsposeCellsLicence();
                    break;
                case "barcode":
                    LoadAsposeBarcodeLicence();
                    break;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Loads the Aspose-Cells License
     *
     * @throws Exception
     */
    public void LoadAsposeCellsLicence() throws Exception {

        logger.info("#### Opening Aspose Cells Licence File");

        InputStream inputStream = null;

        try {
            inputStream = getClass().getResourceAsStream("Aspose.Total.Java.lic");

            // Instantiate the License class
            com.aspose.cells.License cellLicense = new com.aspose.cells.License();

            //Set the license through the stream object
            cellLicense.setLicense(inputStream);

            cellLicense = null;
        } catch (Exception e) {
            throw e;
        } finally {
            inputStream.close();
            inputStream = null;
            System.gc();
        }
    }

    public void LoadAsposeImagingLicence() throws Exception {

        logger.info("#### Opening Aspose Imaging Licence File");

        InputStream inputStream = null;

        try {
            inputStream = getClass().getResourceAsStream("Aspose.Total.Java.lic");

            // Instantiate the License class
            com.aspose.imaging.License imagingLicense = new com.aspose.imaging.License();

            //Set the license through the stream object
            imagingLicense.setLicense(inputStream);

            imagingLicense = null;
        } catch (Exception e) {
            throw e;
        } finally {
            inputStream.close();
            inputStream = null;
            System.gc();
        }
    }

    public void LoadAsposePdfLicence() throws Exception {

        logger.info("#### Opening Aspose Pdf Licence File");

        InputStream inputStream = null;

        try {
            inputStream = getClass().getResourceAsStream("Aspose.Total.Java.lic");

            // Instantiate the License class
            com.aspose.pdf.License pdfLicense = new com.aspose.pdf.License();

            //Set the license through the stream object
            pdfLicense.setLicense(inputStream);

            pdfLicense = null;

        } catch (Exception e) {
            throw e;
        } finally {
            inputStream.close();
            inputStream = null;
            System.gc();
        }
    }

    public void LoadAsposeWordLicence() throws Exception {

        logger.info("#### Opening Aspose Word Licence File");

        InputStream inputStream = null;

        try {

            inputStream = getClass().getResourceAsStream("Aspose.Total.Java.lic");

            // Instantiate the License class
            com.aspose.words.License wordLicense = new com.aspose.words.License();

            //Set the license through the stream object
            wordLicense.setLicense(inputStream);

            wordLicense = null;

        } catch (Exception e) {
            throw e;
        } finally {
            if (inputStream != null)
                inputStream.close();
            inputStream = null;
            System.gc();
        }
    }

    public void LoadAsposeMailLicence() throws Exception {

        logger.info("#### Opening Aspose Mail Licence File");

        InputStream inputStream = null;

        try {

            inputStream = getClass().getResourceAsStream("Aspose.Total.Java.lic");

            // Instantiate the License class
            com.aspose.email.License emaiLicense = new com.aspose.email.License();

            //Set the license through the stream object
            emaiLicense.setLicense(inputStream);

            emaiLicense = null;

        } catch (Exception e) {
            throw e;
        } finally {
            inputStream.close();
            inputStream = null;
            System.gc();
        }
    }

    public void LoadAsposeBarcodeLicence() throws Exception {

        logger.info("#### Opening Aspose Barcode Licence File");

        InputStream inputStream = null;

        try {
            inputStream = getClass().getResourceAsStream("Aspose.Total.Java.lic");
            // Instantiate the License class
            com.aspose.barcode.License barcodeLicense = new com.aspose.barcode.License();
            //Set the license through the stream object
            barcodeLicense.setLicense(inputStream);
            barcodeLicense = null;

        } catch (Exception e) {
            throw e;
        } finally {
            inputStream.close();
            inputStream = null;
            System.gc();
        }
    }

}
