package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.controllers.solversettings.RangeFilesController;
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
import java.net.URL;
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

    @FXML
    private URL location;

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

    private void initializeControllers() {
        dbConnectionController.loadModel(saveHelper);
        dbConnectionController.savePlayerSelectionConfirmedCallback(this::playerSelectedDataPropagation);

        handAnalysisController.saveSolveHandsCallback(this::analyzeHands);

        solverSettingsController.loadModels(saveHelper);
        solverSettingsController.saveDisplayFolderChooserCallback(this::displayFileChooser);
    }

    public File displayFileChooser(DirectoryChooser chooser) {
        return chooser.showDialog(stage);
    }

    public void analyzeHands(List<HandData> hands) {
        //todo: when tree specifying is done, it should hook up here instead of this hard coded crap.
        workQueueController.receiveNewWork(new Work(bundleAllHandsWithTreeSettings(hands)));
        mainTabPain.getSelectionModel().select(workQueueTab);
    }

    // todo: this functionality shouldn't be here. It's not this controller's responsibility
    private List<SolveData> bundleAllHandsWithTreeSettings(List<HandData> hands) {
        ArrayList<SolveData> solveList = new ArrayList<>();
        GameTreeData treeData = defaultGameTreeData();
        for(HandData hand : hands) {
            solveList.add(new SolveData(hand, treeData));
        }

        return solveList;
    }

    private static GameTreeData defaultGameTreeData() {
        GameTreeData data = new GameTreeData("default");
        data.effectiveStack = 910;
        data.pot = 185;

        data.options.allInThresholdPercent = 100;
        data.options.allInOnlyIfLessThanNPercent = 500;
        data.options.forceOOPBet = false;
        data.options.forceOOPCheckIPBet = false;

        data.IPFlop.setActionData(false, false, "52", "2.5x");
        data.IPTurn.setActionData(false, false, "52", "3x");
        data.IPRiver.setActionData(false, false, "52", "3x");


        data.OOPFlop.setActionData(false, "", "2.5x", "52");
        data.OOPTurn.setActionData(false, "52", "3x", "");
        data.OOPRiver.setActionData(false, "52", "3x", "");

        return data;
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
