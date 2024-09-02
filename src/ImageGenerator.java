import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

// Class for Generating the image
public class ImageGenerator {
    private RandomGenerator randomGenerator = new RandomGenerator();
    private final int width = 1000; // in pixel
    private final int height = 1000; // in pixel
    final int minimumPlanetSize = 120;
    final int maximumPlanetSize = 350;
    private BufferedImage image;

    public BufferedImage nextBySeed(int seed) {
        randomGenerator = new RandomGenerator(seed);
        return makeImg();
    }

    public int getSeed() {
        return randomGenerator.getSeed();
    }

    public void rerollSeed() {
        randomGenerator = new RandomGenerator();
    }

    public BufferedImage makeImg() {
        System.out.println("Generating Seed: " + getSeed());
        long startTimeTotal = System.nanoTime();

        // Coordinates of planet
        double coordinatePaddingPercentage = 0.1;
        int planetMidX = (int) randomGenerator.range(width * coordinatePaddingPercentage, width * (1 - coordinatePaddingPercentage));
        int planetMidY = (int) randomGenerator.range(height * coordinatePaddingPercentage, height * (1 - coordinatePaddingPercentage));

        // set the planetRadius
        int planetRadius = randomGenerator.range(minimumPlanetSize, maximumPlanetSize);

        // Random numbers
        long startTime = System.nanoTime();

        BufferedImage planetImage = smoothImage(layeredRandomImage(planetRadius * 2 + 1, planetRadius * 2 + 1, 3), 1);
        BufferedImage nebularImage = smoothImage(layeredRandomImage(width, height, 3, 0, 20, 0, 20, 0, 40), 1);

        long endTime = System.nanoTime();
        long randomNumbersTimeMs = (endTime - startTime) / 1_000_000;
        System.out.println("GenerateRandom: " + randomNumbersTimeMs + " ms");


        // add Background Stars
        startTime = System.nanoTime();
        addStars(nebularImage);
        endTime = System.nanoTime();
        long starsTimeMs = (endTime - startTime) / 1_000_000;
        System.out.println("Stars: " + starsTimeMs + " ms");


        // Planet
        startTime = System.nanoTime();
        final int minimumColors = 4;
        final int maximumColors = 10;
        int colorAmount = randomGenerator.range(minimumColors, maximumColors);
        int[] colors = new int[colorAmount];
        for (int z = 0; z < colors.length; z++) {
            int color = ColorUtil.BLACK | ColorUtil.red(randomGenerator.next(255)) | ColorUtil.green(randomGenerator.next(255)) | ColorUtil.blue(randomGenerator.next(255));
            colors[z] = color;
        }

        // Paint Planet
        for (int x = 0; x < planetImage.getWidth(); x++) {
            for (int y = 0; y < planetImage.getHeight(); y++) {
                int color = planetImage.getRGB(x, y);
                int red = ColorUtil.getRed(color);
                planetImage.setRGB(x, y, ColorUtil.gradient(colors, red, 255));
            }
        }
        planetImage = smoothImage(planetImage, 1);
        endTime = System.nanoTime();
        long planetTimeMs = (endTime - startTime) / 1_000_000;
        System.out.println("Planet: " + planetTimeMs + " ms");


        // Map to result Image
        double[] lightPosition = {randomGenerator.next(width * 1.5) - width / 2.0, -planetMidY - randomGenerator.range(100, 200), planetRadius * 3 + randomGenerator.next(planetRadius)};
        int lightColor = ColorUtil.fromARGB(256, randomGenerator.range(120, 256), randomGenerator.range(120, 256), randomGenerator.range(120, 256));
        startTime = System.nanoTime();
        BufferedImage result = nebularImage;
        for (int x = -planetRadius; x <= planetRadius; x++) {
            int fx = (int) Math.round(Math.sqrt((Math.pow(planetRadius, 2) - Math.pow(x, 2))));
            for (int y = -fx; y <= fx; y++) {
                int newX = x + planetMidX;
                int newY = y + planetMidY;
                if (isValidPixel(newX, newY)) {
                    double z = Math.sqrt(Math.pow(planetRadius, 2) - Math.pow(x, 2) - Math.pow(y, 2));
                    double[] normal = normalize(new double[]{x, y, z});
                    double[] lightDir = normalize(new double[]{
                            lightPosition[0] - x,
                            lightPosition[1] - y,
                            lightPosition[2] - z
                    });
                    double intensity = Math.max(0, dotProduct(normal, lightDir));
                    int originalColor = planetImage.getRGB(x + planetRadius, y + planetRadius);
                    int average = ColorUtil.calcAverage(lightColor, originalColor, 0.5);
                    int shadedColor = ColorUtil.changeLighting(average, intensity);
                    result.setRGB(newX, newY, shadedColor);
                }
            }
        }
        endTime = System.nanoTime();
        long toImageTimeMs = (endTime - startTime) / 1_000_000;
        System.out.println("ToImage: " + toImageTimeMs + " ms");

        startTime = System.nanoTime();
        result = smoothImage(result, 1);
        endTime = System.nanoTime();
        long toSmoothImageTimeMs = (endTime - startTime) / 1_000_000;
        System.out.println("ToSmoothImage: " + toSmoothImageTimeMs + " ms");

        long endTimeTotal = System.nanoTime();
        long totalTimeMs = (endTimeTotal - startTimeTotal) / 1_000_000;
        System.out.println("Total: " + totalTimeMs + " ms");
        System.out.println();
        this.image = result;

        return result;
    }

    private double[] normalize(double[] vector) {
        double length = Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
        return new double[]{vector[0] / length, vector[1] / length, vector[2] / length};
    }

    private double dotProduct(double[] v1, double[] v2) {
        return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
    }

    private void addStars(BufferedImage image) {
        int starAmount = randomGenerator.range(50, 300);
        for (int z = 0; z < starAmount; z++) {
            boolean isDiagonal = randomGenerator.next(4) == 0;
            boolean isBoth = randomGenerator.next(6) == 0;

            int starRadius = randomGenerator.next(isDiagonal && !isBoth ? 5 : 7) + 1;
            int midX = randomGenerator.next(width - 1);
            int midY = randomGenerator.next(height - 1);
            int color = (ColorUtil.BLACK) | (randomGenerator.range(200, 255) << 16) | (randomGenerator.range(200, 255) << 8) | randomGenerator.range(200, 255);


            if (isDiagonal || isBoth) {
                for (int radiusStep = 0; radiusStep < starRadius; radiusStep++) {
                    int lightedColor = ColorUtil.changeLighting(color, 1 - radiusStep * 0.1);

                    if (isValidPixel(midX + radiusStep, midY + radiusStep)) {
                        int currentColor = image.getRGB(midX + radiusStep, midY + radiusStep);
                        image.setRGB(midX + radiusStep, midY + radiusStep, ColorUtil.calcAverage(currentColor, lightedColor));
                    }

                    if (isValidPixel(midX + radiusStep, midY - radiusStep)) {
                        int currentColor = image.getRGB(midX + radiusStep, midY - radiusStep);
                        image.setRGB(midX + radiusStep, midY - radiusStep, ColorUtil.calcAverage(currentColor, lightedColor));
                    }

                    if (isValidPixel(midX - radiusStep, midY + radiusStep)) {
                        int currentColor = image.getRGB(midX - radiusStep, midY + radiusStep);
                        image.setRGB(midX - radiusStep, midY + radiusStep, ColorUtil.calcAverage(currentColor, lightedColor));
                    }

                    if (isValidPixel(midX - radiusStep, midY - radiusStep)) {
                        int currentColor = image.getRGB(midX - radiusStep, midY - radiusStep);
                        image.setRGB(midX - radiusStep, midY - radiusStep, ColorUtil.calcAverage(currentColor, lightedColor));
                    }
                }

                if (starRadius > 3) {
                    int lightedColor = ColorUtil.changeLighting(color, 1 - 1 * 0.15);

                    if (isValidPixel(midX + 1, midY)) {
                        int currentColor = image.getRGB(midX + 1, midY);
                        image.setRGB(midX + 1, midY, ColorUtil.calcAverage(currentColor, lightedColor));
                    }

                    if (isValidPixel(midX - 1, midY)) {
                        int currentColor = image.getRGB(midX - 1, midY);
                        image.setRGB(midX - 1, midY, ColorUtil.calcAverage(currentColor, lightedColor));
                    }

                    if (isValidPixel(midX, midY + 1)) {
                        int currentColor = image.getRGB(midX, midY + 1);
                        image.setRGB(midX, midY + 1, ColorUtil.calcAverage(currentColor, lightedColor));
                    }

                    if (isValidPixel(midX, midY - 1)) {
                        int currentColor = image.getRGB(midX, midY - 1);
                        image.setRGB(midX, midY - 1, ColorUtil.calcAverage(currentColor, lightedColor));
                    }
                }
            }

            if (isBoth || !isDiagonal) {
                for (int radiusStep = 0; radiusStep < starRadius; radiusStep++) {
                    int lightedColor = ColorUtil.changeLighting(color, 1 - radiusStep * 0.1);

                    if (isValidPixel(midX + radiusStep, midY)) {
                        int currentColor = image.getRGB(midX + radiusStep, midY);
                        image.setRGB(midX + radiusStep, midY, ColorUtil.calcAverage(currentColor, lightedColor));
                    }

                    if (isValidPixel(midX - radiusStep, midY)) {
                        int currentColor = image.getRGB(midX - radiusStep, midY);
                        image.setRGB(midX - radiusStep, midY, ColorUtil.calcAverage(currentColor, lightedColor));
                    }

                    if (isValidPixel(midX, midY + radiusStep)) {
                        int currentColor = image.getRGB(midX, midY + radiusStep);
                        image.setRGB(midX, midY + radiusStep, ColorUtil.calcAverage(currentColor, lightedColor));
                    }

                    if (isValidPixel(midX, midY - radiusStep)) {
                        int currentColor = image.getRGB(midX, midY - radiusStep);
                        image.setRGB(midX, midY - radiusStep, ColorUtil.calcAverage(currentColor, lightedColor));
                    }
                }

                if (starRadius > 3) {
                    int lightedColor = ColorUtil.changeLighting(color, 1 - 1 * 0.15);

                    if (isValidPixel(midX + 1, midY + 1)) {
                        int currentColor = image.getRGB(midX + 1, midY + 1);
                        image.setRGB(midX + 1, midY + 1, ColorUtil.calcAverage(currentColor, lightedColor));
                    }

                    if (isValidPixel(midX + 1, midY - 1)) {
                        int currentColor = image.getRGB(midX + 1, midY - 1);
                        image.setRGB(midX + 1, midY - 1, ColorUtil.calcAverage(currentColor, lightedColor));
                    }

                    if (isValidPixel(midX - 1, midY + 1)) {
                        int currentColor = image.getRGB(midX - 1, midY + 1);
                        image.setRGB(midX - 1, midY + 1, ColorUtil.calcAverage(currentColor, lightedColor));
                    }

                    if (isValidPixel(midX - 1, midY - 1)) {
                        int currentColor = image.getRGB(midX - 1, midY - 1);
                        image.setRGB(midX - 1, midY - 1, ColorUtil.calcAverage(currentColor, lightedColor));
                    }
                }
            }
        }
    }

    private boolean isValidPixel(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }


    private Kernel generateGaussianKernel(int sigmaX, int sigmaY) {
        int width = 6 * sigmaX + 1;
        int height = 6 * sigmaY + 1;
        float[] data = new float[width * height];
        int halfSizeX = width / 2;
        int halfSizeY = height / 2;
        double sigma = (double) sigmaX * (double) sigmaY;
        if (sigmaX == 0) {
            sigma = sigmaY;
        }
        if (sigmaY == 0) {
            sigma = sigmaX;
        }
        for (int x = -halfSizeX; x <= halfSizeX; x++) {
            for (int y = -halfSizeY; y <= halfSizeY; y++) {
                int index = (x + halfSizeX) * width + (y + halfSizeY);
                if (index >= 0 && index < data.length) {
                    float value = (float) (Math.exp(-(x * x + y * y) / (2 * sigma)) / (2 * Math.PI * sigma));
                    data[index] = value;
                }
            }
        }

        return new Kernel(width, height, data);
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

    private BufferedImage smoothedRandomImage(int width, int height, int smoothingFactor) {
        return smoothImage(randomImage(width, height), smoothingFactor);
    }

    private BufferedImage smoothImage(BufferedImage image, int smoothingFactor) {
        return smoothImage(image, smoothingFactor, smoothingFactor);
    }

    private BufferedImage smoothImage(BufferedImage image, int smoothingFactorX, int smoothingFactorY) {
        int paddingX = smoothingFactorX * 3;
        int paddingY = smoothingFactorY * 3;
        Kernel kernel = generateGaussianKernel(smoothingFactorX, smoothingFactorY);
        BufferedImageOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage paddedImage = addPadding(image, paddingX, paddingY);
        BufferedImage smoothedPaddedImage = op.filter(paddedImage, null);

        return smoothedPaddedImage.getSubimage(
                paddingX, paddingY,
                image.getWidth(), image.getHeight()
        );
    }

    private BufferedImage addPadding(BufferedImage image, int paddingX, int paddingY) {
        int width = image.getWidth() + 2 * paddingX;
        int height = image.getHeight() + 2 * paddingY;

        BufferedImage paddedImage = new BufferedImage(width, height, image.getType());

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                paddedImage.setRGB(x + paddingX, y + paddingY, image.getRGB(x, y));
            }
        }

        for (int x = 0; x < width; x++) {
            int colorTop = paddedImage.getRGB(x, paddingY);
            int colorBottom = paddedImage.getRGB(x, height - paddingY - 1);
            for (int yOffset = 0; yOffset < paddingY; yOffset++) {
                paddedImage.setRGB(x, yOffset, colorTop);
                paddedImage.setRGB(x, height - yOffset - 1, colorBottom);
            }
        }

        // Left and Right edges
        for (int y = 0; y < height; y++) {
            int colorLeft = paddedImage.getRGB(paddingX, y);
            int colorRight = paddedImage.getRGB(width - paddingX - 1, y);
            for (int xOffset = 0; xOffset < paddingX; xOffset++) {
                paddedImage.setRGB(xOffset, y, colorLeft);
                paddedImage.setRGB(width - xOffset - 1, y, colorRight);
            }
        }

        int topLeftColor = paddedImage.getRGB(paddingX, paddingY);
        int topRightColor = paddedImage.getRGB(width - paddingX - 1, paddingY);
        int bottomLeftColor = paddedImage.getRGB(paddingX, height - paddingY - 1);
        int bottomRightColor = paddedImage.getRGB(width - paddingX - 1, height - paddingY - 1);
        for (int xOffset = 0; xOffset < paddingX; xOffset++) {
            for (int yOffset = 0; yOffset < paddingY; yOffset++) {
                paddedImage.setRGB(xOffset, yOffset, topLeftColor);
                paddedImage.setRGB(width - xOffset - 1, yOffset, topRightColor);
                paddedImage.setRGB(xOffset, height - yOffset - 1, bottomLeftColor);
                paddedImage.setRGB(width - xOffset - 1, height - yOffset - 1, bottomRightColor);
            }
        }


        return paddedImage;
    }

    private BufferedImage strech(BufferedImage image, double x, double y) {
        AffineTransform transform = AffineTransform.getScaleInstance(x, y);
        AffineTransformOp op = new AffineTransformOp(transform, null);
        BufferedImage target = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        op.filter(image, target);
        return target;
    }

    private BufferedImage randomImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, randomGenerator.randomRGB(width * height), 0, width);
        return image;
    }

    private BufferedImage randomImage(int width, int height, int minR, int maxR, int minG, int maxG, int minB, int maxB) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, randomGenerator.randomRGB(width * height, minR, maxR, minG, maxG, minB, maxB), 0, width);
        return image;
    }

    private BufferedImage layeredRandomImage(int width, int height, int layers) {
        return layeredRandomImage(width, height, layers, 0, 256, 0, 256, 0, 256);
    }

    private BufferedImage layeredRandomImage(int width, int height, int layers, int minR, int maxR, int minG, int maxG, int minB, int maxB) {
        assert layers > 0;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage[] layerImages = new BufferedImage[layers];

        // Create thread pool
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);

        for (int layer = 0; layer < layers; layer++) {
            int layerWidth = width >> layer;  // Divide width by 2^layer
            int layerHeight = height >> layer; // Divide height by 2^layer
            int finalLayer = layer;

            executor.submit(() -> {
                layerImages[finalLayer] = smoothImage(randomImage(layerWidth, layerHeight, minR, maxR, minG, maxG, minB, maxB), 1);
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int layer = 0; layer < layers; layer++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int divisor = 1 << layer;
                    image.setRGB(x, y, layerImages[layer].getRGB(width / divisor, height / divisor));
                }
            }
        }


        return smoothImage(image, 1);
    }
}
