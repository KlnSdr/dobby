import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private ServerSocket server;
    private OutputStream out;
    private BufferedReader in;
    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.run();
    }

    private void run() throws IOException {
        server = new ServerSocket(3000);
        while (true) {
            Socket client = server.accept();

            out = client.getOutputStream();
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("'" + line + "'");
                if (line.isEmpty()) {
                    break;
                }
            }
            System.out.println("end");

        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n" +
                "Content-Length: 21\r\n" +
                "\r\n" +
                "<h1>Hello World</h1>";
            out.write(response.getBytes());
            client.close();
        }
    }
}
