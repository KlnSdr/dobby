package pocs;

import dobby.ResponseCodes;
import dobby.Server;

public class Main {
    public static void main(String[] args) {
        Server server = Server.newInstance();
        server.get("/setShips", (req, res) -> {
            res.setCode(ResponseCodes.OK);
            res.send();
        });
    }
}
