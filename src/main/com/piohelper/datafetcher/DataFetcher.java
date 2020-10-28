package com.piohelper.datafetcher;

import com.piohelper.PT4DataManager.PT4HandSummaryDM;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataFetcher extends Application {

    String url = "jdbc:postgresql://localhost:5432/PT4 DB";
    String user = "postgres";
    String password = "dbpass";

    PT4HandSummaryDM pt4HandSummaryDM;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader rootLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
        rootLoader.load();

        Controller rootController = rootLoader.getController();
        rootController.setDataFetcher(this);

        primaryStage.setTitle("PioHelper v 0.0");
        primaryStage.setScene(new Scene(rootLoader.getRoot(), 500, 375));
        primaryStage.show();
    }

    public void initializeDM() {

        Connection con;
        try {
            con = DriverManager.getConnection(url, user, password);
            pt4HandSummaryDM = new PT4HandSummaryDM(con);

            pt4HandSummaryDM.getRowCount();
            System.out.println(pt4HandSummaryDM.getRowCount());


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }


    public static void main(String[] args) {



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

            Logger lgr = Logger.getLogger(DataFetcher.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
        }

    }
}
