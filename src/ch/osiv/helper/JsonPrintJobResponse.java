package ch.osiv.helper;

/**
 * JsonPrintJobResponse
 */
public class JsonPrintJobResponse {

    private boolean isPrinted;
    private String  message;

    /**
     * @return
     */
    public boolean isPrinted() {
        return isPrinted;
    }

    /**
     * @param isPrinted
     */
    public void setPrinted(boolean isPrinted) {
        this.isPrinted = isPrinted;
    }

    /**
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 
     */
    public JsonPrintJobResponse() {
        super();
        this.isPrinted = false;
        this.message   = "";
    }
}
