package dobby.session.service;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.session.*;
import dobby.task.ISchedulerService;
import dobby.Config;
import common.logger.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * The SessionService class is used to manage sessions
 */
@RegisterFor(ISessionService.class)
public class SessionService implements ISessionService {
    private static final Logger LOGGER = new Logger(SessionService.class);
    private ISessionStore sessionStore = new DefaultSessionStore(); // initialize with default session store because the config option is read AFTER running preStart(). accessing sessions (for what ever reason) before the config is read will result in a NPE
    private final int maxSessionAge = Config.getInstance().getInt("dobby.session.maxAge", 24);

    @Inject
    public SessionService(ISchedulerService schedulerService) {
        final int cleanupInterval = Config.getInstance().getInt("dobby.session.cleanUpInterval", 30);
        LOGGER.info("starting session cleanup scheduler with interval of " + cleanupInterval + " min...");
        schedulerService.addRepeating(this::cleanUpSessions, cleanupInterval, TimeUnit.MINUTES);
    }

    public void setSessionStore(ISessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private void cleanUpSessions() {
        LOGGER.info("Cleaning up sessions");
        long currentTime = getCurrentTime();
        final Map<String, Long> sessions = sessionStore.getSessionAges();

        sessions.forEach((id, lastAccessed) -> {
            if (currentTime - lastAccessed > (long) maxSessionAge * 60 * 60 * 1000) {
                sessionStore.remove(id);
            }
        });
    }

    /**
     * Find a session by its id
     *
     * @param sessionId Session id
     * @return The session if found, otherwise empty
     */
    public Optional<ISession> find(String sessionId) {
        final Optional<ISession> optSession = sessionStore.find(sessionId);

        if (optSession.isEmpty()) {
            return Optional.empty();
        }

        optSession.get().setLastAccessed(getCurrentTime());
        return optSession;
    }

    /**
     * Save a session
     *
     * @param session Session to save
     */
    public void set(ISession session) {
        session.setLastAccessed(getCurrentTime());
        sessionStore.update(session);
    }

    /**
     * Remove a session
     *
     * @param session Session to remove
     */
    public void remove(ISession session) {
        if (session.getId() == null) {
            return;
        }
        sessionStore.remove(session.getId());
    }

    /**
     * Create a new session
     *
     * @return The new session
     */
    public ISession newSession() {
        SessionWrapper session = new SessionWrapper(this);
        session.setId(generateSessionId());
        session.setLastAccessed(getCurrentTime());
        sessionStore.update(session);
        return session;
    }

    private String generateSessionId() {
        return UUID.randomUUID() + UUID.randomUUID().toString();
    }
}
