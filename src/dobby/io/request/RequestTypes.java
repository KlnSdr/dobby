package dobby.io.request;

public enum RequestTypes {
    GET, POST,PUT, DELETE, PATCH, UNKNOWN;
    public static RequestTypes fromString(String raw) {
        for (RequestTypes method : RequestTypes.values()) {
            if (method.name().equalsIgnoreCase(raw)) {
                return method;
            }
        }
        return UNKNOWN;
    }
}
