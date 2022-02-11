package hr.markic.fireandwater.utils;

import hr.markic.fireandwater.GameApplication;
import hr.markic.fireandwater.controllers.GameScreenController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class SceneUtil {

    public static void loadNewScene(String nameOfScene) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(GameApplication.class.getResource(nameOfScene));
            Scene scene = new Scene(fxmlLoader.load(), 1280, 768);
            GameApplication.mainStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void loadGame(boolean newGame, boolean multiplayer, boolean replay) {
        loadNewScene("gameScreen.fxml");
        GameScreenController.getInstance().setMultiplayer(multiplayer);
        if (replay == false) {
            GameApplication.mainStage.getScene().setOnKeyPressed(keyEvent -> GameScreenController.getInstance().sceneListeners(keyEvent));
        }

        if (!newGame)
            GameScreenController.getInstance().loadGame();

        if (replay){
            GameScreenController.getInstance().readReplayFromXml();
        }
    }

    public static void exitApp() {
        GameApplication.mainStage.close();
    }
}
