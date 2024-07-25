package ch.osiv.helper;

/**
 * File Options class
 */
public class FileOptions {

    private String colorOption;

    /**
     * @return colorOption
     */
    public String getColorOption() {
        return colorOption;
    }

    /**
     * @param colorOption String <a href="#{@link}">{@link ColorMode} default -> color
     */
    public void setColorOption(String colorOption) {
        switch (colorOption) {
            case ColorMode.color:
                this.colorOption = colorOption;
                break;
            case ColorMode.greyscale:
                this.colorOption = colorOption;
                break;
            case ColorMode.monochrome:
                this.colorOption = colorOption;
                break;
            default:
                this.colorOption = ColorMode.color;
        }
    }

    private Integer resolution;

    /**
     * @return resolution
     */
    public Integer getResolution() {
        return resolution;
    }

    /**
     * @param resolution
     */
    public void setResolution(Integer resolution) {
        this.resolution = resolution;
    }

    /* Default Constructor */
    /**
     * 
     */
    public FileOptions() {
        this.colorOption = ColorMode.color;
        this.resolution  = 300;
    }

}
