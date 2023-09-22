package dobby.session.filter;

import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.session.Session;
import dobby.session.service.SessionService;

public class SessionPostFilter implements Filter {
    private final SessionService sessionService = SessionService.getInstance();

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
        return 0;
    }

    @Override
    public void run(HttpContext ctx) {
        Session session = ctx.getSession();

        if (session.getId() == null) {
            return;
        }

        sessionService.set(session);
        ctx.getResponse().setCookie("DOBBY_SESSION", session.getId(), "/");
    }
}
