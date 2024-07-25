package ch.osiv.document.dms;

import ch.osiv.helper.JsonSerializable;

/**
 * DownloadFileResponse class
 *
 * @author Arno van der Ende
 */
public class DownloadFileResponse
    extends JsonSerializable {

    private String errorCode;
    private String errorDescription;
    private String fileName;
    private long   length;
    private byte[] content;

    /**
     * Getter for errorCode
     *
     * @return errorCode
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Setter for errorCode
     *
     * @param errorCode The errorCode to set
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Getter for errorDescription
     *
     * @return errorDescription
     */
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * Setter for errorDescription
     *
     * @param errorDescription The errorDescription to set
     */
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    /**
     * Getter for fileName
     *
     * @return fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Setter for fileName
     *
     * @param fileName The fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Getter for length
     *
     * @return length
     */
    public long getLength() {
        return length;
    }

    /**
     * Setter for length
     *
     * @param length The length to set
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * Getter for content
     *
     * @return content
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Setter for content
     *
     * @param content The content to set
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

}
