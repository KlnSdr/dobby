package dobby.filter;

interface Filter<T> {
    String getName();
    int getOrder();
    void run(T in);
}
