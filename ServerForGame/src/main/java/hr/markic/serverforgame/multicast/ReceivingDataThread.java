package hr.markic.serverforgame.multicast;

import hr.markic.fireandwater.model.Player;
import hr.markic.serverforgame.controllers.ServerScreenController;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;

public class ReceivingDataThread extends Thread {

    private final ServerScreenController controller;
    private final int port;

    public ReceivingDataThread(ServerScreenController serverScreenController, int port) {
        this.controller = serverScreenController;
        this.port = port;
    }

    @Override
    public void run() {

        try {
            Socket clientSocket = new Socket("localhost", port);

            while (true) {

                ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

                Player player = (Player) objectInputStream.readObject();

                shareDataToClients(player);

                /*Platform.runLater(() -> {
                    controller.shareDataToClients(player);
                });*/

            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Nema vise igraca");
            controller.ExitApp();
        }
    }

    public synchronized void shareDataToClients(Player player) {

        System.out.println("Nit ušla s portom: " + port);
        System.out.println(ServerScreenController.isIsDataSharing());
        while (ServerScreenController.isIsDataSharing()) {
            try {
                System.out.println("Nit mora čekati s portom: " + port);
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ServerScreenController.setIsDataSharing(true);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        controller.getServerThread().trigger(player);
        System.out.println("Nit zapisuje podatke s portom: " + port);
        ServerScreenController.setIsDataSharing(false);
        notifyAll();
    }

}


