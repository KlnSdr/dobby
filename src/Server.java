import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ServerSocket server;
    private final ExecutorService threadPool;
    private boolean isRunning = true;

    private Server(int port, int threadCount) throws IOException {
        server = new ServerSocket(port);
        threadPool = Executors.newFixedThreadPool(threadCount);
    }

    public static Server newInstance() throws IOException {
        return newInstance(3000, 10);
    }

    public static Server newInstance(int port, int threadCount) throws IOException {
        return new Server(port, threadCount);
    }

    public void start() throws IOException {
        acceptConnections();
    }

    private void acceptConnections() throws IOException {
        while (isRunning) {
            Socket client = server.accept();
            threadPool.execute(() -> {
                try {
                    handleConnection(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void handleConnection(Socket client) throws IOException {
        OutputStream out = client.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        Request req = Request.parse(in);

        if (req.getType() == RequestTypes.UNKNOWN) {
            Response res = new Response();
            res.setCode(ResponseCodes.METHOD_NOT_ALLOWED);
            out.write(res.build());
            client.close();
            return;
        }

        if (req.getPath().equals("/")) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Response res = new Response();
        res.setCode(ResponseCodes.OK);
        res.setHeader("Content-Type", "text/html");
        res.setHeader("Connection", "close");
        res.setBody("<h1>Hello World</h1>");

        out.write(res.build());
        client.close();
    }

    public void stop() {
        isRunning = false;
    }
}
