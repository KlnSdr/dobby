package dobby.filter;

public interface Filter<T> {
    String getName();
    int getOrder();
    void run(T in);
}
