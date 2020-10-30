package com.gtohelper.datafetcher.models;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GTOHelper extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
     //   FXMLLoader rootLoader = new FXMLLoader(getClass().getResource("/com/piohelper/fxml/GTOHelper.fxml"));
     //   rootLoader.load();
        Parent root = FXMLLoader.load(getClass().getResource("/com/gtohelper/fxml/GTOHelper.fxml"));

     //   DataFetcherController rootDataFetcherController = rootLoader.getController();
    //    rootDataFetcherController.setDataFetcher(this);


        primaryStage.setTitle("GTOHelper v 0.0");
        primaryStage.setScene(new Scene(root, 500, 375));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


}
