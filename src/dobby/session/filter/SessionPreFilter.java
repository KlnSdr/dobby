package dobby.session.filter;

import dobby.cookie.Cookie;
import dobby.filter.Filter;
import dobby.filter.FilterOrder;
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
        return FilterOrder.SESSION_PRE_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext ctx) {
        Cookie sessionId = ctx.getRequest().getCookie("DOBBY_SESSION");

        if (sessionId == null) {
            return true;
        }

        sessionService.find(sessionId.getValue()).ifPresent(ctx::setSession);
        return true;
    }
}
