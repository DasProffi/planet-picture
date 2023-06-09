import java.util.Arrays;
import java.util.Dictionary;

// Class for helping to manipulate integer color values
public class ColorUtil {
    public static final int BLACK = 255 << 24;

    static int getAlpha(int color) {
        return color >>> 24;
    }

    static int getRed(int color) {
        return (color << 8) >>> 24;
    }

    static int getGreen(int color) {
        return (color << 16) >>> 24;
    }

    static int getBlue(int color) {
        return (color << 24) >>> 24;
    }

    static int setAlpha(int color, int value) {
        return value << 24 | getRed(color) << 16 | getBlue(color) << 8 | getGreen(color);
    }

    static int setRed(int color, int value) {
        return getAlpha(color) << 24 | value << 16 | getBlue(color) << 8 | getGreen(color);
    }

    static int setGreen(int color, int value) {
        return getAlpha(color) << 24 | getRed(color) << 16 | value << 8 | getGreen(color);
    }

    static int setBlue(int color, int value) {
        return getAlpha(color) << 24 | getRed(color) << 16 | getBlue(color) << 8 | value;
    }

    static int alpha(int value) {
        assert value >= 0 && value < 256;
        return value << 24;
    }

    static int red(int value) {
        assert value >= 0 && value < 256;
        return value << 16;
    }

    static int green(int value) {
        assert value >= 0 && value < 256;
        return value << 8;
    }

    static int blue(int value) {
        assert value >= 0 && value < 256;
        return value;
    }

    static int changeLighting(int color, double multiplier) {
        return alpha(getAlpha(color)) | red(((int) (getRed(color) * multiplier) % 256)) | green((int) (getGreen(color) * multiplier) % 256) | blue((int) (getBlue(color) * multiplier) % 256);
    }

    static int grayValue(int value) {
        return ColorUtil.BLACK | value << 16 | value << 8 | value;
    }

    static int calcAverage(int color1, int color2) {
        return calcAverage(color1, color2, 0.5);
    }

    // Formula: root((x^2+y^2)/2)
    static int calcAverage(int color1, int color2, double multiplier) {
        double multiplier1 = multiplier > 0 && multiplier <= 1 ? multiplier : 0.5;
        double multiplier2 = 1 - multiplier1;
        int alpha = (int) Math.sqrt(Math.pow(getAlpha(color1), 2) * multiplier1 + Math.pow(getAlpha(color2), 2) * multiplier2);
        int red = (int) Math.sqrt(Math.pow(getRed(color1), 2) * multiplier1 + Math.pow(getRed(color2), 2) * multiplier2);
        int blue = (int) Math.sqrt(Math.pow(getGreen(color1), 2) * multiplier1 + Math.pow(getGreen(color2), 2) * multiplier2);
        int green = (int) Math.sqrt(Math.pow(getBlue(color1), 2) * multiplier1 + Math.pow(getBlue(color2), 2) * multiplier2);
        return alpha << 24 | red << 16 | blue << 8 | green;
    }

    static int gradient(int[] colors, float value, float range) {
        assert colors.length >= 2;
        value = Math.max(0, value);
        float positionPercentage = value / range;
        int lower = Math.min((int) Math.floor((colors.length) * positionPercentage), colors.length);
        int upper = Math.min((int) Math.ceil((colors.length) * positionPercentage), colors.length);
        float percentage = (positionPercentage - (float) lower / colors.length) * colors.length;
        return calcAverage(colors[Math.max(0, upper - 1)], colors[Math.max(0, lower - 1)], smoothStep(percentage));
    }

    // A Function to smooth values between 0 and 1
    private static double smoothStep(double value) {
        return 3 * Math.pow(value, 2) - 2 * Math.pow(value, 3);
    }

    static int calcAverageList(int... colors) {
        if (colors.length == 0) {
            return ColorUtil.BLACK;
        }
        int alpha = (int) Math.sqrt((float) (Arrays.stream(colors).map(n -> (int) Math.pow(getAlpha(n), 2)).sum() / colors.length));
        int red = (int) Math.sqrt((float) (Arrays.stream(colors).map(n -> (int) Math.pow(getRed(n), 2)).sum() / colors.length));
        int green = (int) Math.sqrt((float) (Arrays.stream(colors).map(n -> (int) Math.pow(getGreen(n), 2)).sum() / colors.length));
        int blue = (int) Math.sqrt((float) (Arrays.stream(colors).map(n -> (int) Math.pow(getBlue(n), 2)).sum() / colors.length));
        return alpha(alpha) | red(red) | blue(blue) | green(green);
    }

    static int calcAverageListWeighted(Tuple<Integer, Float>... weightedColors) {
        if (weightedColors.length == 0) {
            return ColorUtil.BLACK;
        }
        int alpha = (int) Math.sqrt(Arrays.stream(weightedColors).mapToDouble(n -> Math.pow(getAlpha(n.first), 2) * n.second).sum());
        int red = (int) Math.sqrt(Arrays.stream(weightedColors).mapToDouble(n -> Math.pow(getRed(n.first), 2) * n.second).sum());
        int green = (int) Math.sqrt(Arrays.stream(weightedColors).mapToDouble(n -> Math.pow(getGreen(n.first), 2) * n.second).sum());
        int blue = (int) Math.sqrt(Arrays.stream(weightedColors).mapToDouble(n -> Math.pow(getBlue(n.first), 2) * n.second).sum());

        alpha = Math.min(255, Math.max(alpha, 0));
        red = Math.min(255, Math.max(red, 0));
        green = Math.min(255, Math.max(green, 0));
        blue = Math.min(255, Math.max(blue, 0));
        return alpha(alpha) | red(red) | blue(blue) | green(green);
    }
}