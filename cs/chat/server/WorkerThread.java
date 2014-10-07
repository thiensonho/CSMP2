/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cs.chat.server;

import cs.chat.Server;
import java.net.*;
import java.io.*;

/**
 *
 * @author s506571
 */
public class WorkerThread extends Thread {
    private Server serv;
    private String name;
    private Socket sock;
    public Exception error;
    public WorkerThread(Socket ss, Server s) {
        serv = s;
        sock = ss;
        error = null;
    }
    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())));
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            // CONNECT name
            String conn = in.readLine();
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
        } catch (Exception e) {
            // oh no
            error = e;
        } finally {
            serv.threadFinished(this, sock);
        }
    }
}
