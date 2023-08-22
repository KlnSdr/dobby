import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = Server.newInstance();
        server.start();
    }
}
