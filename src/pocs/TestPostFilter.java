package pocs;

import dobby.io.response.Response;
import dobby.filter.post.PostFilter;

public class TestPostFilter implements PostFilter {
    @Override
    public String getName() {
        return "Test post filter";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void run(Response in) {
        in.setHeader("X-Test-Post-Filter", "Test post filter");
    }
}
