package dobby.session.service;

import dobby.session.Session;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The SessionService class is used to manage sessions
 */
public class SessionService {
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    private SessionService() {
    }

    public static SessionService getInstance() {
        return SessionServiceHolder.INSTANCE;
    }

    public Optional<Session> find(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public void set(Session session) {
        sessions.put(session.getId(), session);
    }

    public void remove(Session session) {
        if (session.getId() == null) {
            return;
        }
        sessions.remove(session.getId());
    }

    public Session newSession() {
        Session session = new Session();
        session.setId(generateSessionId());
        sessions.put(session.getId(), session);
        return session;
    }

    private String generateSessionId() {
        return UUID.randomUUID() + UUID.randomUUID().toString();
    }

    private static class SessionServiceHolder {
        private static final SessionService INSTANCE = new SessionService();
    }
}
