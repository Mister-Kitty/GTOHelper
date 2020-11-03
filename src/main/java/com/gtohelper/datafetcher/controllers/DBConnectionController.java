package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.DBConnection;
import com.gtohelper.domain.Player;
import com.gtohelper.domain.Site;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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

    @FXML
    private ComboBox<Site> site;

    @FXML
    private ChoiceBox<Player> player;

    @FXML // maybe this is an abstraction violation? Could pass a callback to init... whatever.
    public Button go;

    @FXML
    private BooleanProperty connectionSuccess = new SimpleBooleanProperty(false);
    public BooleanProperty connectionSuccessProperty() { return connectionSuccess; }
    public boolean getConnectionSuccess() { return connectionSuccess.get(); }
    public void setConnectionSuccess(boolean value) {
        connectionSuccess.setValue(value);
        if(value) {
            updateSites();
        }
    }

    DBConnection dbConnection = new DBConnection();
    Properties prop;

    public void initialize() {
        initializeControls();
        initializeProp();
        testConnection();
        if(!getConnectionSuccess())
            results.setText("PT4's default user/pass/name has failed. Please enter the correct info to continue");
    }

    @FXML
    private boolean testConnection() {
        String url = getDBUrl();
        String reply;

        try {
            dbConnection.testConnection(url, DBUser.getText(), DBPassword.getText());
            dbConnection.saveProperties(prop);
            reply = "Connection attempt succeeded. Fill in poker site & username info below to continue.";
        }  catch (SQLException ex) {
            reply = "Connect attempt failed. Error message has been posted in the debug tab.";
            setConnectionSuccess(false);
            return false;
        } catch (IOException e) {
            reply = "Database access succeeded, but saving the information to the config file has failed. " +
                    "Fill in poker site & username info below to continue. To have this auto-complete in the future" +
                    "ensure this program has file write permissions, or try running as Admin.";
        }

        results.setText(reply);
        setConnectionSuccess(true);
        updatePropsFromFields();
        return true;
    }

    private void updateSites() {
        try {
            ObservableList<Site> t = FXCollections.observableList(dbConnection.getSites());
            site.getItems().clear();
            site.getItems().addAll(t);
        } catch (SQLException ex) {
            results.setText("Database error while fetching poker sites! Error posted on Debug tab.");
        }
    }


    public String getDBUrl() {
        return "jdbc:postgresql://" + DBAddress.getText() + ":" + DBPort.getText() + "/" + DBName.getText();
    }

    private void initializeControls() {
        site.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Site>() {
            @Override
            public void changed(ObservableValue<? extends Site> observable, Site oldValue, Site newValue) {
                // when we change the site, we re-fetch and refresh the players dropdown
                if (newValue == null || newValue.equals(oldValue))
                    return;

                try {
                    ObservableList<Player> t =
                            FXCollections.observableList(dbConnection.getSortedPlayersBySite(newValue.id_site, 15));
                    player.getItems().clear();
                    player.getItems().addAll(t);
                    player.getSelectionModel().selectFirst();

                    go.disableProperty().bind(player.getSelectionModel().selectedItemProperty().isNull());

                } catch (SQLException throwables) {
                    results.setText("Database error while fetching players for selected site! Error posted on Debug tab.");
                }
            }
        });
    }

    private void initializeProp() {
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
                results.setText("Failed to regenerate and save config file. Ensure you have write permissions. Or try to run as Admin.");
            }
        }

        loadFieldsFromProp();
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
