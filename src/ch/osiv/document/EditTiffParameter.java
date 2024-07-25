package ch.osiv.document;

import ch.osiv.helper.ColorMode;

/**
 * Parameter class used for tiff editing options.
 * Includes options for color filter and border size
 *
 * @author tca
 */
public class EditTiffParameter {

    private boolean addFilter;
    private boolean addBorder;
    private int     borderSize;
    private String  colorOption;

    /**
     * @return the addFilter
     */
    public boolean isAddFilter() {
        return addFilter;
    }

    /**
     * @param addFilter the addFilter to set
     */
    public void setAddFilter(boolean addFilter) {
        this.addFilter = addFilter;
    }

    /**
     * @return the addBorder
     */
    public boolean isAddBorder() {
        return addBorder;
    }

    /**
     * @param addBorder the addBorder to set
     */
    public void setAddBorder(boolean addBorder) {
        this.addBorder = addBorder;
    }

    /**
     * @return the borderSize
     */
    public int getBorderSize() {
        return borderSize;
    }

    /**
     * @param borderSize the borderSize to set
     */
    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
    }

    /**
     * @return the colorOption
     */
    public String getColorOption() {
        return colorOption;
    }

    /**
     * @param colorOption the colorOption to set
     */
    public void setColorOption(String colorOption) {
        this.colorOption = colorOption;
    }

    /**
     * @param addFilter   boolean
     * @param addBorder   boolean
     * @param borderSize  int
     * @param colorOption String
     */
    public EditTiffParameter(boolean addFilter,
                             boolean addBorder,
                             int borderSize,
                             String colorOption) {
        this.addFilter   = addFilter;
        this.addBorder   = addBorder;
        this.borderSize  = borderSize;
        this.colorOption = colorOption;
    }

    /**
     * @param borderSize  int
     * @param colorOption String
     */
    public EditTiffParameter(int borderSize,
                             String colorOption) {
        this.borderSize  = borderSize;
        this.colorOption = colorOption;

        if (colorOption.equals(ColorMode.color) || !ColorMode.isImageColorModeOption(colorOption)) {
            this.addFilter = false;
        } else {
            this.addFilter   = true;
            this.colorOption = colorOption;
        }

        if (borderSize > 0) {
            this.addBorder  = true;
            this.borderSize = borderSize;
        }
    }

}
