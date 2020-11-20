package com.gtohelper.datafetcher.models;

import com.gtohelper.datafetcher.controllers.GTOHelperController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GTOHelper extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader rootLoader = new FXMLLoader(getClass().getResource("/com/gtohelper/fxml/GTOHelper.fxml"));
        Parent root = rootLoader.load();

        Scene scene = new Scene(root, 720, 480);
        GTOHelperController controller = (GTOHelperController)rootLoader.getController();
        controller.setStage(primaryStage);

        primaryStage.setTitle("GTOHelper v 0.0");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(720);
        primaryStage.setMinWidth(480);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


}
