package dobby.session;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultSessionStore implements ISessionStore {
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    @Override
    public Optional<Session> find(String sessionId) {
        if (sessions.containsKey(sessionId)) {
            return Optional.of(sessions.get(sessionId));
        }
        return Optional.empty();
    }

    @Override
    public void update(Session session) {
        sessions.put(session.getId(), session);
    }

    @Override
    public void remove(String sessionId) {
        sessions.remove(sessionId);
    }

    @Override
    public Map<String, Long> getSessionAges() {
        final Map<String, Long> sessionAges = new ConcurrentHashMap<>();
        sessions.forEach((id, session) -> sessionAges.put(id, session.getLastAccessed()));
        return sessionAges;
    }
}
