import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

// Class for Generating the image
public class ImageGenerator {
    private RandomGenerator randomGenerator = new RandomGenerator();
    private final int SEED_MAXIMUM = (int) (Math.pow(10, 8) - 1);
    private int width = 1000; // in pixel
    private int height = 1000; // in pixel
    final int minimumPlanetSize = 100;
    final int maximumPlanetSize = 400;
    private BufferedImage image;

    public BufferedImage createBySeed(int seed) {
        BufferedImage img = null;
        if (seed <= SEED_MAXIMUM) {
            randomGenerator = new RandomGenerator(seed);
            img = makeImg();
        }
        return img;
    }

    public int getSeed() {
        return randomGenerator.getSeed();
    }

    public void rerollSeed() {
        randomGenerator = new RandomGenerator(randomGenerator.next(SEED_MAXIMUM));
    }

    public BufferedImage makeImg() {
        System.out.println("Generating Seed: " + getSeed());
        long startTimeTotal = System.nanoTime();
        int[][] pixels = new int[width][height];

        // Coordinates of planet
        int planetMidX = randomGenerator.next(width);
        int planetMidY = randomGenerator.next(height);

        // set the radius
        int radius = randomGenerator.range(minimumPlanetSize, maximumPlanetSize);

        // Random numbers
        long startTime = System.nanoTime();

        final int planetDistance = randomGenerator.range(40, 100);
        final int nebularDistance = randomGenerator.range(120, 300);
        int[][] planetSmoothedPixels = layeredSmoothedNumbers(5, planetDistance);
        int[][] nebularSmoothedPixels = layeredSmoothedNumbers(5, nebularDistance);
        int planetHighestNumber = planetSmoothedPixels[0][0];
        int planetSmallestNumber = planetSmoothedPixels[0][0];
        int nebularHighestNumber = nebularSmoothedPixels[0][0];
        int nebularSmallestNumber = nebularSmoothedPixels[0][0];

        long endTime = System.nanoTime();
        long randomNumbersTimeMs = (endTime - startTime) / 1_000_000;
        System.out.println("GenerateRandom: " + randomNumbersTimeMs + " ms");


        // find biggest and smallest number
        startTime = System.nanoTime();
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                if (planetSmoothedPixels[x][y] < planetSmallestNumber) {
                    planetSmallestNumber = planetSmoothedPixels[x][y];
                }
                if (planetSmoothedPixels[x][y] > planetHighestNumber) {
                    planetHighestNumber = planetSmoothedPixels[x][y];
                }
                if (nebularSmoothedPixels[x][y] < nebularSmallestNumber) {
                    nebularSmallestNumber = nebularSmoothedPixels[x][y];
                }
                if (nebularSmoothedPixels[x][y] > nebularHighestNumber) {
                    nebularHighestNumber = nebularSmoothedPixels[x][y];
                }
            }
        }
        endTime = System.nanoTime();
        long findRangeTimeMs = (endTime - startTime) / 1_000_000;
        System.out.println("FindRanges: " + findRangeTimeMs + " ms");


        // Background
        // Nebular
        startTime = System.nanoTime();
        int nebularColorAmount = randomGenerator.range(15, 20);
        int[] nebularColors = new int[nebularColorAmount];
        nebularColors[0] = ColorUtil.BLACK | ColorUtil.red(randomGenerator.range(10, 20)) | ColorUtil.green(randomGenerator.range(10, 20)) | ColorUtil.blue(randomGenerator.range(10, 20));
        for (int z = 1; z < nebularColorAmount; z++) {
            int before = nebularColors[z - 1];
            nebularColors[z] = ColorUtil.BLACK | ColorUtil.red(ColorUtil.getRed(before) + randomGenerator.range(3, 5)) | ColorUtil.green(ColorUtil.getGreen(before) + randomGenerator.range(3, 5)) | ColorUtil.blue(ColorUtil.getBlue(before) + randomGenerator.range(4, 6));
        }
        int nebularPixelRange = nebularHighestNumber - nebularSmallestNumber;
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                int colorIndex = (nebularSmoothedPixels[x][y] - nebularSmallestNumber) * nebularColorAmount / (nebularPixelRange);
                if (colorIndex > nebularColors.length - 1) {
                    colorIndex = nebularColors.length - 1;
                }
                int currentColor = nebularColors[colorIndex];
                pixels[x][y] = currentColor;
            }
        }
        endTime = System.nanoTime();
        long backgroundTimeMs = (endTime - startTime) / 1_000_000;
        System.out.println("Background: " + backgroundTimeMs + " ms");


        //  Stars
        startTime = System.nanoTime();
        int starAmount = randomGenerator.range(50, 300);
        for (int z = 0; z < starAmount; z++) {
            int starRadius = randomGenerator.next(7) + 1;
            int midX = randomGenerator.next(width);
            int midY = randomGenerator.next(height);
            int color = (ColorUtil.BLACK) | (255 - randomGenerator.next(40) << 16) | (255 - randomGenerator.next(40) << 8) | 255 - randomGenerator.next(40);

            for (int x = -starRadius; x <= starRadius; x++) {
                int fx = (int) Math.round(Math.sqrt((Math.pow(starRadius, 2) - Math.pow(x, 2))));
                for (int y = -fx; y <= fx; y++) {
                    if (midX + x >= 0 && midX + x < width && midY + y >= 0 && midY + y < height) {
                        pixels[midX + x][midY + y] = color;
                    }
                    if (midX + y >= 0 && midX + y < width && midY + x >= 0 && midY + x < height) {
                        pixels[midX + y][midY + x] = color;
                    }
                }
            }
        }
        endTime = System.nanoTime();
        long starsTimeMs = (endTime - startTime) / 1_000_000;
        System.out.println("Stars: " + starsTimeMs + " ms");


        // Planet
        startTime = System.nanoTime();
        final int minimumColors = 3;
        final int maximumColors = 8;
        int colorAmount = randomGenerator.range(minimumColors, maximumColors);
        int[] colors = new int[colorAmount];
        for (int z = 0; z < colors.length; z++) {
            int color = (ColorUtil.BLACK) | (randomGenerator.next(255) << 16) | (randomGenerator.next(255) << 8) | randomGenerator.next(255);
            colors[z] = color;
        }

        // Paint Planet
        for (int x = -radius; x <= radius; x++) {
            int fx = (int) Math.round(Math.sqrt((Math.pow(radius, 2) - Math.pow(x, 2))));
            for (int y = -fx; y <= fx; y++) {
                if (planetMidX + x >= 0 && planetMidX + x < width && planetMidY + y >= 0 && planetMidY + y < height) {
                    int colorIndex = (planetSmoothedPixels[planetMidX + x][planetMidY + y] - planetSmallestNumber)
                            * colorAmount / (planetHighestNumber - planetSmallestNumber);
                    if (colorIndex > colors.length - 1) {
                        colorIndex = colors.length - 1;
                    }
                    int curColor = colors[colorIndex];
                    pixels[planetMidX + x][planetMidY + y] = curColor;
                }
            }
        }
        endTime = System.nanoTime();
        long planetTimeMs = (endTime - startTime) / 1_000_000;
        System.out.println("Planet: " + planetTimeMs + " ms");


        // smooth and blur
        startTime = System.nanoTime();
        pixels = smooth(pixels);
        endTime = System.nanoTime();
        long smoothTimeMs = (endTime - startTime) / 1_000_000;
        System.out.println("Smooth: " + smoothTimeMs + " ms");


        // Map array to Image
        startTime = System.nanoTime();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                img.setRGB(x, y, pixels[x][y]);
            }
        }
        endTime = System.nanoTime();
        long toImageTimeMs = (endTime - startTime) / 1_000_000;
        System.out.println("ToImage: " + toImageTimeMs + " ms");

        long endTimeTotal = System.nanoTime();
        long totalTimeMs = (endTimeTotal - startTimeTotal) / 1_000_000;
        System.out.println("Total: " + totalTimeMs + " ms");
        System.out.println();
        this.image = img;

        return img;
    }

    // Go around a pixel, look at all neighbors in a range and sum up the value
    public int[][] smooth(int[][] template) {
        int range = 2;
        int width = template.length;
        int height = template[0].length;
        int[][] result = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                List<Integer> list = new ArrayList<>();
                for (int currentX = x - range; currentX < x + range; currentX++) {
                    for (int currentY = y - range; currentY < y + range; currentY++) {
                        if (currentX < width && currentX >= 0 && currentY < height && currentY >= 0) {
                            int pixelValue = template[currentX][currentY];
                            list.add(pixelValue);
                        }
                    }
                }
                int color = ColorUtil.calcAverageList(list.stream().mapToInt(Integer::intValue).toArray());
                result[x][y] = color;
            }
        }
        return result;
    }

    // Save Image to File
    public void imgToFile(String pDictionaryName, String pFileName) {
        File folder = new File(pDictionaryName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try {
            File f = new File(pDictionaryName + "\\" + pFileName + ".png");
            if (!f.exists()) {
                ImageIO.write(this.image, "png", f);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    // Layer different smoothed numbers on top of each other to create a result
    private int[][] layeredSmoothedNumbers(int layers, int distance) {
        assert layers > 0;
        int[][] result = new int[width][height];
        int[][][] intermediate = smoothedNumbers(layers, distance);
        for (int x = 0; x < result.length; x++) {
            for (int y = 0; y < result[x].length; y++) {
                double multiplier = 0.5;
                for (int layer = 0; layer < layers; layer++) {
                    result[x][y] += intermediate[layer][x][y] * multiplier;
                    multiplier /= 2;
                }
            }
        }
        return result;
    }

    private int[][] smoothedNumbers(int distance) {
        return smoothedNumbers(1,distance)[0];
    }

    // By bilinearly interpolating and smoothing the values we get 2d array off mostly smooth values
    private int[][][] smoothedNumbers(int layers, int distance) {
        assert layers > 0;
        int width = this.width;
        int height = this.height;
        HashMap<Integer,Integer> distancePerLayer = new HashMap<>();

        int[][][] smoothedNumbers = new int[layers][width][height];
        int[][][] betweenVectors = new int[layers][][];
        for(int layer = 0; layer < layers; layer++){
            distancePerLayer.put(layer, (int) Math.max((double)distance/Math.pow(2,layer),1));
            betweenVectors[layer] = randomPixels((int) Math.ceil((float) width / distancePerLayer.get(layer) + 1), (int) Math.ceil((float) height / distancePerLayer.get(layer) + 1));
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for(int layer = 0; layer < layers; layer++) {
                    smoothedNumbers[layer][x][y] = calcBilinear(x, y, distancePerLayer.get(layer), betweenVectors[layer]);
                }
            }
        }
        return smoothedNumbers;
    }

    private int calcBilinear(int x, int y, int distance, int[][] betweenVectors) {
        /* Position of the point in the top left corner in the between array
           Layout of points in a square: P is our point
            1oo2
            oPoo
            oooo
            3oo4
        */
        int betweenX = x / distance;
        int betweenY = y / distance;

        // Get index in between array
        int X1 = betweenX;
        int Y1 = betweenY;
        int X2 = betweenX + 1;
        int Y2 = betweenY;
        int X3 = betweenX;
        int Y3 = betweenY + 1;
        int X4 = betweenX + 1;
        int Y4 = betweenY + 1;
        double area1 = Math.abs(x - X4 * distance) * Math.abs(y - Y4 * distance);
        double area2 = Math.abs(x - X3 * distance) * Math.abs(y - Y3 * distance);
        double area3 = Math.abs(x - X2 * distance) * Math.abs(y - Y2 * distance);
        double area4 = Math.abs(x - X1 * distance) * Math.abs(y - Y1 * distance);
        double weight1 = smoothStep(area1 / Math.pow(distance, 2));
        double weight2 = smoothStep(area2 / Math.pow(distance, 2));
        double weight3 = smoothStep(area3 / Math.pow(distance, 2));
        double weight4 = smoothStep(area4 / Math.pow(distance, 2));
        double sum = weight1 + weight2 + weight3 + weight4;
        return (int) ((weight1 / sum * betweenVectors[X1][Y1]) + (weight2 / sum * betweenVectors[X2][Y2]) + (weight3 / sum * betweenVectors[X3][Y3]) + (weight4 / sum * betweenVectors[X4][Y4]));
    }

    // A Function to smooth values between 0 and 1
    private double smoothStep(double value) {
        return 3 * Math.pow(value, 2) - 2 * Math.pow(value, 3);
    }

    private int averageValueOfNumbers(int x, int y, int size, int[][] array, int factor) {
        int avg = 0;
        int counter = 0;
        int realX = x - (x % size);
        int realY = y - (y % size);

        for (int curX = realX - (factor * size); curX <= realX + (factor * size); curX += size) {
            for (int curY = realY - (factor * size); curY <= realY + (factor * size); curY += size) {
                if (curX >= 0 && curX < width && curY >= 0 && curY < height) {
                    avg += array[curX][curY];
                    counter++;
                }
            }
        }
        return avg / counter;
    }

    private int[][] randomPixels(int width, int height) {
        int[][] values = new int[width][height];
        for (int x = 0; x < values.length; x++) {
            for (int y = 0; y < values[x].length; y++) {
                values[x][y] = randomGenerator.next(255);
            }
        }
        return values;
    }
}
