package ch.osiv.document.tiff.parameter;

/**
 * Coordinate parameter class
 */
public class CoordinateParameter {

    Double startX;
    Double startY;
    Double width;
    Double height;

    /**
     * @return get coordinate start axe X
     */
    public Double getStartX() {
        return startX;
    }

    /**
     * @param startX set coordinate start axe X
     */
    public void setStartX(Double startX) {
        this.startX = startX;
    }

    /**
     * @return get coordinate start axe Y
     */
    public Double getStartY() {
        return startY;
    }

    /**
     * @param startY set coordinate start axe Y
     */
    public void setStartY(Double startY) {
        this.startY = startY;
    }

    /**
     * @return get width
     */
    public Double getWidth() {
        return width;
    }

    /**
     * @param width set width
     */
    public void setWidth(Double width) {
        this.width = width;
    }

    /**
     * @return get height
     */
    public Double getHeight() {
        return height;
    }

    /**
     * @param height set heigh
     */
    public void setHeight(Double height) {
        this.height = height;
    }

}
