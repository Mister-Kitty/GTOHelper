package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.DBConnectionModel;
import com.gtohelper.domain.Player;
import com.gtohelper.domain.Site;
import com.gtohelper.utility.SaveFileHelper;
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
import java.util.function.Consumer;

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
    public Site getSite() { return site.getValue(); }

    @FXML
    private ChoiceBox<Player> player;
    public Player getPlayer() { return player.getValue(); }

    @FXML
    public Button go;

    @FXML
    private final BooleanProperty connectionSuccess = new SimpleBooleanProperty(false);
    public BooleanProperty connectionSuccessProperty() { return connectionSuccess; }
    public boolean getConnectionSuccess() { return connectionSuccess.get(); }
    public void setConnectionSuccess(boolean value) {
        connectionSuccess.setValue(value);
        if(value) {
            updateSites();
            dbConnectionModel.saveAllAndPopupOnError();
        }
    }

    DBConnectionModel dbConnectionModel;
    public void loadModel(SaveFileHelper saveHelper) {
        dbConnectionModel = new DBConnectionModel(saveHelper);
        loadFieldsFromModel();
        testConnection();
        if(!getConnectionSuccess())
            results.setText("PT4's default user/pass/name has failed. Please enter the correct info to continue");
    }

    private Consumer<Player> playerSelectionConfirmedCallback;
    public void savePlayerSelectionConfirmedCallback(Consumer<Player> callback) {
        playerSelectionConfirmedCallback = callback;
    }

    @FXML
    private void playerSelectionConfirmed() {
        playerSelectionConfirmedCallback.accept(player.getValue());
    }

    public void initialize() {
        initializeControls();
    }

    @FXML
    private boolean testConnection() {
        String url = getDBUrl();
        String reply;

        try {
            dbConnectionModel.testConnection(url, DBUser.getText(), DBPassword.getText());
            reply = "Connection attempt succeeded. Fill in poker site & username info below to continue.";
        }  catch (SQLException ex) {
            reply = "Connect attempt failed. Error message has been posted in the debug tab.";
            setConnectionSuccess(false);
            return false;
        }

        results.setText(reply);

        setConnectionSuccess(true);
        updatePropsFromFields();

        dbConnectionModel.saveAllAndPopupOnError();

        return true;
    }

    private void updateSites() {
        try {
            ObservableList<Site> t = FXCollections.observableList(dbConnectionModel.getSites());
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
                            FXCollections.observableList(dbConnectionModel.getSortedPlayersBySite(newValue.id_site, 15));
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

    void loadFieldsFromModel() {
        DBAddress.setText(dbConnectionModel.loadTextField("address"));
        DBPort.setText(dbConnectionModel.loadTextField("port"));
        DBName.setText(dbConnectionModel.loadTextField("name"));
        DBUser.setText(dbConnectionModel.loadTextField("user"));
        DBPassword.setText(dbConnectionModel.loadTextField("pass"));
    }

    private void updatePropsFromFields() {
        dbConnectionModel.saveTextField("address", DBAddress.getText());
        dbConnectionModel.saveTextField("port", DBPort.getText());
        dbConnectionModel.saveTextField("name", DBName.getText());
        dbConnectionModel.saveTextField("user", DBUser.getText());
        dbConnectionModel.saveTextField("pass", DBPassword.getText());
    }
}
