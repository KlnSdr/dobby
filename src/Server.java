import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final ServerSocket server;
    private boolean isRunning = true;

    private Server(int port) throws IOException {
        server = new ServerSocket(port);
    }

    public static Server newInstance() throws IOException {
        return newInstance(3000);
    }

    public static Server newInstance(int port) throws IOException {
        return new Server(port);
    }

    public void start() throws IOException {
        acceptConnections();
    }

    private void acceptConnections() throws IOException {
        while (isRunning) {
            Socket client = server.accept();

            OutputStream out = client.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            Request req = Request.parse(in);

            if (req.getType() == RequestTypes.UNKNOWN) {
                Response res = new Response();
                res.setCode(ResponseCodes.METHOD_NOT_ALLOWED);
                out.write(res.build());
                client.close();
                continue;
            }

            Response res = new Response();
            res.setCode(ResponseCodes.OK);
            res.setHeader("Content-Type", "text/html");
            res.setHeader("Connection", "close");
            res.setBody("<h1>Hello World</h1>");

            out.write(res.build());
            client.close();
        }
    }

    public void stop() {
        isRunning = false;
    }
}
