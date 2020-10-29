package com.piohelper.datafetcher.controllers;

import com.piohelper.datafetcher.models.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class DBConnectionController {

    @FXML
    private TextField DBAddress;

    @FXML
    private TextField DBPort;

    @FXML
    private TextField DBName;

    @FXML
    private TextField DBUser;

    @FXML
    private TextField DBPassword;

    @FXML
    private TextArea results;

    public boolean connectionSuccess = false;
    DBConnection dbConnection = new DBConnection();

    public void initialize() {
        testConnection();
        if(!connectionSuccess)
            results.setText("Default user/pass/name has failed. Please enter to continue.");
    }

    @FXML
    private void testConnection() {
        String url = getDBUrl();
        String reply;

        try {
            reply = dbConnection.getVersionTest(url, DBUser.getText(), DBPassword.getText());
        }  catch (SQLException ex) {
            reply = "Connect attempt failed. Error posted in the debug tab.";
            // throwables.printStackTrace();
        }

        results.setText(reply);
        connectionSuccess = true;
    }

    public String getDBUrl() {
        return "jdbc:postgresql://" + DBAddress.getText() + ":" + DBPort.getText() + "/" + DBName.getText();
    }

}
