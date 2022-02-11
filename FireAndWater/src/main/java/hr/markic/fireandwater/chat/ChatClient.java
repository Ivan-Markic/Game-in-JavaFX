package hr.markic.fireandwater.chat;

import hr.markic.fireandwater.controllers.GameScreenController;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient extends Thread {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void sendMessage(String msg) throws IOException {
        out.println(msg);
        String answer = in.readLine();
        Platform.runLater(() -> {
            GameScreenController.getInstance().getAnswer(answer);
        });
    }



    @Override
    public void run() {
        try {
            startConnection("localhost", 6666);
        } catch (IOException e) {
            System.out.println("Can not connect to server");
            System.exit(0);
        }
    }
}
