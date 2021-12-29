package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.DBConnectionModel;
import com.gtohelper.domain.Player;
import com.gtohelper.domain.Site;
import com.gtohelper.utility.Logger;
import com.gtohelper.utility.SaveFileHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.function.BiConsumer;

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
        if(!getConnectionSuccess()) {
            results.setText("Connect attempt failed. Error message has been posted in the debug tab.");
        }
    }

    private BiConsumer<Site, Player> connectionSuccessfulCallback;
    public void connectionSuccessfulCallback(BiConsumer<Site, Player> callback) {
        connectionSuccessfulCallback = callback;
    }

    @FXML
    private void playerSelectionConfirmed() {
        connectionSuccessfulCallback.accept(site.getValue(), player.getValue());
    }

    public void initialize() {
        initializeControls();
    }

    @FXML
    private boolean testConnection() {
        String url = getDBUrl();

        try {
            dbConnectionModel.testConnection(url, DBUser.getText(), DBPassword.getText());

            String reply = "Connection attempt succeeded. Fill in poker site & username info below to continue.";
            results.setText(reply);
        }  catch (SQLException ex) {
            String reply = "Connect attempt failed. Error message has been posted in the debug tab.";
            results.setText(reply);

            Logger.log(Logger.Channel.HUD, "-----------------------------------------------\n");
            Logger.log(Logger.Channel.HUD, "Database error while trying to connect.\n");
            Logger.log(Logger.Channel.HUD, "-----------------------------------------------\n");
            Logger.log(Logger.Channel.HUD, ex.getMessage());
            setConnectionSuccess(false);
            return false;
        }



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

        player.setConverter(new StringConverter<>() {
            @Override
            public String toString(Player player) {
                if (player == null)
                    return "";
                return player.player_name + " (hands: " + player.total_hands + ")";
            }

            @Override
            public Player fromString(String s) {
                return null;
            }
        });

        site.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // when we change the site, we re-fetch and refresh the players dropdown
            if (newValue == null || newValue.equals(oldValue))
                return;

            try {
                ObservableList<Player> t =
                        FXCollections.observableList(dbConnectionModel.getSortedPlayersBySite(newValue.id_site, 30));
                player.getItems().clear();
                player.getItems().addAll(t);
                player.getSelectionModel().selectFirst();

                go.disableProperty().bind(player.getSelectionModel().selectedItemProperty().isNull());

            } catch (SQLException throwables) {
                results.setText("Database error while fetching players for selected site! Error posted on Debug tab.");
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
