package dobby.filter.defaultFilter.post;

import dobby.io.response.Response;
import dobby.filter.post.PostFilter;

import java.util.HashMap;

public class CookiePostFilter implements PostFilter {
    @Override
    public String getName() {
        return "cookie";
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public void run(Response res) {
        HashMap<String, String> cookies = res.getCookies();

        for (String key : cookies.keySet()) {
            res.setHeader("Set-Cookie", key + "=" + cookies.get(key));
        }
    }
}
