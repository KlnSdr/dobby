package pocs;

import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;

public class TestPostFilter implements Filter {
    @Override
    public String getName() {
        return "Test post filter";
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
        ctx.getResponse().setHeader("X-Test-Post-Filter", "Test post filter");
        return true;
    }
}
