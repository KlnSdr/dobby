package dobby.session.service;

import dobby.session.ISession;
import dobby.session.ISessionStore;
import dobby.session.Session;

import java.util.Optional;

public interface ISessionService {
    void setSessionStore(ISessionStore sessionStore);
    Optional<ISession> find(String sessionId);
    void set(ISession session);
    void remove(ISession session);
    ISession newSession();
}
