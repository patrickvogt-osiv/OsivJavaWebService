package ch.osiv.helper;

public class LicenseHelper {

    public static void LoadAsposeLicences() throws Exception {

        try {
            new LicenseWrapper("Word");
            new LicenseWrapper("Pdf");
            new LicenseWrapper("Imaging");
            new LicenseWrapper("Email");
            new LicenseWrapper("Cells");
            new LicenseWrapper("Barcode");
        } catch (Exception e) {
            throw e;
        } finally {
            System.gc();
        }
    }

}
