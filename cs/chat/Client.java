package cs.chat;

import cs.chat.client.ClientFrame;
import cs.chat.client.ClientListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;

/**
 * The main Client class.
 *
 * It has the client part of the client-server architecture.
 */
public class Client implements ClientListener {
    /**
     * The output stream of the client socket
     */
    PrintWriter out;
    /**
     * The input stream of the client socket
     */
    BufferedReader in;
    /**
     * The socket
     */
    Socket sock;
    /**
     * The GUI window
     */
    ClientFrame frame;
    /**
     * Stores the error in an ack failure
     */
    String error;
    /**
     * The client's name
     */
    String name;
    boolean running = true;

    public static final int PORT = Server.PORT;

    public Client() {
        out = null;
        in = null;
        sock = null;
        frame = new ClientFrame();
        error = null;
        name = "";
    }

    public void showError(String error) {
        JOptionPane.showMessageDialog(frame, error, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Called to process a server's response
     * @param data The data from the server
     */
    public void callback(String data) {
        // process data
        String[] toks = data.split(" ", 2); // split at FIRST space only
        if (toks.length == 0) // empty message
            return;
        if (toks[0].equals("QUIT")) { // ex. QUIT John
            if (toks[1].equals(name)) {
                // quit
                running = false;
                return;
            } else {
                frame.addMessage("Server", toks[1] + " quit");
                // request user list
                sendAck("USERS");
            }
        }
        if (toks[0].equals("MESSAGE")) { // ex. MESSAGE John:Hello, world!
            String[] mtoks = toks[1].split(":", 2);
            frame.addMessage(mtoks[0], mtoks[1]);
        }
        if (toks[0].equals("JOIN")) { // ex. JOIN John
            frame.addMessage("Server", toks[1] + " joined");
            // request user list
            sendAck("USERS");
        }
        if (toks[0].equals("USERS")) { // ex. USERS CLEAR
            String[] utoks = toks[1].split(" ", 2);
            if (utoks[0].equals("CLEAR")) // server sends this first to clear
                frame.users.clear();
            else if (utoks[0].equals("ADD")) // server sends this after clearing
                frame.users.addElement(utoks[1]);
            // this is used to synchronize the server's user list with the clients
            // first, the server sends a CLEAR message
            // then sends the list of users with ADD
        }
    }

    /**
     * Send a message and waits for a response
     * @param data The data to send
     * @return Whether the data was acknowledged (if false, error will be set)
     */
    public boolean sendAck(String data) {
        out.println(data);
        out.flush();
        // read all pending messages until ACK
        while (true) {
            try {
                String res = in.readLine();
                if (res == null) // server quit/disconnect
                    throw new IOException();
                String[] toks = res.split(" ");
                if (toks.length == 0)
                    continue;
                if (toks[0].equals("ACK")) { // ACK
                    error = null;            // this is OK because
                    return true;             // TCP guarantees order
                }
                if (toks[0].equals("ERROR")) { // there was an error!
                    error = ""; // now we add the tokens back
                    for (int i = 1; i < toks.length; i++) {
                        if (i != 1)
                            error += " ";
                        error += toks[i];
                    }
                    return false;
                }
                callback(res);
            } catch (IOException ex) {
                // ABORT
                showError("IO ERROR!");
                System.exit(1);
                return false;
            }
        }
    }

    /**
     * Send a message and throw an exception if an error occurs
     * @param data
     */
    public void verifyAck(String data) {
        if (!sendAck(data))
            throw new RuntimeException(error);
    }

    /**
     * Main method
     */
    public void run() {
        frame.setVisible(true);
        String ip;
        do { // keep asking until OK
            ip = JOptionPane.showInputDialog(frame, "Enter Server IP", "Server IP", JOptionPane.QUESTION_MESSAGE);
            try {
                sock = new Socket(ip, PORT);
                in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())));
            } catch (UnknownHostException e) {
                showError("Unknown host; invalid IP");
            } catch (IOException e) {
                showError("IO error opening socket");
                return;
            }
        } while (sock == null);
        while (true) { // ask for name until OK
            name = JOptionPane.showInputDialog(frame, "Enter your name", "Name", JOptionPane.QUESTION_MESSAGE);
            if (!sendAck("CONNECT " + name)) {
                if (error.equals("FORMAT")) {
                    showError("Format error");
                } else if (error.equals("TAKEN")) {
                    showError("Name taken");
                } else if (error.equals("NAME")) {
                    showError("Name format error");
                } else {
                    showError("Error error");
                }
                try { // reconnect
                    sock = new Socket(ip, PORT);
                    in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())));
                } catch (UnknownHostException e) {
                    showError("Unknown host; invalid IP");
                    return;
                } catch (IOException e) {
                    showError("IO error opening socket");
                    return;
                }
            } else {
                break;
            }
        }
        frame.setTitle("Chat - " + name);
        frame.setListener(this);
        frame.addWindowListener(new WindowAdapter() {
            // send quit message when closing
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    out.println("QUIT");
                    out.flush(); // don't wait for ACK
                } catch (NullPointerException ee) {
                    System.exit(0);
                }
            }
        });
        while (running) {
            try { // read until QUIT us
                String data = in.readLine();
                if (data == null) {
                    data = "QUIT " + name;
                }
                callback(data);
            } catch (IOException e) {
                showError("Error!");
                return;
            }
        }
    }

    public static void main(String argv[]) {
        Client c = new Client();
        c.run();
        System.exit(0);
    }

    public void messageSend(String msg) {
        if (msg.equals("/quit")) // simple command processing
            out.println("QUIT");
        else
            out.println("MESSAGE " + msg);
        out.flush(); // make sure we send the message immediately
    }
}
