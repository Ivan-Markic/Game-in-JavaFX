package hr.markic.fireandwater.controllers;

import hr.markic.fireandwater.GameApplication;
import hr.markic.fireandwater.utils.SceneUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import java.net.URL;
import java.util.ResourceBundle;

public class EndScreenController implements Initializable {

    @FXML
    private Label lblText;

    public void exitApplication()
    {
        SceneUtil.exitApp();
    }

    public void restartGame(){
        SceneUtil.loadGame(true, false, false);
        //SceneUtil.loadGame(true);
    }

    public void openHome(){
        SceneUtil.loadNewScene("homeScreen.fxml");
        GameApplication.mainStage.getScene().getStylesheets().add(this.getClass().getResource("/toggleButtons.css").toExternalForm());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lblText.setText(GameScreenController.getInstance().EndText);
        lblText.setTextAlignment(TextAlignment.CENTER);
        lblText.setTextFill(Color.rgb(0, 255, 0));
    }
}
