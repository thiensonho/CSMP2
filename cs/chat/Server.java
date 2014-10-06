package cs.chat;

import java.io.*;
import java.net.*;

public class Server {
    private static final int PORT = 6429;
    private ServerSocket ss;

    public static void main(String argv[]) {
        new Server().listen(Server.PORT);
    }

    public void listen(int port) {
        try {
            ss = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Error initializing:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
