package dobby.io;

import java.io.IOException;
import java.net.Socket;

public interface PureRequestHandler {
    void onRequest(Socket socket) throws IOException;
}
