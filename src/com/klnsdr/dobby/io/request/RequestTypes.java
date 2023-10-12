package com.klnsdr.dobby.io.request;

public enum RequestTypes {
    GET, POST,PUT, DELETE, UNKNOWN;
    public static RequestTypes fromString(String raw) {
        for (RequestTypes method : RequestTypes.values()) {
            if (method.name().equalsIgnoreCase(raw)) {
                return method;
            }
        }
        return UNKNOWN;
    }
}
