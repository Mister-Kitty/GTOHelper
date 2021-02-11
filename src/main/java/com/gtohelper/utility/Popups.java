package com.gtohelper.utility;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.util.Optional;

public class Popups {

    public static void showError(String error) {
        Dialog alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(error);
        alert.showAndWait();
    }

    public static void showWarning(String warning) {
        Dialog alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(warning);
        alert.showAndWait();
    }

    public static boolean showConfirmation(String question) {
        Dialog alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setContentText(question);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK){
            return true;
        } else {
            return false;
        }
    }

}
