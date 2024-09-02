import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// Pseudorandom number generator
public class RandomGenerator {
    private final int seed;
    private final Random random;

    public RandomGenerator(){
        this((int) System.nanoTime());
    }

    public RandomGenerator(int seed){
        this.seed = seed;
        this.random = new Random();
        random.setSeed(seed);
    }

    public int getSeed() {
        return seed;
    }

    /**
     * @param max exclusive upper bound
     */
    public int next(int max) {
        return random.nextInt(max);
    }

    /**
     * @param min inclusive lower bound
     * @param max exclusive upper bound
     */
    public int range(int min,int max) {
        int next = next(max-min);
        return min+next;
    }

    public double next() {
        return random.nextDouble();
    }

    /**
     * @param max exclusive upper bound
     */
    public double next(double max) {
        return random.nextDouble(max);
    }

    /**
     * @param min inclusive lower bound
     * @param max exclusive upper bound
     */
    public double range(double min,double max) {
        double next = next(max-min);
        return min+next;
    }

    public int[] randomRGB(int size) {
        return randomRGB(size, 0, 256, 0, 256, 0, 256);
    }

    /**
     *
     * @param minR inclusive lower bound for R value
     * @param maxR exclusive upper bound for R value
     * @param minG inclusive lower bound for G value
     * @param maxG exclusive upper bound for G value
     * @param minB inclusive lower bound for B value
     * @param maxB exclusive upper bound for B value
     */
    public int[] randomRGB(int size, int minR, int maxR, int minG, int maxG, int minB, int maxB) {
        assert minR > 0 && maxR <= 255 && maxR >= minR;
        assert minG > 0 && maxG <= 255 && maxG >= minG;
        assert minB > 0 && maxB <= 255 && maxB >= minB;
        int[] rgbArray = new int[size];

        int cores = Runtime.getRuntime().availableProcessors(); // Number of available cores
        ExecutorService executor = Executors.newFixedThreadPool(cores);

        final int chunkSize = (int) Math.ceil((double) size / (double) cores); // Split work evenly among cores

        // Submit tasks to the thread pool
        for (int i = 0; i < cores; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                Random threadRandom = new Random(seed + threadIndex);
                int startIndex = chunkSize * threadIndex;
                int endIndex = Math.min(startIndex + chunkSize, size);
                for (int j = startIndex; j < endIndex; j++) {
                    int r = threadRandom.nextInt(minR, maxR);
                    int g = threadRandom.nextInt(minG, maxG);
                    int b = threadRandom.nextInt(minB, maxB);
                    rgbArray[j] = ColorUtil.fromARGB(255, r, g, b); // Combine RGB into a single int
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return rgbArray;
    }
}