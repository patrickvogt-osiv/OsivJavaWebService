package ch.osiv.document.convert;

/**
 * DocumentColor enum
 *
 * @author Arno van der Ende
 */

@SuppressWarnings("javadoc")
public enum DocumentColor {

    BLACK_AND_WHITE,
    GREY,
    JPEG,
    RGB;

    /**
     * Checks if the given value is a valid Enum
     *
     * @param documentColor
     * @return true/false
     */
    public static boolean exists(String documentColor) {
        for (DocumentColor enumValue : DocumentColor.values()) {
            if (enumValue.name().equals(documentColor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a given value to the Enum value
     *
     * @param documentColor
     * @return DocumentColor
     */
    public static DocumentColor fromString(String documentColor) {

        if (documentColor == null) {
            return null;
        }

        if (documentColor.equalsIgnoreCase("GRAY")) {
            return DocumentColor.GREY;
        }

        for (DocumentColor enumValue : DocumentColor.values()) {
            if (enumValue.name().equals(documentColor)) {
                return enumValue;
            }
        }

        return null;
    }
}
