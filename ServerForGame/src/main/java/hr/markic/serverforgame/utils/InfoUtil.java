package hr.markic.serverforgame.utils;

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

        alert.setX(400);
        alert.setY(250);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        return alert;
    }
}
