package pocs.sub;

import dobby.annotations.Get;
import dobby.io.HttpContext;
import dobby.io.response.Response;
import dobby.util.logging.Logger;

import java.io.IOException;

public class SystemStatusResourceController {
    private final Logger LOGGER = new Logger(SystemStatusResourceController.class);

    @Get("/system/status")
    public void getSystemStatus(HttpContext context) throws IOException {
        Response res = context.getResponse();

        res.setBody("System is up and running!");
        res.setCookie("system_status", "up");
        res.setCookie("system_health", "good");
        res.send();
    }

    @Get("/system/wait")
    public void wait(HttpContext ctx) throws IOException {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            LOGGER.trace(e);
        }
        ctx.getResponse().setBody("System waited 10 seconds!");
        ctx.getResponse().send();
    }
}
