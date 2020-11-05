package com.gtohelper.datafetcher.controllers;

import com.gtohelper.domain.HandData;
import com.gtohelper.domain.Player;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GTOHelperController  {

    @FXML
    DBConnectionController dbConnectionController;

    @FXML
    HandAnalysisController handAnalysisController;

    @FXML
    WorkQueueController workQueueController;

    @FXML
    TabPane mainTabPain;
    @FXML Tab handAnalysisTab;
    @FXML Tab workQueueTab;
    @FXML Tab dbConnectionTab;

    @FXML
    private URL location;

    @FXML
    private ResourceBundle resources;

    @FXML
    private void initialize() {
        initializeControls();
        initializeCallbacks();
    }

    private void initializeCallbacks() {
        dbConnectionController.savePlayerSelectionConfirmedCallback(this::playerSelectedDataPropagation);
        handAnalysisController.saveSolveHandsCallback(this::analyzeHands);
    }

    public void analyzeHands(List<HandData> hands) {
        workQueueController.receiveNewWork(hands);
        mainTabPain.getSelectionModel().select(workQueueTab);
    }

    public void playerSelectedDataPropagation(Player player) {
        handAnalysisController.refreshTags(dbConnectionController.getPlayer());
        mainTabPain.getSelectionModel().select(handAnalysisTab);
    }

    private void initializeControls() {
        // Listener for when different tabs are selected.
        mainTabPain.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                if (oldValue == null || oldValue.idProperty() == null)
                    return;

                String oldTabName = oldValue.idProperty().getValueSafe();

                // moving away from DBConnection. Let's see if we have a good connection, and propogate it.
                if(oldTabName.equals("dbConnectionTab")) {


                }
            }
        });
    }

}
