package dobby.util;

/**
 * Tupel class
 */
public class Tupel<X, Y> {
    private final X x;
    private final Y y;

    public Tupel(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Get the first value of the tupel
     *
     * @return The first value of the tupel
     */
    public X _1() {
        return x;
    }

    /**
     * Get the second value of the tupel
     *
     * @return The second value of the tupel
     */
    public Y _2() {
        return y;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", x.toString(), y.toString());
    }
}
