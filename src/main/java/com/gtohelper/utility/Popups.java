package com.gtohelper.utility;

import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;

public class Popups {

    public static void showError(String error) {
        Dialog alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(error);
        alert.showAndWait();
    }

    public static void showWarning(String error) {
        Dialog alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(error);
        alert.showAndWait();
    }



}
