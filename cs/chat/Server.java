package cs.chat;

import cs.chat.server.WorkerThread;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    public static final int PORT = 6429;
    public static final int BACKLOG = 5;
    private ServerSocket ss;
    private AtomicBoolean running = new AtomicBoolean();
    private TreeMap<String, WorkerThread> threads;

    public static void main(String argv[]) {
        new Server().listen(Server.PORT);
    }

    public boolean nameTaken(String name) {
        return threads.get(name) != null;
    }

    public WorkerThread getThread(String name) {
        return threads.get(name);
    }

    public void threadFinished(WorkerThread t, Socket s) {
        String sid = s.toString();
        try {
            if (t.error != null)
                throw t.error;
        } catch (IOException e) {
            Logger.getLogger(sid).log(Level.WARNING, "IO error, probably disconnect", e);
        } catch (Exception e) {
            Logger.getLogger(sid).log(Level.WARNING, "Client error", e);
        }finally {
            Logger.getLogger(sid).info("Client left");
            try {
                if (!s.isClosed())
                    s.close();
            } catch (IOException e) {
                Logger.getLogger(sid).info("Error closing socket");
            }
        }
    }

    public void listen(int port) {
        threads = new TreeMap<String, WorkerThread>();
        running.set(true);
        try {
            ss = new ServerSocket(port, BACKLOG);
        } catch (IOException e) {
            System.err.println("Error initializing:");
            e.printStackTrace();
            System.exit(1);
        }
        Logger.getLogger("server").info("Listening on " + ss.getInetAddress().getHostAddress() + ":" + ss.getLocalPort());
        Logger.getLogger("server").info("Accepting clients");
        final Scanner sss = new Scanner(System.in);
        new Thread(new Runnable() {
            public void run() {
                while (sss.hasNextLine()) {
                    String ssss = sss.nextLine();
                    if (ssss.equals("quit"))
                        break;
                    System.out.println(ssss);
                }
                try {
                    ss.close();
                } catch (IOException ex) {
                    Logger.getLogger("server").log(Level.SEVERE, "Error closing server socket", ex);
                }
                running.set(false);
            }
        }).start();
        while (running.get()) {
            Socket s = null;
            try {
                try {
                    s = ss.accept();
                } catch (SocketException e) {
                    if (running.get())
                        throw e;
                }
            } catch (IOException ex) {
                Logger.getLogger("server").log(Level.SEVERE, "Error accepting client", ex);
                Logger.getLogger("server").warning("Shutting down because of errors");
                running.set(false);
                break;
            }
            String sid = s.toString();
            Logger.getLogger(sid).info("Client at " + s.getInetAddress().getHostAddress() + " connected");
            WorkerThread t = new WorkerThread(s, this);
            t.start();
        }
        try {
            if (!ss.isClosed())
                ss.close();
        } catch (IOException ex) {
            Logger.getLogger("server").log(Level.SEVERE, "Error closing socket", ex);
        }
    }

    public void putThread(String name, WorkerThread aThis) {
        threads.put(name, aThis);
    }
}
