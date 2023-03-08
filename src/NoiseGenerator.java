import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

public class NoiseGenerator {
    private static final int PERMUTATION_SIZE = 256;
    private static final int[] PERMS; // permutation table

    static {
        /*
        Generate the permutation table
         */
        int[] array = new int[PERMUTATION_SIZE * 2];
        /*
        The table must include every value up to its max size
         */
        for (int i = 0; i < PERMUTATION_SIZE; i++) {
            array[i] = i;
        }

        /*
        Now, the array must be shuffled
         */
        Random shuffler = new Random(2023);
        int index;
        for (int i = PERMUTATION_SIZE - 1; i > 0; i--) {
            index = shuffler.nextInt(i + 1);

            if (index != i) {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }

        /*
            The second half of the array needs to be a clone of the first half.
         */
        for (int i = 0; i < PERMUTATION_SIZE; i++) {
            array[i + PERMUTATION_SIZE] = array[i];
        }
        PERMS = array;
    }

    private final ArrayList<Octave> octaves = new ArrayList<>();

    public NoiseGenerator(float startFreq, float startAmp) {
        this.addOctave(startFreq, startAmp);
    }

    public NoiseGenerator addOctave(float frequency, float amplitude) {
        octaves.add(new Octave(frequency, amplitude));
        return this;
    }

    public float gen(float x, float y) {
        float result = 0;
        for (Octave octave : octaves) {
            result += (this.genOctave(x * octave.frequency, y * octave.frequency) * octave.amplitude);
        }
        return result;
    }

    private float genOctave(float x, float y) {
        float xf = (float) (x - Math.floor(x));
        float yf = (float) (y - Math.floor(y));

        /*
            Input values are said to be on an integer grid. Decimal values lie inside a square in that grid.
            For each of the corners where the input lies, a value is generated.
            This value is the dot product of 2 vectors.
            The first vector comes from a grid point to the input value.
         */
        Point2D topRight = new Point2D.Float(xf - 1.0F, yf - 1.0F);
        Point2D topLeft = new Point2D.Float(xf, yf - 1.0F);
        Point2D bottomRight = new Point2D.Float(xf - 1.0F, yf);
        Point2D bottomLeft = new Point2D.Float(xf, yf);

        /*
            The second vector should be "random", but consistent for each grid point.
            We use the permutation table to obtain it (RNG could be used, but is more expensive).

            First we use the bitwise & operator (in this case works like % 256) to obtain indexes for the permutation table.
            Notice 255 is 1 less than the length of the table. This is because we will also access permX + 1 and permY + 1.
         */
        int permX = (int) Math.floor(x) & 255;
        int permY = (int) Math.floor(y) & 255;

        int valueTopRight = PERMS[PERMS[permX + 1] + permY + 1];
        int valueTopLeft = PERMS[PERMS[permX] + permY + 1];
        int valueBottomRight = PERMS[PERMS[permX + 1] + permY];
        int valueBottomLeft = PERMS[PERMS[permX] + permY];

        /*
            Calculate the dot products. We finally have the special values for each grid corner.
         */
        float dotTopRight = dot(topRight, getVector(valueTopRight));
        float dotTopLeft = dot(topLeft, getVector(valueTopLeft));
        float dotBottomRight = dot(bottomRight, getVector(valueBottomRight));
        float dotBottomLeft = dot(bottomLeft, getVector(valueBottomLeft));

        /*
            Finally, we begin interpolating these values.
            Since we can only interpolate two numbers at a time, we interpolate 2 pairs and then interpolate their results.
            Also, using linear interpolation will produce sharp edges.
            We use the ease function to improve our inputs to the interpolation function.
         */
        float u = ease(xf);
        float v = ease(yf);
        return lerp(
                lerp(dotBottomLeft, dotTopLeft, v),
                lerp(dotBottomRight, dotTopRight, v),
                u
        );

    }

    private Point2D getVector(int value) {
        return switch (value & 3) { // in this case the & operator works like value % 4
            case 0 -> new Point2D.Float(1.0F, 1.0F);
            case 1 -> new Point2D.Float(-1.0F, 1.0F);
            case 2 -> new Point2D.Float(1.0F, -1.0F);
            default -> new Point2D.Float(-1.0F, -1.0F);
        };
    }

    private float dot(Point2D point1, Point2D point2) {
        return (float) ((point1.getX() * point2.getX()) + (point1.getY() * point2.getY()));
    }

    private float lerp(float num1, float num2, float lerpAmount) {
        return num1 + (num2 - num1) * lerpAmount;
    }

    /**
     * Ease function used by Ken Perlin. E6x^5 - 15x^4 + 10x^3.
     */
    private float ease(float num) {
        return (((6 * num) - 15) * num + 10) * num * num * num;
    }

    private class Octave {
        private final float frequency;
        private final float amplitude;

        private Octave(float frequency, float amplitude) {
            this.frequency = frequency;
            this.amplitude = amplitude;
        }
    }
}