import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class BiomeGeneration {
    private static final Random RANDOM = new Random();
    private static final double BIOME_SEPARATION = 100;
    private static final double NOISE_EFFECT = 50;

    private static final NoiseGenerator X_GENERATOR = new NoiseGenerator(1F / 80, 1)
            .addOctave(1F / 40, 0.5F)
            .addOctave(1F / 20, 0.25F)
            .addOctave(1F / 10, 0.125F)
            .addOctave(1F / 5, 0.125F / 2);
    private static final NoiseGenerator Y_GENERATOR = new NoiseGenerator(1F / 80, 1)
            .addOctave(1F / 40, 0.5F)
            .addOctave(1F / 20, 0.25F)
            .addOctave(1F / 10, 0.125F)
            .addOctave(1F / 5, 0.125F / 2);

    private static final boolean NOISE_MAP = true;

    public static void main(String[] args) {
        BufferedImage image = new BufferedImage(4096, 2048, BufferedImage.TYPE_INT_RGB);
        if (NOISE_MAP) {
            double[][] noiseValues = new double[image.getWidth()][image.getHeight()];
            double min = 0, max = 0;
            for (int x = 0; x < 10000; x++) {
                for (int y = 0; y < 10000; y++) {
                    if (Math.abs(X_GENERATOR.gen(x, y)) > Math.sqrt(0.5)) {
                        System.out.println(X_GENERATOR.gen(x, y));
                    }
                }
            }
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    double noise = X_GENERATOR.gen(x, y);
                    min = Math.min(noise, min);
                    max = Math.max(noise, max);
                    noiseValues[x][y] = noise;
                }
            }
            final double range = max - min;
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int color = (int) Math.round(((noiseValues[x][y] - min) / range) * 255);
                    image.setRGB(x, y, new Color(color, color, color).getRGB());
                }
            }
        } else {
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    image.setRGB(x, y, getColorAt(x, y).getRGB());
                }
            }
        }
        File out = new File("Biomes.png");
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException exception) {
            System.err.println("Could not write image");
            System.exit(-1);
        }
    }

    private static Color getColorAt(float x, float y) {
        double biomeX = x / BIOME_SEPARATION;
        double biomeY = y / BIOME_SEPARATION;

        BiomeOrigin[] biomes = new BiomeOrigin[] {
                new BiomeOrigin((int) Math.floor(biomeX), (int) Math.floor(biomeY)),
                new BiomeOrigin((int) Math.floor(biomeX), (int) Math.ceil(biomeY)),
                new BiomeOrigin((int) Math.ceil(biomeX), (int) Math.floor(biomeY)),
                new BiomeOrigin((int) Math.ceil(biomeX), (int) Math.ceil(biomeY))
        };

        double closestDist = Double.MAX_VALUE;
        Color currColor = null;
        for (BiomeOrigin biome : biomes) {
            double distX = x - biome.x + X_GENERATOR.gen(x, y) * NOISE_EFFECT;
            double distY = y - biome.y + Y_GENERATOR.gen(x, y) * NOISE_EFFECT;
            double currDist = Math.sqrt(distX * distX + distY * distY);
            if (currDist < closestDist) {
                closestDist = currDist;
                currColor = biome.color;
            }
        }
        return currColor;
    }

    private static class BiomeOrigin {
        private static final Color[] BIOME_COLORS = new Color[] {
                new Color(100,255, 30), //plains
                new Color(0, 255, 0), //forest
                new Color(255, 255, 0), //desert
                new Color(255, 255, 255), //snow
                new Color(0, 150, 0), //jungle
                new Color(0, 0, 200) //ocean
        };
        double x, y;
        Color color;

        private BiomeOrigin(int x, int y) {
            RANDOM.setSeed(x);
            long random1 = RANDOM.nextLong();
            RANDOM.setSeed(y);
            long random2 = RANDOM.nextLong();
            RANDOM.setSeed(random1 * random2);

            this.x = x * BIOME_SEPARATION + (RANDOM.nextDouble(BIOME_SEPARATION) - BIOME_SEPARATION / 2);
            this.y = y * BIOME_SEPARATION + (RANDOM.nextDouble(BIOME_SEPARATION) - BIOME_SEPARATION / 2);
            color = BIOME_COLORS[RANDOM.nextInt(BIOME_COLORS.length)];
        }
    }
}