package ch.osiv.document;

/**
 * Base annotation class
 *
 * @author Arno van der Ende
 */
public abstract class AnnotationBase {

    private double startX;
    private double startY;
    private double endX;
    private double endY;
    private String coordinate;
    private double scalingFactor;
    private int    fontSizeInPixel;

    /**
     * Constructor
     *
     * @param scalingFactor
     */
    protected AnnotationBase(Double scalingFactor) {
        this.scalingFactor = (scalingFactor != null && scalingFactor != 0.0) ? scalingFactor
                                                                             : 1.0;
    }

    /**
     * @return the startX
     */
    public double getStartX() {
        return Math.round(startX / scalingFactor);
    }

    /**
     * @param startX the startX to set
     */
    protected void setStartX(double startX) {
        this.startX = startX;
    }

    /**
     * @return the startY
     */
    public double getStartY() {
        return Math.round(startY / scalingFactor);
    }

    /**
     * @param startY the startY to set
     */
    protected void setStartY(double startY) {
        this.startY = startY;
    }

    /**
     * @return the endX
     */
    public double getEndX() {
        return Math.round(endX / scalingFactor);
    }

    /**
     * @param endX the endX to set
     */
    protected void setEndX(double endX) {
        this.endX = endX;
    }

    /**
     * @return the endY
     */
    public double getEndY() {
        return Math.round(endY / scalingFactor);
    }

    /**
     * @param endY the endY to set
     */
    protected void setEndY(double endY) {
        this.endY = endY;
    }

    /**
     * @return the fontSizeInPixel
     */
    public int getFontSizeInPixel() {
        return (int) Math.round(fontSizeInPixel / scalingFactor);
    }

    /**
     * @param fontSizeInPixel the fontSizeInPixel to set
     */
    public void setFontSizeInPixel(int fontSizeInPixel) {
        this.fontSizeInPixel = fontSizeInPixel;
    }

    /**
     * @return the coordinate
     */
    public String getCoordinate() {
        return coordinate;
    }

    /**
     * @param coordinate the coordinate to set
     */
    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

}
