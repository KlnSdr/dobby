package pocs;

import dobby.ResponseCodes;
import dobby.Server;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = Server.newInstance();
        server.get("/setShips", (req, res) -> {
            res.setCode(ResponseCodes.OK);
            res.send();
        });
        server.start();
    }
}
