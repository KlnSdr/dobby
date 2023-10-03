package dobby.session.filter;

import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.session.service.SessionService;

/**
 * The SessionPreFilter class is used to find the session in the database and set it in the HttpContext
 */
public class SessionPreFilter implements Filter {
    private final SessionService sessionService = SessionService.getInstance();

    @Override
    public String getName() {
        return "session";
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean run(HttpContext ctx) {
        String sessionId = ctx.getRequest().getCookie("DOBBY_SESSION");

        if (sessionId == null) {
            return true;
        }

        sessionService.find(sessionId).ifPresent(ctx::setSession);
        return true;
    }
}
