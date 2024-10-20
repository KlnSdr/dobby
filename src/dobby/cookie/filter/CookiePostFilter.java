package dobby.cookie.filter;

import dobby.cookie.Cookie;
import dobby.filter.Filter;
import dobby.filter.FilterOrder;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.response.Response;

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
        return FilterOrder.COOKIE_POST_FILTER.getOrder();
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
