package com.piohelper.pokerDataFetcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PokerDataFetcher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {

        String url = "jdbc:postgresql://localhost:5432/PT4 DB";
        String user = "postgres";
        String password = "dbpass";

    //    connect(url, user, password);

        launch(args);

    }

    public static void connect(String url, String user, String pass) {
        try (Connection con = DriverManager.getConnection(url, user, pass);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT VERSION()")) {

            if (rs.next()) {
                System.out.println(rs.getString(1));
            }

        } catch (SQLException ex) {

            Logger lgr = Logger.getLogger(PokerDataFetcher.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
        }

    }
}
