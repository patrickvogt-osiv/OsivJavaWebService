package ch.osiv.helper;

/**
 * JsonConversionResponse
 */
public class JsonConversionResponse
    extends JsonSerializable {

    private String  sourceFile;
    private String  destFile;
    private Integer pageCount;
    private String  errorMessage;
    private boolean errorStatus;

    /**
     * JsonConversionResponse
     */
    public JsonConversionResponse() {}

    /**
     * @return
     */
    public boolean isErrorStatus() {
        return errorStatus;
    }

    /**
     * @return
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @param errorStatus
     */
    public void setErrorStatus(boolean errorStatus) {
        this.errorStatus = errorStatus;
    }

    /**
     * @param sourceFile
     */
    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    /**
     * @param destFile
     */
    public void setDestFile(String destFile) {
        this.destFile = destFile;
    }

    /**
     * @param pageCount
     */
    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    /**
     * @return
     */
    public String getSourceFile() {
        return sourceFile;
    }

    /**
     * @return
     */
    public String getDestFile() {
        return destFile;
    }

    /**
     * @return
     */
    public Integer getPageCount() {
        return pageCount;
    }

    @Override
    public String toString() {

        if (errorStatus)
            return errorMessage;
        else
            return "File [sourceFile=" + sourceFile + ", destFile=" + destFile + ", pageCount=" +
                   pageCount + "]";
    }
}
