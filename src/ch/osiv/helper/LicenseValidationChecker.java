package ch.osiv.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LicenseValidationChecker {

    private String                  licenseFileName = "Aspose.Total.Java.lic";
    private Integer                 remainingDays;
    private boolean                 expired;
    private String                  expirationDate;
    private static SimpleDateFormat Format          = new SimpleDateFormat("dd/MM/yyyy");

    public LicenseValidationChecker() {
        try {
            checkExpiration();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set
     *
     * @throws ParseException
     * @throws IOException
     */
    private void checkExpiration() throws ParseException, IOException {

        expirationDate = getExpiration();
        Date current = new Date();
        Date expire  = Format.parse(expirationDate);

        if (current.before(expire)) {
            this.expired = false;
        } else {
            expired = true;
        }

        long diff = expire.getTime() - current.getTime();
        remainingDays = (int) (diff / (24 * 60 * 60 * 1000));

    }

    /**
     * Reading License File and return the Expiration date as String
     *
     * @return String expiration Date
     * @throws IOException
     */
    private String getExpiration() throws IOException {
        String         expDate     = "";
        InputStream    inputStream = getClass().getResourceAsStream(licenseFileName);
        BufferedReader br          = new BufferedReader(new InputStreamReader(inputStream));

        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.contains("<SubscriptionExpiry>")) {
                expDate = line.substring(line.indexOf("<SubscriptionExpiry>") +
                                         "<SubscriptionExpiry>".length(),
                                         line.lastIndexOf("</SubscriptionExpiry>"));
            }
        }

        String dd   = expDate.substring(6).trim();
        String MM   = expDate.substring(4, 6).trim();
        String yyyy = expDate.substring(0, 4).trim();
        expDate = dd + "/" + MM + "/" + yyyy;

        br.close();
        inputStream.close();

        return expDate;
    }

    public String getLicenseFileName() {
        return licenseFileName;
    }

    public void setLicenseFileName(String licenseFileName) {
        this.licenseFileName = licenseFileName;
    }

    public int getRemainingDays() {
        return remainingDays;
    }

    public boolean isExpired() {
        return expired;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

}
