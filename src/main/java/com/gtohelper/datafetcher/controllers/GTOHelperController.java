package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.controllers.solversettings.SolverSettingsController;
import com.gtohelper.domain.*;
import com.gtohelper.utility.SaveFileHelper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GTOHelperController  {

    @FXML
    DBConnectionController dbConnectionController;

    @FXML
    SolverSettingsController solverSettingsController;

    @FXML
    HandAnalysisController handAnalysisController;

    @FXML
    WorkQueueController workQueueController;

    @FXML
    TabPane mainTabPain;
    @FXML Tab handAnalysisTab;
    @FXML Tab workQueueTab;
    @FXML Tab dbConnectionTab;

    private Stage stage;
    private SaveFileHelper saveHelper = new SaveFileHelper();

    @FXML
    private void initialize() {
        // todo: loadproperties shoudl be in a constructor or something.
        saveHelper.loadProperties();
        initializeControls();
        initializeControllers();
    }

    public void setStage(Stage s) {
        stage = s;
    }

    /*
        This controller is a hub for passing the minimal ammount of data between tabs.
        here we save and direct callbacks for data requests.
     */

    private void initializeControllers() {
        // Save the callbacks before loading models. Some models use these.
        dbConnectionController.savePlayerSelectionConfirmedCallback(this::playerSelectedDataPropagation);
        solverSettingsController.saveDisplayFolderChooserCallback(this::displayFileChooser);
        handAnalysisController.saveSolveHandsCallback(this::analyzeHands);
        solverSettingsController.saveBetSettingsChangedCallback(this::betSettingsUpdatedDataPropogation);

        dbConnectionController.loadModel(saveHelper);
        handAnalysisController.loadModel(saveHelper);
        solverSettingsController.loadModels(saveHelper);
    }

    /*
        Callbacks section
     */

    public File displayFileChooser(DirectoryChooser chooser) {
        return chooser.showDialog(stage);
    }

    public void playerSelectedDataPropagation(Player player) {
        handAnalysisController.refreshTags(dbConnectionController.getPlayer());
        mainTabPain.getSelectionModel().select(handAnalysisTab);
    }

    public void betSettingsUpdatedDataPropogation(List<String> betSettings) {
        handAnalysisController.refreshBetSettings(betSettings);
    }

    public void analyzeHands(List<HandData> hands, String betSettingName) {
        workQueueController.receiveNewWork(buildWork(hands, betSettingName));
        mainTabPain.getSelectionModel().select(workQueueTab);
    }

    private Work buildWork(List<HandData> hands, String betSettingName) {
        ArrayList<SolveData> solveList = new ArrayList<>();

        BetSettings treeData = solverSettingsController.getBetSettingByName(betSettingName);
        for(HandData hand : hands) {
            solveList.add(new SolveData(hand, treeData));
        }

        return new Work(solveList);
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
