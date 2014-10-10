package cs.chat;

import cs.chat.server.WorkerThread;
import java.io.*;
import java.net.*;
import java.util.Collection;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.*;

public class Server {
    public static final int PORT = 6429;
    public static final int BACKLOG = 5;
    private ServerSocket ss;
    private AtomicBoolean running = new AtomicBoolean();
    private final TreeMap<String, WorkerThread> threads;

    public Server() {
        threads = new TreeMap<String, WorkerThread>();
        Logger l = log();
        l.setUseParentHandlers(false);
        NotStupidLogFormatter formatter = new NotStupidLogFormatter();
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        l.addHandler(handler);
    }

    public static void main(String argv[]) {
        new Server().listen(Server.PORT);
    }

    public boolean nameTaken(String name) {
        synchronized (threads) {
            return threads.get(name) != null;
        }
    }

    public WorkerThread getThread(String name) {
        synchronized (threads) {
            return threads.get(name);
        }
    }

    public Collection<WorkerThread> getThreads() {
        synchronized (threads) {
            return threads.values();
        }
    }

    public TreeMap<String, WorkerThread> getThreadLock() {
        return threads;
    }

    public void broadcast(String data) {
        synchronized (threads) {
            for (WorkerThread t : threads.values()) {
                t.output(data);
            }
        }
    }

    public void threadFinished(WorkerThread t, Socket s) {
        String sid = s.toString();
        try {
            if (t.error != null)
                throw t.error;
        } catch (IOException e) {
            t.log().log(Level.WARNING, "IO error, probably disconnect", e);
        } catch (Exception e) {
            t.log().log(Level.WARNING, "Client error", e);
        }finally {
            t.log().info("Client left");
            threads.remove(t.getClientName());
            try {
                if (!s.isClosed())
                    s.close();
            } catch (IOException e) {
                t.log().info("Error closing socket");
            }
        }
    }

    public void listen(int port) {
        running.set(true);
        try {
            ss = new ServerSocket(port, BACKLOG);
        } catch (IOException e) {
            System.err.println("Error initializing:");
            e.printStackTrace();
            System.exit(1);
        }
        log().info("Listening on " + ss.getInetAddress().getHostAddress() + ":" + ss.getLocalPort());
        log().info("Accepting clients");
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
                    log().log(Level.SEVERE, "Error closing server socket", ex);
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
                if (s == null)
                    throw new SocketException();
            } catch (IOException ex) {
                if (ex.getMessage() != null) {
                    log().log(Level.SEVERE, "Error accepting client", ex);
                    log().warning("Shutting down because of errors");
                } else {
                    log().info("Shutting down normally");
                }
                running.set(false);
                break;
            }
            WorkerThread t = new WorkerThread(s, this);
            t.log().info("Client at " + s.getInetAddress().getHostAddress() + " connected");
            t.start();
        }
        try {
            if (!ss.isClosed())
                ss.close();
        } catch (IOException ex) {
            log().log(Level.SEVERE, "Error closing socket", ex);
        }
    }

    public void putThread(String name, WorkerThread aThis) {
        threads.put(name, aThis);
    }

    public Logger log() {
        return Logger.getLogger("server");
    }
}
