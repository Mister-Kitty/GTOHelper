package com.gtohelper.datafetcher.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ResourceBundle;

public class GTOHelperController {

    @FXML
    DBConnectionController dbConnectionController;

    @FXML
    HandAnalysisController handAnalysisController;

    @FXML
    TabPane mainTabPain;

    @FXML
    private URL location;

    @FXML
    private ResourceBundle resources;



    @FXML
    private void initialize()
    {
        mainTabPain.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                if (oldValue == null || oldValue.idProperty() == null)
                    return;

                String oldTabName = oldValue.idProperty().getValueSafe();

                // moving away from DBConnection. Let's see if we have a good connection, and propogate it.
                if(oldTabName.equals("dbConnectionTab")) {
                    if(dbConnectionController.connectionSuccess) {
                        handAnalysisController.refreshTags();
                    }

                }
            }
        });
    }
}
