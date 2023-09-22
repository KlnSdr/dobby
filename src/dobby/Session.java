package dobby;

import dobby.session.SessionService;

import java.util.HashMap;

public class Session {
    private final HashMap<String, String> session = new HashMap<>();
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void set(String key, String value) {
        session.put(key, value);
    }

    public String get(String key) {
        return session.get(key);
    }

    public void remove(String key) {
        session.remove(key);
    }

    public void destroy() {
        SessionService.getInstance().remove(this);
        session.clear();
        setId(null);
    }

    public boolean contains(String key) {
        return session.containsKey(key);
    }
}
