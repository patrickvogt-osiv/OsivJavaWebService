package ch.osiv.document.tiff;

import java.awt.Color;

/**
 * Color 3i class
 */
public class Color3i {

    private int r, g, b;

    /**
     * @param c
     */
    public Color3i(int c) {
        Color color = new Color(c);
        this.r = color.getRed();
        this.g = color.getGreen();
        this.b = color.getBlue();
    }

    /**
     * @param r
     * @param g
     * @param b
     */
    public Color3i(int r,
                   int g,
                   int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /**
     * @param o
     * @return Color3i class
     */
    public Color3i add(Color3i o) {
        return new Color3i(r + o.r, g + o.g, b + o.b);
    }

    /**
     * @param o
     * @return Color3i class
     */
    public Color3i sub(Color3i o) {
        return new Color3i(r - o.r, g - o.g, b - o.b);
    }

    /**
     * @param d
     * @return Color3i class
     */
    public Color3i mul(double d) {
        return new Color3i((int) (d * r), (int) (d * g), (int) (d * b));
    }

    /**
     * @param o
     * @return int representing the greyscale value
     */
    public int diff(Color3i o) {
        int Rdiff           = o.r - r;
        int Gdiff           = o.g - g;
        int Bdiff           = o.b - b;
        int distanceSquared = Rdiff * Rdiff + Gdiff * Gdiff + Bdiff * Bdiff;
        return distanceSquared;
    }

    /**
     * @return int representing the RGB value
     */
    public int toRGB() {
        return toColor().getRGB();
    }

    /**
     * @return Color object
     */
    public Color toColor() {
        return new Color(clamp(r), clamp(g), clamp(b));
    }

    /**
     * @param c
     * @return int between 0 and 255
     */
    public int clamp(int c) {
        return Math.max(0, Math.min(255, c));
    }

}
