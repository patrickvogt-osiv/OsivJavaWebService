package ch.osiv.helper;

import java.awt.image.BufferedImage;

import ch.osiv.document.tiff.Color3i;

/**
 * All Dithering methode
 */
public class DitheringUtils {

    /**
     * @param image
     * @return buffer image
     */
    public static BufferedImage FloydSteinbergDithering(BufferedImage image) {
        Color3i[] palette = new Color3i[] {
                new Color3i(0, 0, 0), new Color3i(255, 255, 255)
        };

        int width  = image.getWidth();
        int height = image.getHeight();

        Color3i[][] buffer = new Color3i[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer[x][y] = new Color3i(image.getRGB(x, y));
            }
        }

        int yPlus1  = 0;
        int yMinus1 = 0;
        int xPlus1  = 0;

        for (int y = 1; y < height - 1; y++) {

            yPlus1  = y + 1;
            yMinus1 = y - 1;

            for (int x = 0; x < width - 1; x++) {
                Color3i nem = findClosestPaletteColor(buffer[x][y], palette);
                image.setRGB(x, y, nem.toColor().getRGB());

                Color3i error = buffer[x][y].sub(nem);

                xPlus1 = x + 1;

                if (yPlus1 < width)
                    buffer[x][yPlus1] = buffer[x][yPlus1].add(error.mul(7. / 16));
                if (yMinus1 >= 0 && xPlus1 < height)
                    buffer[xPlus1][yMinus1] = buffer[xPlus1][yMinus1].add(error.mul(3. / 16));
                if (xPlus1 < height)
                    buffer[xPlus1][y] = buffer[xPlus1][y].add(error.mul(5. / 16));
                if (yPlus1 < width && xPlus1 < height)
                    buffer[xPlus1][yPlus1] = buffer[xPlus1][yPlus1].add(error.mul(1. / 16));
            }
        }

        return image;
    }

    /**
     * @param image
     * @return buffer image
     */
    public static BufferedImage BurkesDithering(BufferedImage image) {
        Color3i[] palette = new Color3i[] {
                new Color3i(0, 0, 0), new Color3i(255, 255, 255)
        };

        int width  = image.getWidth();
        int height = image.getHeight();

        Color3i[][] buffer = new Color3i[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer[x][y] = new Color3i(image.getRGB(x, y));
            }
        }

        int yPlus1  = 0;
        int yPlus2  = 0;
        int yMinus2 = 0;
        int yMinus1 = 0;
        int xPlus1  = 0;

        for (int y = 2; y < height - 2; y++) {

            yPlus1  = y + 1;
            yPlus2  = y + 2;
            yMinus1 = y - 1;
            yMinus2 = y - 2;

            for (int x = 2; x < width - 1; x++) {
                Color3i nem = findClosestPaletteColor(buffer[x][y], palette);
                image.setRGB(x, y, nem.toColor().getRGB());

                Color3i error = buffer[x][y].sub(nem);

                xPlus1 = x + 1;

                if (yPlus1 < width)
                    buffer[x][yPlus1] = buffer[x][yPlus1].add(error.mul(8. / 32));
                if (yPlus2 < width)
                    buffer[x][yPlus2] = buffer[x][yPlus2].add(error.mul(4. / 32));
                if (yMinus2 < width && xPlus1 < height)
                    buffer[xPlus1][yMinus2] = buffer[xPlus1][yMinus2].add(error.mul(2. / 32));
                if (yMinus1 < width && xPlus1 < height)
                    buffer[xPlus1][yMinus1] = buffer[xPlus1][yMinus1].add(error.mul(4. / 32));
                if (xPlus1 < height)
                    buffer[xPlus1][y] = buffer[xPlus1][y].add(error.mul(8. / 32));
                if (yPlus1 < width && xPlus1 < height)
                    buffer[xPlus1][yPlus1] = buffer[xPlus1][yPlus1].add(error.mul(4. / 32));
                if (yPlus2 < width && xPlus1 < height)
                    buffer[xPlus1][yPlus2] = buffer[xPlus1][yPlus2].add(error.mul(2. / 32));
            }
        }

        return image;
    }

    /**
     * @param image
     * @return buffer image
     */
    public static BufferedImage JarvisJudiceNinkeDithering(BufferedImage image) {
        Color3i[] palette = new Color3i[] {
                new Color3i(0, 0, 0), new Color3i(255, 255, 255)
        };

        int width  = image.getWidth();
        int height = image.getHeight();

        Color3i[][] buffer = new Color3i[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer[x][y] = new Color3i(image.getRGB(x, y));
            }
        }

        int yPlus1  = 0;
        int yPlus2  = 0;
        int yMinus2 = 0;
        int yMinus1 = 0;
        int xPlus1  = 0;
        int xPlus2  = 0;

        for (int y = 2; y < height; y++) {

            yPlus1  = y + 1;
            yPlus2  = y + 2;
            yMinus1 = y - 1;
            yMinus2 = y - 2;

            for (int x = 0; x < width - 2; x++) {
                Color3i nem = findClosestPaletteColor(buffer[x][y], palette);
                image.setRGB(x, y, nem.toColor().getRGB());

                Color3i error = buffer[x][y].sub(nem);

                xPlus1 = x + 1;
                xPlus2 = x + 2;

                if (yPlus1 < width)
                    buffer[x][yPlus1] = buffer[x][yPlus1].add(error.mul(7. / 48));
                if (yPlus2 < width)
                    buffer[x][yPlus2] = buffer[x][yPlus2].add(error.mul(5. / 48));

                if (yMinus2 < width && xPlus1 < height)
                    buffer[xPlus1][yMinus2] = buffer[xPlus1][yMinus2].add(error.mul(3. / 48));
                if (yMinus1 < width && xPlus1 < height)
                    buffer[xPlus1][yMinus1] = buffer[xPlus1][yMinus1].add(error.mul(5. / 48));
                if (xPlus1 < height)
                    buffer[xPlus1][y] = buffer[xPlus1][y].add(error.mul(7. / 48));
                if (yPlus1 < width && xPlus1 < height)
                    buffer[xPlus1][yPlus1] = buffer[xPlus1][yPlus1].add(error.mul(5. / 48));
                if (yPlus2 < width && xPlus1 < height)
                    buffer[xPlus1][yPlus2] = buffer[xPlus1][yPlus2].add(error.mul(3. / 48));

                if (yMinus2 < width && x + 2 < height)
                    buffer[xPlus2][yMinus2] = buffer[xPlus2][yMinus2].add(error.mul(1. / 48));
                if (yMinus1 < width && xPlus2 < height)
                    buffer[xPlus2][yMinus1] = buffer[xPlus2][yMinus1].add(error.mul(3. / 48));
                if (xPlus2 < height)
                    buffer[xPlus2][y] = buffer[xPlus2][y].add(error.mul(5. / 48));
                if (yPlus1 < width && xPlus2 < height)
                    buffer[xPlus2][yPlus1] = buffer[xPlus2][yPlus1].add(error.mul(3. / 48));
                if (yPlus2 < width && xPlus2 < height)
                    buffer[xPlus2][yPlus2] = buffer[xPlus2][yPlus2].add(error.mul(1. / 48));
            }
        }

        return image;
    }

    /**
     * @param image
     * @return buffer image
     */
    public static BufferedImage StuckiDithering(BufferedImage image) {
        Color3i[] palette = new Color3i[] {
                new Color3i(0, 0, 0), new Color3i(255, 255, 255)
        };

        int width  = image.getWidth();
        int height = image.getHeight();

        Color3i[][] buffer = new Color3i[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer[x][y] = new Color3i(image.getRGB(x, y));
            }
        }

        int yPlus1  = 0;
        int yPlus2  = 0;
        int yMinus1 = 0;
        int yMinus2 = 0;
        int xPlus1  = 0;
        int xPlus2  = 0;

        for (int y = 2; y < height - 2; y++) {

            yPlus1  = y + 1;
            yPlus2  = y + 2;
            yMinus1 = y - 1;
            yMinus2 = y - 2;

            for (int x = 2; x < width - 2; x++) {
                Color3i nem = findClosestPaletteColor(buffer[x][y], palette);
                image.setRGB(x, y, nem.toColor().getRGB());

                Color3i error = buffer[x][y].sub(nem);

                xPlus1 = x + 1;
                xPlus2 = x + 2;

                if (yPlus1 < width)
                    buffer[x][yPlus1] = buffer[x][yPlus1].add(error.mul(8. / 42));
                if (yPlus2 < width)
                    buffer[x][yPlus2] = buffer[x][yPlus2].add(error.mul(4. / 42));

                if (yMinus2 < width && xPlus1 < height)
                    buffer[xPlus1][yMinus2] = buffer[xPlus1][yMinus2].add(error.mul(2. / 42));
                if (yMinus1 < width && xPlus1 < height)
                    buffer[xPlus1][yMinus1] = buffer[xPlus1][yMinus1].add(error.mul(4. / 42));
                if (xPlus1 < height)
                    buffer[xPlus1][y] = buffer[xPlus1][y].add(error.mul(8. / 42));
                if (yPlus1 < width && xPlus1 < height)
                    buffer[xPlus1][yPlus1] = buffer[xPlus1][yPlus1].add(error.mul(4. / 42));
                if (yPlus2 < width && xPlus1 < height)
                    buffer[xPlus1][yPlus2] = buffer[xPlus1][yPlus2].add(error.mul(2. / 42));

                if (yMinus2 < width && xPlus2 < height)
                    buffer[xPlus2][yMinus2] = buffer[xPlus2][yMinus2].add(error.mul(1. / 42));
                if (yMinus1 < width && xPlus2 < height)
                    buffer[xPlus2][yMinus1] = buffer[xPlus2][yMinus1].add(error.mul(2. / 42));
                if (xPlus2 < height)
                    buffer[xPlus2][y] = buffer[xPlus2][y].add(error.mul(4. / 42));
                if (yPlus1 < width && xPlus2 < height)
                    buffer[xPlus2][yPlus1] = buffer[xPlus2][yPlus1].add(error.mul(2. / 42));
                if (yPlus2 < width && xPlus2 < height)
                    buffer[xPlus2][yPlus2] = buffer[xPlus2][yPlus2].add(error.mul(1. / 42));
            }
        }

        return image;
    }

    /**
     * @param image
     * @return buffer image
     */
    public static BufferedImage SierraLiteDithering(BufferedImage image) {
        Color3i[] palette = new Color3i[] {
                new Color3i(0, 0, 0), new Color3i(255, 255, 255)
        };

        int width  = image.getWidth();
        int height = image.getHeight();

        Color3i[][] buffer = new Color3i[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer[x][y] = new Color3i(image.getRGB(x, y));
            }
        }

        int yPlus1  = 0;
        int yMinus1 = 0;
        int xPlus1  = 0;

        for (int y = 1; y < height - 1; y++) {

            yPlus1  = y + 1;
            yMinus1 = y - 1;

            for (int x = 0; x < width - 1; x++) {
                Color3i nem = findClosestPaletteColor(buffer[x][y], palette);
                image.setRGB(x, y, nem.toColor().getRGB());

                Color3i error = buffer[x][y].sub(nem);

                xPlus1 = x + 1;

                if (yPlus1 < width)
                    buffer[x][yPlus1] = buffer[x][yPlus1].add(error.mul(2. / 4));
                if (yMinus1 < width && xPlus1 < height)
                    buffer[xPlus1][yMinus1] = buffer[xPlus1][yMinus1].add(error.mul(1. / 4));
                if (xPlus1 < height)
                    buffer[xPlus1][y] = buffer[xPlus1][y].add(error.mul(1. / 4));

            }
        }

        return image;
    }

    /**
     * @param image
     * @return buffer image
     */
    public static BufferedImage LatticeBoltzmannDithering(BufferedImage image) {
        Color3i[] palette = new Color3i[] {
                new Color3i(0, 0, 0), new Color3i(255, 255, 255)
        };

        int width  = image.getWidth();
        int height = image.getHeight();

        Color3i[][] buffer = new Color3i[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer[x][y] = new Color3i(image.getRGB(x, y));
            }
        }

        int yPlus1  = 0;
        int yMinus1 = 0;
        int xPlus1  = 0;
        int xMinus1 = 0;

        for (int y = 1; y < image.getHeight() - 1; y++) {

            yPlus1  = y + 1;
            yMinus1 = y - 1;

            for (int x = 1; x < image.getWidth() - 1; x++) {
                Color3i nem = findClosestPaletteColor(buffer[x][y], palette);
                image.setRGB(x, y, nem.toColor().getRGB());

                xPlus1  = x + 1;
                xMinus1 = x - 1;

                Color3i error = buffer[x][y].sub(nem);
                if (yMinus1 < width && yMinus1 < height)
                    buffer[xMinus1][yMinus1] = buffer[xMinus1][yMinus1].add(error.mul(1. / 36));
                if (yPlus1 < width && xMinus1 < height)
                    buffer[xMinus1][yPlus1] = buffer[xMinus1][yPlus1].add(error.mul(1. / 36));
                if (yMinus1 < width && xPlus1 < height)
                    buffer[xPlus1][yMinus1] = buffer[xPlus1][yMinus1].add(error.mul(1. / 36));
                if (yPlus1 < width && xPlus1 < height)
                    buffer[xPlus1][yPlus1] = buffer[xPlus1][yPlus1].add(error.mul(1. / 36));

                if (yPlus1 < width)
                    buffer[x][yPlus1] = buffer[x][yPlus1].add(error.mul(1. / 9));
                if (yMinus1 < width)
                    buffer[x][yPlus1] = buffer[x][yPlus1].add(error.mul(1. / 9));
                if (xPlus1 < height)
                    buffer[xPlus1][y] = buffer[xPlus1][y].add(error.mul(1. / 9));
                if (xMinus1 < height)
                    buffer[xPlus1][y] = buffer[xPlus1][y].add(error.mul(1. / 9));

            }
        }

        return image;
    }

    private static Color3i findClosestPaletteColor(Color3i match,
                                                   Color3i[] palette) {

        int i = 0;
        for (Color3i color : palette) {
            if (color.diff(match) < palette[i].diff(match)) {
                i = i + 1;
            }
        }

        return palette[i];
    }
}
