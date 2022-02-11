package hr.markic.fireandwater;

import hr.markic.fireandwater.utils.SceneUtil;
import javafx.application.Application;
import javafx.stage.Stage;

public class GameApplication extends Application {

    public static Stage mainStage;

    @Override
    public void start(Stage stage){
        stage.centerOnScreen();
        stage.setResizable(false);
        stage.setFullScreen(false);
        mainStage = stage;
        stage.setTitle("Fire and Water");
        SceneUtil.loadNewScene("homeScreen.fxml");
        mainStage.getScene().getStylesheets().add(this.getClass().getResource("/toggleButtons.css").toExternalForm());
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
