package dobby.filter.defaultFilter.pre;

import dobby.Request;
import dobby.filter.pre.PreFilter;
import dobby.session.SessionService;

public class SessionPreFilter implements PreFilter {
    private final SessionService sessionService = SessionService.getInstance();

    @Override
    public String getName() {
        return "session";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void run(Request in) {
        String sessionId = in.getCookie("DOBBY_SESSION");

        if (sessionId == null) {
            return;
        }

        sessionService.find(sessionId).ifPresent(in::setSession);
    }
}
