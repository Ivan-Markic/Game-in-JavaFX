package hr.markic.fireandwater.multicast;

import hr.markic.fireandwater.controllers.GameScreenController;
import hr.markic.fireandwater.model.Player;
import hr.markic.fireandwater.utils.ByteUtils;
import javafx.application.Application;
import javafx.application.Platform;

import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread extends Thread {

    private static final String PROPERTIES_FILE = "socket.properties";
    private static final String CLIENT_PORT = "CLIENT_PORT";
    private static final Properties PROPERTIES = new Properties();

    static {
        try {
            PROPERTIES.load(new FileInputStream(PROPERTIES_FILE));
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private final GameScreenController controller;

    public ClientThread(GameScreenController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {

        try {
            Socket clientSocket = new Socket("localhost", Integer.parseInt(PROPERTIES.getProperty(CLIENT_PORT)));

            while (true) {


                ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

                Player player = (Player) objectInputStream.readObject();

                Platform.runLater(() -> {
                    controller.setPlayer(player);
                });

            }

        } catch (IOException | ClassNotFoundException e) {
            //e.printStackTrace();
            System.err.println("Server is down");
            System.exit(0);

        }
    }
}


