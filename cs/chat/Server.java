package cs.chat;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    public static final int PORT = 6429;
    public static final int BACKLOG = 5;
    private ServerSocket ss;

    public static void main(String argv[]) {
        new Server().listen(Server.PORT);
    }

    public void listen(int port) {
        try {
            ss = new ServerSocket(port, BACKLOG);
        } catch (IOException e) {
            System.err.println("Error initializing:");
            e.printStackTrace();
            System.exit(1);
        }
        Logger.getLogger("server").info("Listening on " + ss.getInetAddress().toString() + ":" + ss.getLocalPort());
        try {
            ss.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
