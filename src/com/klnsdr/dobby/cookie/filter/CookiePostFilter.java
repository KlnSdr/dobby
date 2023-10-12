package com.klnsdr.dobby.cookie.filter;

import com.klnsdr.dobby.cookie.Cookie;
import com.klnsdr.dobby.filter.Filter;
import com.klnsdr.dobby.filter.FilterType;
import com.klnsdr.dobby.io.HttpContext;
import com.klnsdr.dobby.io.response.Response;

import java.util.HashMap;

/**
 * Filter for setting cookies
 */
public class CookiePostFilter implements Filter {
    @Override
    public String getName() {
        return "cookie";
    }

    @Override
    public FilterType getType() {
        return FilterType.POST;
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public boolean run(HttpContext ctx) {
        Response res = ctx.getResponse();

        HashMap<String, Cookie> cookies = res.getCookies();

        for (String key : cookies.keySet()) {
            res.setHeader("Set-Cookie", cookies.get(key).toString());
        }

        return true;
    }
}
