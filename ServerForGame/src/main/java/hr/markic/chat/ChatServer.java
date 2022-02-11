package hr.markic.chat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServer extends Thread {

    private ServerSocket serverSocket;
    private Socket client1;
    private Socket client2;
    private PrintWriter outClient1;
    private PrintWriter outClient2;
    private ObjectOutputStream objectWriter1;
    private ObjectOutputStream objectWriter2;
    private BufferedReader inClient1;
    private BufferedReader inClient2;
    private int PORT = 6666;

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);

            client1 = serverSocket.accept();

            client2 = serverSocket.accept();

            outClient1 = new PrintWriter(client1.getOutputStream(), true);
            outClient2 = new PrintWriter(client2.getOutputStream(), true);

            objectWriter1 = new ObjectOutputStream(
                    client1.getOutputStream());

            objectWriter2 = new ObjectOutputStream(
                    client2.getOutputStream());


            inClient1 = new BufferedReader(new InputStreamReader(
                    client1.getInputStream()));

            inClient2 = new BufferedReader(new InputStreamReader(
                    client2.getInputStream()));

            while (true) {


                String client1Answer = inClient1.readLine();
                String client2Answer = inClient2.readLine();


                if (client1Answer != null) {
                    outClient2.println(client1Answer);
                }

                if (client2Answer != null) {
                    outClient1.println(client2Answer);
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null,
                    ex);
        }
    }

    public void stopServer() {
        System.out.println("Server je zaustavljen!");
        try {
            inClient1.close();
            inClient2.close();
            outClient1.close();
            outClient2.close();
            client1.close();
            client2.close();
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null,
                    ex);
        }
    }
}
