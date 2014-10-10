package cs.chat;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

    PrintWriter out;
    BufferedReader in;
    Socket cSocket;

    public static final int PORT = Server.PORT;

    public Client() {
        out = null;
        in = null;
        cSocket = null;
    }

    public void run() {
        // connect to server
        try {
            Scanner scan = new Scanner(System.in);
            System.out.print("Enter IP: ");
            cSocket = new Socket(scan.nextLine(), PORT);
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(cSocket.getOutputStream())));
            in = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
            System.out.println("Enter your name"); //prompt
            out.println("CONNECT " + scan.nextLine());
            out.flush();
            String s = in.readLine();
            System.out.println(s);

        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        // message thread
        Thread messageThread = new Thread() {
            @Override
            public void run() {
                // read input from console
                // and then write "MESSAGE [input]" to the socket
                // if input is /quit
                // then write "QUIT" to the socket
                Scanner scan = new Scanner(System.in);
                while (true) {
                    String input = scan.nextLine();
                    if(input.equalsIgnoreCase("/quit"))
                    {
                       out.println("QUIT");
                       out.flush();
                       break;
                    }
                    out.println("MESSAGE " + input);
                    out.flush();
                }
                try {
                    cSocket.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        messageThread.start();
        do {
            // read from in
            // and print to System.out
            try {
                String input = in.readLine();
                if(input == null) break;
                System.out.println(input);
            }
            catch(IOException e) {
                e.printStackTrace();
                break;
            }
        } while(true);
    }

    public static void main(String argv[]) {
        new Client().run();
    }
}
