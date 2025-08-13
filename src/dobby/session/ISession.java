package dobby.session;

public interface ISession {
    String getId();
    void setId(String id);
    void set(String key, String value);
    String get(String key);
    void remove(String key);
    void destroy();
    long getLastAccessed();
    void setLastAccessed(long lastAccessed);
    boolean contains(String key);
    Session getSession();
    void setSession(Session session);
}
