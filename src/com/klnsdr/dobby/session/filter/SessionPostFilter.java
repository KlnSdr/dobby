package com.klnsdr.dobby.session.filter;

import com.klnsdr.dobby.cookie.Cookie;
import com.klnsdr.dobby.filter.Filter;
import com.klnsdr.dobby.filter.FilterType;
import com.klnsdr.dobby.io.HttpContext;
import com.klnsdr.dobby.session.Session;
import com.klnsdr.dobby.session.service.SessionService;

/**
 * The SessionPostFilter class is used to save the session to the database and set the session cookie
 */
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
