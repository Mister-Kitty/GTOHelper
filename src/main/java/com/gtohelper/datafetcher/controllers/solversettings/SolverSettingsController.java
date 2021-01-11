package com.gtohelper.datafetcher.controllers.solversettings;

import com.gtohelper.datafetcher.models.solversettings.SolverSettingsModel;
import com.gtohelper.domain.BettingOptions;
import com.gtohelper.domain.RakeData;
import com.gtohelper.domain.Ranges;
import com.gtohelper.utility.SaveFileHelper;
import javafx.fxml.FXML;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class SolverSettingsController {

    @FXML
    BetSettingsController betSettingsController;

    @FXML
    RangeFilesController rangeFilesController;

    @FXML
    OtherSettingsController otherSettingsController;

    SolverSettingsModel solverSettingsModel;

    @FXML
    private void initialize() {

    }

    public void loadModels(SaveFileHelper saveHelper) {
        solverSettingsModel = new SolverSettingsModel(saveHelper);
        rangeFilesController.loadModel(saveHelper);
        betSettingsController.loadModel(saveHelper);
        otherSettingsController.loadModel(saveHelper);
    }

    public void saveDisplayFolderChooserCallback(Function<DirectoryChooser, File> callback) {
        rangeFilesController.saveDisplayFolderChooserCallback(callback);
    }

    public void saveDisplayFileChooserCallback(Function<FileChooser, File> callback) {
        otherSettingsController.saveDisplayFileChooserCallback(callback);
    }

    public void saveBetSettingsChangedCallback(Consumer<List<String>> callback) {
        betSettingsController.saveBetSettingsChangedCallback(callback);
    }

    public String getSolverLocation() {
        return otherSettingsController.getSolverLocation();
    }

    public BettingOptions getBetSettingByName(String name) {
        return betSettingsController.getBetSettingByName(name);
    }

    public Ranges loadRangeFiles() {
        return rangeFilesController.loadRangeFiles();
    }

    public RakeData loadRakeData() { return otherSettingsController.loadRakeData(); }
}
