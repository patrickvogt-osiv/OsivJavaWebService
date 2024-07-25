package ch.osiv.helper;

import javax.print.PrintService;

public class OsivPrintJob {

    private String       login              = "";
    private String       domain             = "";
    private String       password           = "";
    private String       fileName           = "";
    private String       printerName        = "";
    private Boolean      landscape          = false;
    private short        numberOfCopies     = 1;
    private String       printJobName       = "";
    private String       fileFormat         = "";
    private Boolean      hasCredentials     = false;
    private Boolean      printWithAspose    = false;
    private String       servicePrinterName = "";
    private PrintService printService       = null;

    public String getServicePrinterName() {
        return servicePrinterName;
    }

    public void setServicePrinterName(String selectedPrinter) {

        this.printService = PrinterHelper.searchForPrinter(selectedPrinter);

        if (this.printService == null) {
            this.servicePrinterName = "";
        } else {
            this.servicePrinterName = this.printService.getName();
        }

    }

    public PrintService getPrintService() {
        return printService;
    }

    public String getLogin() {
        return login;
    }

    public Boolean getPrintWithAspose() {
        return printWithAspose;
    }

    public void setPrintWithAspose(Boolean printWithAspose) {
        if (printWithAspose == null) {

            this.printWithAspose = false;

        } else {

            this.printWithAspose = printWithAspose;

        }
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public Boolean getHasCredentials() {
        return hasCredentials;
    }

    public void setLogin(String login) {
        this.login = login;
        if (this.login.contains("@")) {
            this.domain = this.login.split("@")[1];
            this.login  = this.login.split("@")[0];
        }

        if (!this.login.isEmpty() && !this.password.isEmpty()) {
            this.hasCredentials = true;
        } else {
            this.hasCredentials = false;
        }
    }

    public String getDomain() {
        return domain;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName   = fileName.replace("\"", "");
        this.fileFormat = FileHelper.getFileExtension(fileName, true);

    }

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName.replace("\"", "").trim().toLowerCase();
        this.setServicePrinterName(printerName);
    }

    public Boolean getLandscape() {
        return landscape;
    }

    public void setLandscape(Boolean landscape) {
        this.landscape = landscape;
    }

    public short getNumberOfCopies() {
        return numberOfCopies;
    }

    public void setNumberOfCopies(short numberOfCopies) {
        if (numberOfCopies <= 0) {
            this.numberOfCopies = 1;
        } else {
            this.numberOfCopies = numberOfCopies;
        }
    }

    public String getPrintJobName() {
        return printJobName;
    }

    public void setPrintJobName(String printJobName) {
        if (printJobName.isEmpty()) {
            this.printJobName = this.fileName.substring(this.fileName.lastIndexOf("\\") + 1)
                                             .replace("\\", "");
        } else {
            if (printJobName.contains("\\")) {
                this.printJobName = printJobName.substring(printJobName.lastIndexOf("\\") + 1)
                                                .replace("\\", "");
            } else {
                this.printJobName = printJobName.replace("\\", "");
            }
        }
    }
}
