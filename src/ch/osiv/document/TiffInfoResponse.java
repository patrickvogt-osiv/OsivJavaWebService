package ch.osiv.document;

import java.util.List;

import ch.osiv.helper.JsonSerializable;

/**
 * Tiff info response
 */
public class TiffInfoResponse
    extends JsonSerializable {

    private List<Integer> pageWidthList;
    private List<Integer> pageHeightList;
    private int           dpi;
    private String        errorMessage;
    private boolean       errorStatus;

    /**
     * 
     */
    public TiffInfoResponse() {}

    /**
     * @return errorStatus
     */
    public boolean isErrorStatus() {
        return errorStatus;
    }

    /**
     * @return errorMessage
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
     * @return dpi
     */
    public int getDpi() {
        return dpi;
    }

    /**
     * @param dpi
     */
    public void setDpi(int dpi) {
        this.dpi = dpi;
    }

    /**
     * @return pageWidthList
     */
    public List<Integer> getPageWidthList() {
        return pageWidthList;
    }

    /**
     * @param pageWidthList
     */
    public void setPageWidthList(List<Integer> pageWidthList) {
        this.pageWidthList = pageWidthList;
    }

    /**
     * @return pageHeightList
     */
    public List<Integer> getPageHeightList() {
        return pageHeightList;
    }

    /**
     * @param pageHeightList
     */
    public void setPageHeightList(List<Integer> pageHeightList) {
        this.pageHeightList = pageHeightList;
    }

    @Override
    public String toString() {

        if (errorStatus)
            return errorMessage;
        else
            return "File [dpi=" + dpi + "]";
    }

}
