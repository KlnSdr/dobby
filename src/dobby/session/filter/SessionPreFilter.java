package dobby.session.filter;

import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.session.service.SessionService;

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
    public void run(HttpContext ctx) {
        String sessionId = ctx.getRequest().getCookie("DOBBY_SESSION");

        if (sessionId == null) {
            return;
        }

        sessionService.find(sessionId).ifPresent(ctx::setSession);
    }
}
