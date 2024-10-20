package dobby.filter;

public enum FilterOrder {
    // pre filters
    CLEAN_ROUTES_PRE_FILTER(0),
    SESSION_PRE_FILTER(1),
    // post filters
    SESSION_POST_FILTER(0),
    COOKIE_POST_FILTER(1);

    private final int order;

    FilterOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
