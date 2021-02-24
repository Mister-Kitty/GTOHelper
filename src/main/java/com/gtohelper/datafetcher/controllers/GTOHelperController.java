package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.controllers.solversettings.SolverSettingsController;
import com.gtohelper.domain.*;
import com.gtohelper.utility.Popups;
import com.gtohelper.utility.SaveFileHelper;
import com.gtohelper.utility.StateManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
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
    DebugOutputController debugOutputController;

    @FXML
    TabPane mainTabPain;
    @FXML Tab handAnalysisTab;
    @FXML Tab workQueueTab;
    @FXML Tab dbConnectionTab;
    @FXML Tab debugOutputTab;

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
        solverSettingsController.saveDisplayFolderChooserCallback(this::displayFolderChooser);
        solverSettingsController.saveDisplayFileChooserCallback(this::displayFileChooser);
        solverSettingsController.saveBetSettingsChangedCallback(this::betSettingsUpdatedDataPropogation);
        handAnalysisController.saveSolveHandsCallback(this::analyzeHands);
        workQueueController.saveGetGlobalSolverSettingsCallback(this::getGlobalSolverSettings);

        dbConnectionController.loadModel(saveHelper);
        handAnalysisController.loadModel(saveHelper);
        solverSettingsController.loadModels(saveHelper);

        workQueueController.loadWork(getGlobalSolverSettings());
    }

    /*
        Callbacks section
     */

    public File displayFolderChooser(DirectoryChooser chooser) {
        return chooser.showDialog(stage);
    }

    public File displayFileChooser(FileChooser chooser) {
        return chooser.showOpenDialog(stage);
    }

    public void playerSelectedDataPropagation(Player player) {
        handAnalysisController.refreshTags(dbConnectionController.getPlayer());
        mainTabPain.getSelectionModel().select(handAnalysisTab);
    }

    public void betSettingsUpdatedDataPropogation(List<String> betSettings) {
        handAnalysisController.refreshBetSettings(betSettings);
    }

    public void analyzeHands(List<HandData> hands, Work.WorkSettings settings) {
        Work work = buildWork(hands, settings);
        if(work == null)
            return;

        // One last fail-prone condition before we proceed. Create the folder and save the Work State in it.
        Path workFolder = StateManager.createWorkFolder(work, getGlobalSolverSettings());
        if(workFolder == null)
            return;

        // Note that saveNewWorkObject() will fill in work.location field.
        boolean success = StateManager.saveNewWorkObject(work, getGlobalSolverSettings());
        if(!success) {
            Popups.showError(String.format("Failed to write new work %s's data file to it's folder %s. Check write permissions", work.toString()));
            return;
        }

        workQueueController.receiveNewWork(work);
        mainTabPain.getSelectionModel().select(workQueueTab);
    }

    public GlobalSolverSettings getGlobalSolverSettings() {
        return solverSettingsController.getGlobalSolverSettings();
    }

    private Work buildWork(List<HandData> hands, Work.WorkSettings settings) {
        ArrayList<SolveTask> solveList = new ArrayList<>();

        BettingOptions treeData = solverSettingsController.getBetSettingByName(settings.getBetSettingName());
        if(treeData == null) {
            Popups.showError("Failed to load bet settings for " + settings.getBetSettingName());
            return null;
        }

        Ranges workRanges = solverSettingsController.loadRangeFiles();
        if(workRanges == null) {
            Popups.showError("Failed to load required range files.");
            return null;
        }

        RakeData rakeData = null;
        if(settings.getUseRake()) {
            rakeData = solverSettingsController.loadRakeData();
            if (rakeData == null) {
                Popups.showError("Failed to load rake data. Fix this in the Bet Settings tab or select no rake and retry.");
                return null;
            }
        }

        for(HandData hand : hands) {
            solveList.add(new SolveTask(hand));
        }

        return new Work(solveList, settings, workRanges, treeData, rakeData);
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
