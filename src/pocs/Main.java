package pocs;

import dobby.Server;
import dobby.io.response.ResponseCodes;

public class Main {
    public static void main(String[] args) {
        Server server = Server.newInstance();
        server.get("/setShips", (ctx) -> {
            ctx.getResponse().setCode(ResponseCodes.OK);
            ctx.getResponse().send();
        });
    }
}
