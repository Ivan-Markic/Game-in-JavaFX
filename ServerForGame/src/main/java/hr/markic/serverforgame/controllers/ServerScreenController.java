package hr.markic.serverforgame.controllers;

import hr.markic.chat.ChatServer;
import hr.markic.fireandwater.model.Player;
import hr.markic.fireandwater.model.PlayerType;
import hr.markic.serverforgame.multicast.ServerThread;
import hr.markic.serverforgame.utils.SerializationFileUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class ServerScreenController{

    public ServerThread getServerThread() {
        return serverThread;
    }

    private ServerThread serverThread;

    private ChatServer chatServer;

    @FXML
    private Button btnStart;

    public static boolean isIsDataSharing() {
        return isDataSharing;
    }

    public static void setIsDataSharing(boolean isDataSharing) {
        ServerScreenController.isDataSharing = isDataSharing;
    }

    private static boolean isDataSharing = false;


    public void StartServer() {



        serverThread = new ServerThread(this);
        serverThread.setDaemon(true);
        serverThread.start();

        btnStart.setDisable(true);

        startGame();

        new Timeline(
                new KeyFrame(
                        Duration.millis(1000),
                        refreshEvent -> {
                            serverThread.trigger(new Player(PlayerType.BOY, "Fire"));
                        }
                )
        );

        initChatServer();
    }

    private void initChatServer() {
        chatServer = new ChatServer();
        chatServer.setDaemon(true);
        chatServer.start();
    }

    private void startGame() {

        if (SerializationFileUtil.isFileExists()) {
            List<Object> objects = SerializationFileUtil.readFromFile();
            List<Player> players = new ArrayList();

            for(Object object : objects)
            {
                players.add((Player) object);
            }

            for( Player player : players)
            {
                serverThread.trigger(player);
            }
        }

    }


    //Stavio da bude synchronized
    public synchronized void shareDataToClients(Player player) {

        System.out.println("Nit ušla");
        System.out.println(isDataSharing);
        while (isDataSharing == true) {

            try {
                System.out.println("Nit mora čekati");
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        isDataSharing = true;
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        serverThread.trigger(player);
        System.out.println("Nit zapisuje podatke");
        isDataSharing = false;
        notifyAll();
    }

    public void ExitApp() {

        System.err.println("Server is shutting down...");
        System.exit(0);
    }
}
