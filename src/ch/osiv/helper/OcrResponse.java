package ch.osiv.helper;

public class OcrResponse {

    private String  sourceFile;
    private String  ocrFile;
    private String  ocrTextFile;
    private String  contentFormat;
    private String  errorMessage;
    private boolean errorStatus;

    public OcrResponse() {}

    public boolean isErrorStatus() {
        return errorStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setErrorStatus(boolean errorStatus) {
        this.errorStatus = errorStatus;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setOcrFile(String ocrFile) {
        this.ocrFile = ocrFile;
    }

    public void setOcrTextFile(String ocrTextFile) {
        this.ocrTextFile = ocrTextFile;
    }

    public void setContentFormat(String contentFormat) {
        this.contentFormat = contentFormat;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getContentFormat() {
        return contentFormat;
    }

    public String getOcrFile() {
        return ocrFile;
    }

    public String getOcrTextFile() {
        return ocrTextFile;
    }

    @Override
    public String toString() {

        if (errorStatus)
            return errorMessage;
        else
            return "File [sourceFile=" + sourceFile + ", ocrFile=" + ocrFile + ", ocrTextFile=" +
                   ocrTextFile + "]";
    }
}
