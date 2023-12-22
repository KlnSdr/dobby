package dobby.session.service;

import dobby.session.Session;
import dobby.task.SchedulerService;
import dobby.util.Config;
import dobby.util.logging.Logger;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * The SessionService class is used to manage sessions
 */
public class SessionService {
    private static final Logger LOGGER = new Logger(SessionService.class);
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final int maxSessionAge = Config.getInstance().getInt("dobby.session.maxAge", 24);

    private SessionService() {
        final int cleanupInterval = Config.getInstance().getInt("dobby.session.cleanUpInterval", 30);
        LOGGER.info("starting session cleanup scheduler with interval of " + cleanupInterval + " min...");
        SchedulerService.getInstance().addRepeating(this::cleanUpSessions, cleanupInterval, TimeUnit.MINUTES);
    }

    public static SessionService getInstance() {
        return SessionServiceHolder.INSTANCE;
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private void cleanUpSessions() {
        LOGGER.info("Cleaning up sessions");
        long currentTime = getCurrentTime();
        sessions.forEach((id, session) -> {
            if (currentTime - session.getLastAccessed() > (long) maxSessionAge * 60 * 60 * 1000) {
                sessions.remove(id);
            }
        });
    }

    /**
     * Find a session by its id
     *
     * @param sessionId Session id
     * @return The session if found, otherwise empty
     */
    public Optional<Session> find(String sessionId) {
        final Session session = sessions.get(sessionId);
        if (session == null) {
            return Optional.empty();
        }
        session.setLastAccessed(getCurrentTime());
        return Optional.of(session);
    }

    /**
     * Save a session
     *
     * @param session Session to save
     */
    public void set(Session session) {
        session.setLastAccessed(getCurrentTime());
        sessions.put(session.getId(), session);
    }

    /**
     * Remove a session
     *
     * @param session Session to remove
     */
    public void remove(Session session) {
        if (session.getId() == null) {
            return;
        }
        sessions.remove(session.getId());
    }

    /**
     * Create a new session
     *
     * @return The new session
     */
    public Session newSession() {
        Session session = new Session();
        session.setId(generateSessionId());
        session.setLastAccessed(getCurrentTime());
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
