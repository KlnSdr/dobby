package pocs;

import dobby.Request;
import dobby.util.logging.Logger;
import dobby.filter.pre.PreFilter;

public class TestPreFilter implements PreFilter {
    private final Logger LOGGER = new Logger(TestPreFilter.class);

    @Override
    public String getName() {
        return "Test pre filter";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void run(Request in) {
        LOGGER.debug("Test pre filter");
        LOGGER.debug(in.getPath());

        in.setHeader("X-Test-Pre-Filter", "Test pre filter");
    }
}
