package dobby.session;

import java.util.Map;
import java.util.Optional;

public interface ISessionStore {
    Optional<ISession> find(String sessionId);
    void update(ISession session);
    void remove(String sessionId);
    Map<String, Long> getSessionAges();
}
