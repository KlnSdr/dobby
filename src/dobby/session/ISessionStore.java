package dobby.session;

import java.util.Map;
import java.util.Optional;

public interface ISessionStore {
    Optional<Session> find(String sessionId);
    void update(Session session);
    void remove(String sessionId);
    Map<String, Long> getSessionAges();
}
