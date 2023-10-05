package pocs.sub;

import dobby.annotations.Get;
import dobby.cookie.Cookie;
import dobby.io.HttpContext;
import dobby.io.response.Response;
import dobby.util.Json;
import dobby.util.logging.Logger;

import java.io.IOException;

public class SystemStatusResourceController {
    private final Logger LOGGER = new Logger(SystemStatusResourceController.class);

    @Get("/system/status")
    public void getSystemStatus(HttpContext context) {
        Response res = context.getResponse();

        Json payload = new Json();
        payload.setString("status", "up");
        payload.setInt("code", 200);

        Json subPayload = new Json();
        subPayload.setString("status", "good");
        subPayload.setInt("code", 200);

        payload.setJson("data", subPayload);

        res.setBody(payload.toString());

        Cookie cookie_sysetmStatus = new Cookie("system_status", "up");
        res.setCookie("system_status", cookie_sysetmStatus);

        Cookie cookie_systemHealth = new Cookie("system_health", "good");
        res.setCookie("system_health", cookie_systemHealth);
    }

    @Get("/system/wait")
    public void wait(HttpContext ctx) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            LOGGER.trace(e);
        }
        ctx.getResponse().setBody("System waited 10 seconds!");
    }
}
