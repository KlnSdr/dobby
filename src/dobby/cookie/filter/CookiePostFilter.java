package dobby.cookie.filter;

import dobby.filter.Filter;
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
        return 1;
    }

    @Override
    public boolean run(HttpContext ctx) {
        Response res = ctx.getResponse();

        HashMap<String, String> cookies = res.getCookies();

        for (String key : cookies.keySet()) {
            res.setHeader("Set-Cookie", key + "=" + cookies.get(key));
        }

        return true;
    }
}
