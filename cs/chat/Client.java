package cs.chat;

import cs.chat.client.ClientFrame;
import cs.chat.client.ClientListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;

public class Client implements ClientListener {

    PrintWriter out;
    BufferedReader in;
    Socket sock;
    ClientFrame frame;
    String error;
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
    
    public void callback(String data) {
        // process data
        String[] toks = data.split(" ", 2);
        if (toks.length == 0)
            return;
        if (toks[0].equals("QUIT")) {
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
        if (toks[0].equals("MESSAGE")) {
            String[] mtoks = toks[1].split(":", 2);
            frame.addMessage(mtoks[0], mtoks[1]);
        }
        if (toks[0].equals("JOIN")) {
            frame.addMessage("Server", toks[1] + " joined");
            // request user list
            sendAck("USERS");
        }
        if (toks[0].equals("USERS")) {
            String[] utoks = toks[1].split(" ", 2);
            if (utoks[0].equals("CLEAR"))
                frame.users.clear();
            else if (utoks[0].equals("ADD"))
                frame.users.addElement(utoks[1]);
        }
    }

    public boolean sendAck(String data) {
        out.println(data);
        out.flush();
        while (true) {
            try {
                String res = in.readLine();
                if (res == null)
                    throw new IOException();
                String[] toks = res.split(" ");
                if (toks.length == 0)
                    continue;
                if (toks[0].equals("ACK")) {
                    error = null;
                    return true;
                }
                if (toks[1].equals("ERROR")) {
                    System.out.println(res);
                    error = "";
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

    public void verifyAck(String data) {
        if (!sendAck(data))
            throw new RuntimeException(error);
    }

    public void run() {
        frame.setVisible(true);
        String ip = JOptionPane.showInputDialog(frame, "Enter Server IP", "Server IP", JOptionPane.QUESTION_MESSAGE);
        while (sock == null) {
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
        }
        while (true) {
            name = JOptionPane.showInputDialog(frame, "Enter your name", "Name", JOptionPane.QUESTION_MESSAGE);
            if (!sendAck("CONNECT " + name)) {
                System.out.println("Connect error: " + error);
                if (error.equals("FORMAT")) {
                    showError("Format error");
                } else if (error.equals("TAKEN")) {
                    showError("Name taken");
                } else if (error.equals("NAME")) {
                    showError("Name format error");
                } else {
                    showError("Error error");
                }
                try {
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
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    out.println("QUIT");
                    out.flush();
                } catch (NullPointerException ee) {
                    System.exit(0);
                }
            }
        });
        while (running) {
            try {
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
        if (msg.equals("/quit"))
            out.println("QUIT");
        else
            out.println("MESSAGE " + msg);
        out.flush();
    }
}
