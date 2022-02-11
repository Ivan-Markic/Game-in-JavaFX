package hr.markic.fireandwater.utils;

import hr.markic.fireandwater.GameApplication;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class InfoUtil {

    public  static  Alert showAlert(Alert.AlertType alertType,
                                    String title,
                                    String headerText,
                                    String contentText,
                                    ButtonType... buttonTypes)
    {

        Alert alert = new Alert(alertType, contentText, buttonTypes);

        alert.setX(GameApplication.mainStage.getWidth() / 2 - 100);
        alert.setY(250);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        return alert;
    }

    public  static  void showExitingAppAlert()
    {
        Alert alert = InfoUtil.showAlert(
                Alert.AlertType.INFORMATION,
                "Information",
                "Information",
                "Error on loading game happened, exiting application now....",
                ButtonType.OK);

        alert.showAndWait().ifPresent(response -> {
            SceneUtil.exitApp();
        });
    }
}
