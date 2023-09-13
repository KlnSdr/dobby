package pocs;

import dobby.Request;
import dobby.filter.pre.PreFilter;

public class TestPreFilter implements PreFilter {
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
        System.out.println("Test pre filter");
        System.out.println(in.getPath());

        in.setHeader("X-Test-Pre-Filter", "Test pre filter");
    }
}
