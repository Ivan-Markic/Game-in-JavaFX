package hr.markic.fireandwater.multicast;

import hr.markic.fireandwater.model.Player;

import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;

public class ServerThread extends Thread {

    private final LinkedBlockingDeque<Player> players = new LinkedBlockingDeque<>();

    public static int DEFAULT_PORT = 1234;

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    private ServerSocket serverSocket;

    @Override
    public void run() {

        //See how to fix this shit
        try {

            try {
                serverSocket = new ServerSocket(DEFAULT_PORT);
            } catch (Exception e) {
                serverSocket = new ServerSocket(1235);
                //e.printStackTrace();
            }


            Socket clientSocket = serverSocket.accept();

            while (true) {

                if (!players.isEmpty()) {

                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                    out.writeObject(players.getFirst());
                    players.remove(players.getFirst());
                    out.flush();

                }
            }
        } catch (Exception e) {
            System.err.println("Greska pri povezivanju molimo vas pokusajte ponovno");
            e.printStackTrace();
        }
    }

    public void trigger(Player player) {
        players.add(player);
    }
}
