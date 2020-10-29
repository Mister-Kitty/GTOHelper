package com.piohelper.datafetcher.controllers;

import com.piohelper.datafetcher.models.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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

    DBConnection dbConnection = new DBConnection();

    public void initialize() {}

    @FXML
    private void testConnection() {
        String url = "jdbc:postgresql://" + DBAddress.getText() + ":" + DBPort.getText() + "/" + DBName.getText();

        String reply = dbConnection.getVersionTest(url, DBUser.getText(), DBPassword.getText());
        results.setText(reply);
    }

}
