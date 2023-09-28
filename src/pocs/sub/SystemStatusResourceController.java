package pocs.sub;

import dobby.annotations.Get;
import dobby.io.HttpContext;
import dobby.io.response.Response;

import java.io.IOException;

public class SystemStatusResourceController {
    @Get("/system/status")
    public void getSystemStatus(HttpContext context) throws IOException {
        Response res = context.getResponse();

        res.setBody("System is up and running!");
        res.setCookie("system_status", "up");
        res.setCookie("system_health", "good");
        res.send();
    }
}
