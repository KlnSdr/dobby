package dobby;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ServerSocket server;
    private final ExecutorService threadPool;
    private boolean isRunning = false;

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
        isRunning = true;
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
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        Request req = Request.parse(in);
        RouteManager.getInstance().getHandler(req.getType(), req.getPath()).handle(req, new Response(client));
    }

    public void stop() {
        isRunning = false;
    }

    public void addRoute(RequestTypes type, String route, IRequestHandler handler) {
        RouteManager.getInstance().add(type, route, handler);
    }

    public void get(String route, IRequestHandler handler) {
        addRoute(RequestTypes.GET, route, handler);
    }

    public void post(String route, IRequestHandler handler) {
        addRoute(RequestTypes.POST, route, handler);
    }
}
