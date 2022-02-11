module hr.markic.serverforgame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;


    opens hr.markic.serverforgame to javafx.fxml;
    opens hr.markic.fireandwater.model to javafx.fxml;
    opens hr.markic.serverforgame.controllers to javafx.fxml;
    exports hr.markic.serverforgame.controllers;
    exports hr.markic.serverforgame to javafx.graphics;
}