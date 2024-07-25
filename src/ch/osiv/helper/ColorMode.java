package ch.osiv.helper;

import com.aspose.words.ImageColorMode;

/**
 * Constant class for color modes
 */
@SuppressWarnings("javadoc")
public class ColorMode {

    public static final String color      = "color";
    public static final String greyscale  = "greyscale";
    public static final String monochrome = "monochrome";

    /**
     * @param colorMode Convert the color mode to the Aspose.Words
     *                  <a href="#{@link}">{@link ImageColorMode}
     * @return int value corresponding to color mode
     */
    public static int toImageColorMode(String colorMode) {
        switch (colorMode) {
            case ColorMode.color:
                return ImageColorMode.NONE;
            case ColorMode.greyscale:
                return ImageColorMode.GRAYSCALE;
            case ColorMode.monochrome:
                return ImageColorMode.BLACK_AND_WHITE;
            default:
                return ImageColorMode.NONE;
        }

    }

    /**
     * Check if a input string is a valid colorMode option
     * 
     * @param colorMode
     * @return true if colorMode, false otherwise
     */
    public static boolean isImageColorModeOption(String colorMode) {

        switch (colorMode) {
            case ColorMode.color:
                return true;
            case ColorMode.greyscale:
                return true;
            case ColorMode.monochrome:
                return true;
            default:
                return false;
        }
    }
}
