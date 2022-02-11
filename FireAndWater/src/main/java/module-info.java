module hr.markic.fireandwater {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.desktop;


    opens hr.markic.fireandwater to javafx.fxml;
    exports hr.markic.fireandwater;
    exports hr.markic.fireandwater.controllers;
    opens hr.markic.fireandwater.controllers to javafx.fxml;
}