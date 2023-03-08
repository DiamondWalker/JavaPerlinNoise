import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class BiomeGeneration {

    private static final NoiseGenerator X_GENERATOR = new NoiseGenerator(1F / 80, 1);

    public static void main(String[] args) {
        for (int x = 0; x < 10000; x++) {
            for (int y = 0; y < 10000; y++) {
                if (Math.abs(X_GENERATOR.gen(x, y)) > Math.sqrt(0.5)) {
                    System.out.println(X_GENERATOR.gen(x, y));
                }
            }
        }
    }
}