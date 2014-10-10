/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cs.chat.server;

import cs.chat.Server;
import java.net.*;
import java.io.*;
import java.util.logging.Logger;

/**
 *
 * @author s506571
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
    }
    protected Logger log() {
        return Logger.getLogger(sock.getInetAddress().getHostAddress() + "/" + name);
    }
    public void output(String data) {
        if (out != null) {
            out.println(data);
            out.flush();
        }
    }
    public String getClientName() {
        return name;
    }
    @Override
    public void run() {
        try {
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
            Logger.getLogger(sock.toString()).info("Connected with name " + name);
            // main loop
            while (true) {
                String cmdstr = in.readLine();
                if (cmdstr == null) break;
                String[] cmd_toks = cmdstr.split(" ", 2);
                String cmd = cmd_toks[0];
                if (cmd.equals("QUIT")) {
                    Logger.getLogger(sock.toString()).info(name + " quit");
                }
                if (cmd.equals("MESSAGE")) {
                    Logger.getLogger(sock.toString()).info("Message: " + cmd_toks[1]);
                    // send message to other people
                    serv.broadcast("MESSAGE " + name + ":" + cmd_toks[1]);
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
