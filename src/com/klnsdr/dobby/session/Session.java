package com.klnsdr.dobby.session;

import com.klnsdr.dobby.session.service.SessionService;

import java.util.HashMap;

/**
 * The Session class is used to store session data
 */
public class Session {
    private final HashMap<String, String> session = new HashMap<>();
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets a value in the session
     * @param key The key to set
     * @param value The value to set
     */
    public void set(String key, String value) {
        session.put(key, value);
    }

    public String get(String key) {
        return session.get(key);
    }

    /**
     * Removes a value from the session
     * @param key The key to remove
     */
    public void remove(String key) {
        session.remove(key);
    }

    /**
     * Destroys the session
     */
    public void destroy() {
        SessionService.getInstance().remove(this);
        session.clear();
        setId(null);
    }

    /**
     * Checks if the session contains a value for the given key
     * @param key The key to check
     * @return True if the session contains a value for the given key, false otherwise
     */
    public boolean contains(String key) {
        return session.containsKey(key);
    }
}
