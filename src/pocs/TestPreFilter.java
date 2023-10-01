package pocs;

import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.util.logging.Logger;

public class TestPreFilter implements Filter {
    private final Logger LOGGER = new Logger(TestPreFilter.class);

    @Override
    public String getName() {
        return "Test pre filter";
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
        LOGGER.debug("Test pre filter");
        LOGGER.debug(ctx.getRequest().getPath());

        ctx.getRequest().setHeader("X-Test-Pre-Filter", "Test pre filter");
        return true;
    }
}
