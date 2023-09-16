package pocs.sub;

import dobby.Request;
import dobby.Response;
import dobby.annotations.Get;

import java.io.IOException;

public class SystemStatusResourceController {
    @Get("/system/status")
    public void getSystemStatus(Request req, Response res) throws IOException {
        res.setBody("System is up and running!");
        res.setCookie("system_status", "up");
        res.setCookie("system_health", "good");
        res.send();
    }
}
