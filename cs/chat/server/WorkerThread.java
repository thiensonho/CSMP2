package cs.chat.server;

import cs.chat.NotStupidLogFormatter;
import cs.chat.Server;
import java.net.*;
import java.io.*;
import java.util.logging.*;

/**
 * Worker thread for each client
 */
public class WorkerThread extends Thread {
    private Server serv;
    private String name;
    private Socket sock;
    private PrintWriter out;
    private BufferedReader in;
    public Exception error;
    public WorkerThread(Socket ss, Server s) {
        serv = s;
        sock = ss;
        error = null;
        in = null;
        out = null;
        Logger l = log(); // logger stuff
        l.setUseParentHandlers(false);
        NotStupidLogFormatter formatter = new NotStupidLogFormatter();
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        for (Handler h : l.getHandlers())
                l.removeHandler(h);
        l.addHandler(handler);
    }
    public void kill() {
        try {
            sock.close();
        } catch (IOException e) {
            // do nothing
        }
    }
    public Logger log() {
        return Logger.getLogger(sock.getInetAddress().getHostAddress() + "/" + name);
    }
    public void output(String data) { // put a thing and flush it
        if (out != null) {
            out.println(data);
            out.flush();
        }
    }
    public String getClientName() {
        return name;
    }
    /**
     * Main run method
     */
    @Override
    public void run() {
        try {
            // strings
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())));
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            // CONNECT name
            String conn = in.readLine();
            if (conn == null) throw new Exception("Client didn't send data");
            if (!conn.startsWith("CONNECT ")) {
                // format error
                out.println("ERROR FORMAT");
                out.flush();
                throw new Exception("Format error");
            }
            name = conn.substring("CONNECT ".length());
            if (serv.nameTaken(name)) {
                out.println("ERROR TAKEN");
                out.flush();
                name = null; // make sure we don't delete the record
                throw new Exception("Name taken");
            }
            if (name.indexOf((int)':') != -1) {
                out.println("ERROR NAME");
                out.flush();
                throw new Exception("Name error");
            }
            out.println("ACK");
            out.flush();
            serv.putThread(name, this);
            // send cache
            synchronized (serv.cache) {
                for (String msg : serv.cache) {
                    out.println("MESSAGE " + msg);
                }
            }
            out.flush();
            // broadcast join
            serv.broadcast("JOIN " + name);
            Logger l = log(); // logger!
            l.setUseParentHandlers(false);
            NotStupidLogFormatter formatter = new NotStupidLogFormatter();
            ConsoleHandler handler = new ConsoleHandler();
            handler.setFormatter(formatter);
            for (Handler h : l.getHandlers())
                l.removeHandler(h); // don't add handler more than once
            l.addHandler(handler);
            log().info("Connected with name " + name);
            // main loop
            while (true) {
                String cmdstr = in.readLine();
                if (cmdstr == null) break;
                String[] cmd_toks = cmdstr.split(" ", 2); // see Client stuff
                String cmd = cmd_toks[0];
                if (cmd.equals("QUIT")) {
                    log().info(name + " quit");
                    serv.broadcast("QUIT " + name);
                    break;
                }
                if (cmd.equals("MESSAGE")) {
                    log().info("Message: " + cmd_toks[1]);
                    out.println("ACK");
                    out.flush();
                    // send message to other people
                    String msg = name + ":" + cmd_toks[1];
                    synchronized (serv.cache) {
                        serv.cache.add(msg);
                        if (serv.cache.size() > Server.CACHE_SIZE) {
                            serv.cache.remove(0);
                        }
                    }
                    serv.broadcast("MESSAGE " + msg);
                }
                if (cmd.equals("USERS")) {
                    log().info("Sending users");
                    out.println("ACK");
                    out.flush();
                    // send USERS CLEAR
                    // and  USERS ADD name
                    out.println("USERS CLEAR");
                    for (String n : serv.getNames()) {
                        out.println("USERS ADD " + n);
                    }
                    out.flush();
                }
            }
        } catch (Exception e) {
            // oh no
            error = e;
        } finally {
            in = null;
            out = null;
            serv.threadFinished(this, sock);
        }
    }
}
