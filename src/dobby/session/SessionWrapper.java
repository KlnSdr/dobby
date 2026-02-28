package dobby.session;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import dobby.session.service.ISessionService;
import dobby.session.service.SessionService;

@RegisterFor(ISession.class)
public class SessionWrapper implements ISession {
    private Session session;
    private ISessionService sessionService;

    @Inject
    public SessionWrapper(ISessionService sessionService) {
        this.sessionService = sessionService;
        this.session = new Session();
    }

    @Override
    public String getId() {
        return session.getId();
    }

    @Override
    public void setId(String id) {
        session.setId(id);
    }

    @Override
    public void set(String key, String value) {
        session.set(key, value);
    }

    @Override
    public String get(String key) {
        return session.get(key);
    }

    @Override
    public void remove(String key) {
        session.remove(key);
    }

    @Override
    public void destroy() {
        sessionService.remove(this);
        session.destroy();
    }

    @Override
    public long getLastAccessed() {
        return session.getLastAccessed();
    }

    @Override
    public void setLastAccessed(long lastAccessed) {
        session.setLastAccessed(lastAccessed);
    }

    @Override
    public boolean contains(String key) {
        return session.contains(key);
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }
}
