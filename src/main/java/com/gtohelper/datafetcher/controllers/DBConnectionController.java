package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

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
    Properties prop;
    public boolean connectionSuccess = false;

    public void initialize() {
        try {
            prop = dbConnection.loadProperties();
        } catch (IOException e) {
            results.setText("Error when trying to load existing config file. Regeneration and save was successful.");
        }

        if(prop == null || prop.isEmpty()) {
            prop = dbConnection.createDefaultProp();
            try {
                dbConnection.saveProperties(prop);
            } catch (IOException e) {
                results.setText("Failed to regenerate and save config file. Ensure you have write permissions. Or just run as Admin.");
            }
        }

        loadFieldsFromProp();

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
            return;
            // throwables.printStackTrace();
        }

        results.setText(reply);
        connectionSuccess = true;
        updatePropsFromFields();

        try {
            dbConnection.saveProperties(prop);
        } catch (IOException e) {
            results.setText("Database access succeeded, but connection info failed to save ");
        }
    }

    public String getDBUrl() {
        return "jdbc:postgresql://" + DBAddress.getText() + ":" + DBPort.getText() + "/" + DBName.getText();
    }

    private void loadFieldsFromProp() {
        DBAddress.setText(prop.getProperty("db.address"));
        DBPort.setText(prop.getProperty("db.port"));
        DBName.setText(prop.getProperty("db.name"));
        DBUser.setText(prop.getProperty("db.user"));
        DBPassword.setText(prop.getProperty("db.pass"));
    }

    private void updatePropsFromFields() {
        prop.setProperty("db.address", DBAddress.getText());
        prop.setProperty("db.port", DBPort.getText());
        prop.setProperty("db.name", DBName.getText());
        prop.setProperty("db.user", DBUser.getText());
        prop.setProperty("db.pass", DBPassword.getText());
    }
}
