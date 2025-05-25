package dobby.session.service;

import dobby.session.ISessionStore;
import dobby.session.Session;

import java.util.Optional;

public interface ISessionService {
    void setSessionStore(ISessionStore sessionStore);
    Optional<Session> find(String sessionId);
    void set(Session session);
    void remove(Session session);
    Session newSession();
}
