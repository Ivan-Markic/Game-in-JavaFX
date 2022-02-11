package hr.markic.serverforgame.multicast;

import hr.markic.fireandwater.model.Player;
import hr.markic.fireandwater.model.PlayerType;
import hr.markic.serverforgame.controllers.ServerScreenController;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThread extends Thread {


    public static final String PROPERTIES_FILE = "socket.properties";
    private static final String CLIENT_PORT = "CLIENT_PORT";
    private static final Properties PROPERTIES = new Properties();

    static {
        try {
            PROPERTIES.load(new FileInputStream(PROPERTIES_FILE));
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private final ServerScreenController controller;

    private final LinkedBlockingDeque<Player> players = new LinkedBlockingDeque<>();

    private int initializeGame = 0;

    public ServerThread(ServerScreenController controller) {
        this.controller = controller;
    }


    public void trigger(Player player) {
        players.add(player);
    }


    @Override
    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(PROPERTIES.getProperty(CLIENT_PORT)));
        ) {
            Socket client1 = serverSocket.accept();
            Socket client2 = serverSocket.accept();

            setReceivingDataThread(1234);
            setReceivingDataThread(1235);

            while (true) {

                if (!players.isEmpty()) {
                    if (initializeGame < 2) {
                        sendToClient(client1);
                        sendToClient(client2);
                        initializeGame++;

                    } else if (players.getFirst().getType() == PlayerType.BOY){
                        sendToClient(client2);
                    } else {
                        sendToClient(client1);
                    }
                    players.remove(players.getFirst());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setReceivingDataThread(int port) {
        ReceivingDataThread receivingDataThread = new ReceivingDataThread(controller, port);
        receivingDataThread.setDaemon(true);
        receivingDataThread.start();
    }

    private void sendToClient(Socket clientSocket) {

        try {
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.writeObject(players.getFirst());

            out.flush();

        } catch (IOException e) {
            controller.ExitApp();
        }

    }

}
