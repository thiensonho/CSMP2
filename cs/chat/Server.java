package cs.chat;

import cs.chat.server.WorkerThread;
import java.io.*;
import java.net.*;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.*;

/**
 * Server class the implements the server part of the client-server architecture
 */
public class Server {
    // random number
    public static final int PORT = 6429;
    // number of clients that can wait for accept
    public static final int BACKLOG = 5;
    // server socket
    private ServerSocket ss;
    // thread-safety
    private AtomicBoolean running = new AtomicBoolean();
    // map of name to thread
    private final TreeMap<String, WorkerThread> threads;
    // list of messages in cache
    public final List<String> cache;
    public static final int CACHE_SIZE = 100;

    public Server() {
        threads = new TreeMap<String, WorkerThread>();
        // make the cache synchronized for thread safety
        cache = Collections.synchronizedList(new LinkedList<String>());
        // let us send to the nicely formatted logger
        Logger l = log();
        l.setUseParentHandlers(false); // don't use the default
        NotStupidLogFormatter formatter = new NotStupidLogFormatter(); // use ours
        ConsoleHandler handler = new ConsoleHandler(); // print to console
        handler.setFormatter(formatter);
        l.addHandler(handler);
    }

    public static void main(String argv[]) {
        new Server().listen(Server.PORT);
    }

    public boolean nameTaken(String name) {
        synchronized (threads) { // thread safety
            return threads.get(name) != null; // self-explanatory
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

    public Collection<String> getNames() {
        synchronized (threads) {
            return threads.keySet();
        }
    }

    public TreeMap<String, WorkerThread> getThreadLock() {
        return threads;
    }

    /**
     * Send to all of the clients
     * @param data The data to send
     */
    public void broadcast(String data) {
        synchronized (threads) {
            for (WorkerThread t : threads.values()) {
                t.output(data);
            }
        }
    }

    /**
     * Run whenever a thread is finished (client quit/disconnect)
     * @param t The thread
     * @param s The socket
     */
    public void threadFinished(WorkerThread t, Socket s) {
        try {
            if (t.error != null)
                throw t.error; // better than a string of instanceof
        // now we log some errors
        } catch (IOException e) {
            t.log().log(Level.WARNING, "IO error, probably disconnect", e);
        } catch (Exception e) {
            t.log().log(Level.WARNING, "Client error", e);
        } finally {
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

    /**
     * Main server method
     * @param port The port on which to listen
     */
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
        final Scanner sss = new Scanner(System.in); // let us quit using command line
        new Thread(new Runnable() { // command-line input thread
            public void run() {
                while (sss.hasNextLine()) {
                    String ssss = sss.nextLine();
                    if (ssss.equals("quit")) // weak quit, wait for threads to finish
                        break;
                    if (ssss.equals("force")) { // force quit
                        for (WorkerThread t : getThreads()) {
                            t.kill();
                        }
                        break;
                    }
                    System.out.println(ssss); // print the string
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
                    s = ss.accept(); // get a socket (client) from waiting list
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
                    // thrown when we try to quit, not an error
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

    /**
     * Put a thread into the map
     * @param name Name of the client
     * @param aThis the thread
     */
    public void putThread(String name, WorkerThread aThis) {
        threads.put(name, aThis);
    }

    public Logger log() {
        return Logger.getLogger("server");
    }
}
