package com.klnsdr.dobby.session.filter;

import com.klnsdr.dobby.cookie.Cookie;
import com.klnsdr.dobby.filter.Filter;
import com.klnsdr.dobby.filter.FilterType;
import com.klnsdr.dobby.io.HttpContext;
import com.klnsdr.dobby.session.service.SessionService;

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
        Cookie sessionId = ctx.getRequest().getCookie("DOBBY_SESSION");

        if (sessionId == null) {
            return true;
        }

        sessionService.find(sessionId.getValue()).ifPresent(ctx::setSession);
        return true;
    }
}
