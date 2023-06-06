// Pseudorandom number generator
public class RandomGenerator {
    private int current;
    private final int seed;

    public RandomGenerator(){
        this((int)(Math.random()*Math.pow(10, 8)));
    }

    public RandomGenerator(int seed){
        this.seed = seed;
        this.current = seed;
    }

    public int getSeed() {
        return seed;
    }

    public int next(int max) {
        int x = current;
        x ^= x << 13;
        x ^= x >> 17;
        x ^= x << 5;
        current = x;
        return Math.abs((x%(max+1)));
    }

    public int range(int min,int max) {
        int next = next(max-min);
        return min+next;
    }
}