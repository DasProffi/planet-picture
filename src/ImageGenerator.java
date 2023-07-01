import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

// Class for Generating the image
public class ImageGenerator {
    private RandomGenerator randomGenerator = new RandomGenerator();
    private final int SEED_MAXIMUM = (int) (Math.pow(10, 8) - 1);
    private static int width = 1000; // in pixel
    private static int height = 1000; // in pixel
    final int minimumPlanetSize = 120;
    final int maximumPlanetSize = 350;
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
        double coordinatePaddingPercentage = 0.1;
        int planetMidX = (int) randomGenerator.range(width * coordinatePaddingPercentage, width * (1 - coordinatePaddingPercentage));
        int planetMidY = (int) randomGenerator.range(height * coordinatePaddingPercentage, height * (1 - coordinatePaddingPercentage));

        // set the radius
        int radius = randomGenerator.range(minimumPlanetSize, maximumPlanetSize);

        // Random numbers
        long startTime = System.nanoTime();

        final int planetDistanceY = (int) randomGenerator.range(radius * 0.15, radius * 0.33);
        final int nebularDistance = (int) randomGenerator.range(Math.min(height, width) * 0.10, Math.min(height, width) * 0.30);
        int[][] planetSmoothedPixels = layeredSmoothedNumbers(5, (int) (planetDistanceY * randomGenerator.range(1.7, 3.5)), planetDistanceY);
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
        int nebularColorAmount = randomGenerator.range(11, 15);
        int[] nebularColors = new int[nebularColorAmount];
        nebularColors[0] = ColorUtil.BLACK;
        for (int z = 1; z < nebularColorAmount; z++) {
            int before = nebularColors[z - 1];
            nebularColors[z] = ColorUtil.BLACK | ColorUtil.red(ColorUtil.getRed(before) + randomGenerator.range(2, 5)) | ColorUtil.green(ColorUtil.getGreen(before) + randomGenerator.range(2, 5)) | ColorUtil.blue(ColorUtil.getBlue(before) + randomGenerator.range(3, 5));
        }

        int nebularPixelRange = nebularHighestNumber - nebularSmallestNumber;
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                int currentColor = ColorUtil.gradient(nebularColors, nebularSmoothedPixels[x][y] - nebularSmallestNumber, nebularPixelRange);
                pixels[x][y] = currentColor;
            }
        }
        endTime = System.nanoTime();
        long backgroundTimeMs = (endTime - startTime) / 1_000_000;
        System.out.println("Background: " + backgroundTimeMs + " ms");


        //  Stars
        startTime = System.nanoTime();
        addStars(width, height, randomGenerator, pixels);
        endTime = System.nanoTime();
        long starsTimeMs = (endTime - startTime) / 1_000_000;
        System.out.println("Stars: " + starsTimeMs + " ms");


        // Planet
        startTime = System.nanoTime();
        final int minimumColors = 3;
        final int maximumColors = 7;
        int colorAmount = randomGenerator.range(minimumColors, maximumColors);
        int[] colors = new int[colorAmount];
        for (int z = 0; z < colors.length; z++) {
            int color = ColorUtil.BLACK | ColorUtil.red(randomGenerator.next(255)) | ColorUtil.green(randomGenerator.next(255)) | ColorUtil.blue(randomGenerator.next(255));
            colors[z] = color;
        }

        float blurDistance = 5;
        // Paint Planet
        for (int x = -radius; x <= radius; x++) {
            int fx = (int) Math.round(Math.sqrt((Math.pow(radius, 2) - Math.pow(x, 2))));
            for (int y = -fx; y <= fx; y++) {
                final int currentX = planetMidX + x;
                final int currentY = planetMidY + y;
                if (isValidPixel(currentX, currentY)) {
                    int color = ColorUtil.gradient(colors, planetSmoothedPixels[currentX][currentY] - planetSmallestNumber, planetHighestNumber - planetSmallestNumber);
                    double edgeDistance = Math.abs(Math.hypot(x, y) - radius);
                    if (edgeDistance < blurDistance) {
                        color = ColorUtil.calcAverage(color, pixels[currentX][currentY], edgeDistance / blurDistance);
                    }
                    pixels[currentX][currentY] = color;
                }
            }
        }
        endTime = System.nanoTime();
        long planetTimeMs = (endTime - startTime) / 1_000_000;
        System.out.println("Planet: " + planetTimeMs + " ms");


        // smooth and blur
        startTime = System.nanoTime();
        pixels = smooth(pixels, 1);
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

    private static void addStars(int width, int height, RandomGenerator randomGenerator, int[][] pixels) {
        int starAmount = randomGenerator.range(50, 300);
        for (int z = 0; z < starAmount; z++) {
            boolean isDiagonal = randomGenerator.next(255) % 5 == 0;
            boolean isBoth = randomGenerator.next(255) % 7 == 0;

            int starRadius = randomGenerator.next(isDiagonal && !isBoth ? 5 : 7) + 1;
            int midX = randomGenerator.next(width - 1);
            int midY = randomGenerator.next(height - 1);
            int color = (ColorUtil.BLACK) | (randomGenerator.range(200, 255) << 16) | (randomGenerator.range(200, 255) << 8) | randomGenerator.range(200, 255);


            if (isDiagonal || isBoth) {
                for (int radiusStep = 0; radiusStep < starRadius; radiusStep++) {
                    int lightedColor = ColorUtil.changeLighting(color, 1 - radiusStep * 0.1);
                    if (isValidPixel(midX + radiusStep, midY + radiusStep)) {
                        pixels[midX + radiusStep][midY + radiusStep] = ColorUtil.calcAverage(pixels[midX + radiusStep][midY + radiusStep], lightedColor);
                    }
                    if (isValidPixel(midX + radiusStep, midY - radiusStep)) {
                        pixels[midX + radiusStep][midY - radiusStep] = ColorUtil.calcAverage(pixels[midX + radiusStep][midY - radiusStep], lightedColor);
                    }
                    if (isValidPixel(midX - radiusStep, midY + radiusStep)) {
                        pixels[midX - radiusStep][midY + radiusStep] = ColorUtil.calcAverage(pixels[midX - radiusStep][midY + radiusStep], lightedColor);
                    }
                    if (isValidPixel(midX - radiusStep, midY - radiusStep)) {
                        pixels[midX - radiusStep][midY - radiusStep] = ColorUtil.calcAverage(pixels[midX - radiusStep][midY - radiusStep], lightedColor);
                    }
                }

                if (starRadius > 3) {
                    int lightedColor = ColorUtil.changeLighting(color, 1 - 1 * 0.15);
                    if (isValidPixel(midX + 1, midY)) {
                        pixels[midX + 1][midY] = ColorUtil.calcAverage(pixels[midX + 1][midY], lightedColor);
                    }
                    if (isValidPixel(midX - 1, midY)) {
                        pixels[midX - 1][midY] = ColorUtil.calcAverage(pixels[midX - 1][midY], lightedColor);
                    }
                    if (isValidPixel(midX, midY + 1)) {
                        pixels[midX][midY + 1] = ColorUtil.calcAverage(pixels[midX][midY + 1], lightedColor);
                    }
                    if (isValidPixel(midX, midY - 1)) {
                        pixels[midX][midY - 1] = ColorUtil.calcAverage(pixels[midX][midY - 1], lightedColor);
                    }
                }
            }
            if (isBoth || !isDiagonal) {
                for (int radiusStep = 0; radiusStep < starRadius; radiusStep++) {
                    int lightedColor = ColorUtil.changeLighting(color, 1 - radiusStep * 0.1);
                    if (midX + radiusStep < width) {
                        pixels[midX + radiusStep][midY] = ColorUtil.calcAverage(pixels[midX + radiusStep][midY], lightedColor);
                    }
                    if (midX - radiusStep >= 0) {
                        pixels[midX - radiusStep][midY] = ColorUtil.calcAverage(pixels[midX - radiusStep][midY], lightedColor);
                    }
                    if (midY + radiusStep < height) {
                        pixels[midX][midY + radiusStep] = ColorUtil.calcAverage(pixels[midX][midY + radiusStep], lightedColor);
                    }
                    if (midY - radiusStep >= 0) {
                        pixels[midX][midY - radiusStep] = ColorUtil.calcAverage(pixels[midX][midY - radiusStep], lightedColor);
                    }
                }

                if (starRadius > 3) {
                    int lightedColor = ColorUtil.changeLighting(color, 1 - 1 * 0.15);
                    if (midX + 1 < width && midY + 1 < height) {
                        pixels[midX + 1][midY + 1] = ColorUtil.calcAverage(pixels[midX + 1][midY + 1], lightedColor);
                    }
                    if (midX + 1 < width && midY - 1 >= 0) {
                        pixels[midX + 1][midY - 1] = ColorUtil.calcAverage(pixels[midX + 1][midY - 1], lightedColor);
                    }
                    if (midX - 1 >= 0 && midY + 1 < height) {
                        pixels[midX - 1][midY + 1] = ColorUtil.calcAverage(pixels[midX - 1][midY + 1], lightedColor);
                    }
                    if (midX - 1 >= 0 && midY - 1 > 0) {
                        pixels[midX - 1][midY - 1] = ColorUtil.calcAverage(pixels[midX - 1][midY - 1], lightedColor);
                    }
                }
            }
        }
    }

    private static boolean isValidPixel(int x, int y) {
        return x >= 0 && y >= 0 && x < ImageGenerator.width && y < ImageGenerator.height;
    }

    public int[][] smooth(int[][] template) {
        return smooth(template, 2);
    }

    // Go around a pixel, look at all neighbors in a range and sum up the value
    public int[][] smooth(int[][] template, int range) {
        int width = template.length;
        int height = template[0].length;
        int[][] result = new int[width][height];

        int numThreads = 8; // Number of threads to use
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        int regionSize = width / numThreads;
        for (int i = 0; i < numThreads; i++) {
            final int threadIndex = i;
            executorService.execute(() -> {
                int start = threadIndex * regionSize;
                // Last thread needs to fill all values cut of by in division
                int end = threadIndex == numThreads - 1 ? width : start + regionSize;
                for (int x = start; x < end; x++) {
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
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    private int[][] layeredSmoothedNumbers(int layers, int distance) {
        return layeredSmoothedNumbers(layers, distance, distance);
    }

    // Layer different smoothed numbers on top of each other to create a result
    private int[][] layeredSmoothedNumbers(int layers, int horizontalDistance, int verticalDistance) {
        assert layers > 0;
        int[][] result = new int[width][height];
        int[][][] intermediate = smoothedNumbers(layers, horizontalDistance, verticalDistance);

        int numThreads = 8; // Number of threads to use
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        int regionSize = width / numThreads;
        for (int i = 0; i < numThreads; i++) {
            final int threadIndex = i;
            executorService.execute(() -> {
                int start = threadIndex * regionSize;
                // Last thread needs to fill all values cut of by in division
                int end = threadIndex == numThreads - 1 ? width : start + regionSize;
                for (int x = start; x < end; x++) {
                    for (int y = 0; y < result[x].length; y++) {
                        double multiplier = 0.5;
                        for (int layer = 0; layer < layers; layer++) {
                            result[x][y] += intermediate[layer][x][y] * multiplier;
                            multiplier /= 2;
                        }
                    }
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    private int[][] smoothedNumbers(int distance) {
        return smoothedNumbers(1, distance, distance)[0];
    }

    // By bilinearly interpolating and smoothing the values we get 2d array off mostly smooth values
    private int[][][] smoothedNumbers(int layers, int horizontalDistance, int verticalDistance) {
        assert layers > 0;
        int width = ImageGenerator.width;
        int height = ImageGenerator.height;
        ConcurrentHashMap<Integer, Tuple<Integer, Integer>> distancePerLayer = new ConcurrentHashMap<>();

        int[][][] smoothedNumbers = new int[layers][width][height];
        int[][][] betweenVectors = new int[layers][][];
        ExecutorService executorService = Executors.newFixedThreadPool(layers);

        for (int layer = 0; layer < layers; layer++) {
            final int currentLayer = layer;
            executorService.execute(() -> {
                distancePerLayer.put(currentLayer, new Tuple<>((int) Math.max((double) horizontalDistance / Math.pow(2, currentLayer), 1), (int) Math.max((double) verticalDistance / Math.pow(2, currentLayer), 1)));
                betweenVectors[currentLayer] = randomPixels(
                        (int) Math.ceil((float) width / distancePerLayer.get(currentLayer).first + 1),
                        (int) Math.ceil((float) height / distancePerLayer.get(currentLayer).second + 1)
                );
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int threads = 8;
        ExecutorService executorService2 = Executors.newFixedThreadPool(threads);
        int regionSize = width / threads; // Divide width into four regions
        for (int i = 0; i < threads; i++) {
            final int threadIndex = i;
            executorService2.execute(() -> {
                int start = regionSize * threadIndex;
                int end = threadIndex == threads - 1 ? width : start + regionSize;

                for (int x = start; x < end; x++) {
                    for (int y = 0; y < height; y++) {
                        for (int layer = 0; layer < layers; layer++) {
                            smoothedNumbers[layer][x][y] = calcBilinear(x, y, distancePerLayer.get(layer).first, distancePerLayer.get(layer).second, betweenVectors[layer]);
                        }
                    }
                }
            });
        }

        executorService2.shutdown();
        try {
            executorService2.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return smoothedNumbers;
    }

    private int calcBilinear(int x, int y, int distance, int[][] betweenVectors) {
        return calcBilinear(x, y, distance, distance, betweenVectors);
    }

    private int calcBilinear(int x, int y, int horizontalDistance, int verticalDistance, int[][] betweenVectors) {
        /* Position of the point in the top left corner in the between array
           Layout of points in a square: P is our point
            1oo2
            oPoo
            oooo
            3oo4
        */
        int betweenX = x / horizontalDistance;
        int betweenY = y / verticalDistance;

        // Get index in between array
        int X1 = betweenX;
        int Y1 = betweenY;
        int X2 = betweenX + 1;
        int Y2 = betweenY;
        int X3 = betweenX;
        int Y3 = betweenY + 1;
        int X4 = betweenX + 1;
        int Y4 = betweenY + 1;
        double area1 = Math.abs(x - X4 * horizontalDistance) * Math.abs(y - Y4 * verticalDistance);
        double area2 = Math.abs(x - X3 * horizontalDistance) * Math.abs(y - Y3 * verticalDistance);
        double area3 = Math.abs(x - X2 * horizontalDistance) * Math.abs(y - Y2 * verticalDistance);
        double area4 = Math.abs(x - X1 * horizontalDistance) * Math.abs(y - Y1 * verticalDistance);
        final double entireArea = horizontalDistance * verticalDistance;
        double weight1 = smoothStep(area1 / entireArea);
        double weight2 = smoothStep(area2 / entireArea);
        double weight3 = smoothStep(area3 / entireArea);
        double weight4 = smoothStep(area4 / entireArea);
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

        int numThreads = 8; // Number of threads to use
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        int regionSize = values.length / numThreads;
        for (int i = 0; i < numThreads; i++) {
            final int threadIndex = i;
            executorService.execute(() -> {
                int start = threadIndex * regionSize;
                // Last thread needs to fill all values cut of by in division
                int end = threadIndex == numThreads - 1 ? values.length : start + regionSize;
                for (int x = start; x < end; x++) {
                    for (int y = 0; y < values[x].length; y++) {
                        values[x][y] = randomGenerator.next(255);
                    }
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return values;
    }
}
