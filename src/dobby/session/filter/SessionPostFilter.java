package dobby.session.filter;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.cookie.Cookie;
import dobby.filter.Filter;
import dobby.filter.FilterOrder;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.session.Session;
import dobby.session.service.ISessionService;
import dobby.session.service.SessionService;

/**
 * The SessionPostFilter class is used to save the session to the database and set the session cookie
 */
@RegisterFor(ISessionPostFilter.class)
public class SessionPostFilter implements ISessionPostFilter {
    private final ISessionService sessionService;

    @Inject
    public SessionPostFilter(ISessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public String getName() {
        return "session";
    }

    @Override
    public FilterType getType() {
        return FilterType.POST;
    }

    @Override
    public int getOrder() {
        return FilterOrder.SESSION_POST_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext ctx) {
        Session session = ctx.getSession();

        if (session.getId() == null) {
            return true;
        }

        sessionService.set(session);

        Cookie cookie = new Cookie("DOBBY_SESSION", session.getId());
        ctx.getResponse().setCookie("DOBBY_SESSION", cookie);

        return true;
    }
}
