package cs.chat;

import java.io.*;
import java.net.*;

public class Client {

    public static final int PORT = Server.PORT;

    public static void main(String argv[]) {
        // connect to server
        try {
            Socket cSocket = new Socket("10.21.50.118", PORT);
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(cSocket.getOutputStream())));
            BufferedReader in = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
            out.println("CONNECT ThienSon");
            out.flush();
            String s = in.readLine();
            System.out.println(s);
            cSocket.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
