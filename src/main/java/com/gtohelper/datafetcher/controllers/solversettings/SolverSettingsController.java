package com.gtohelper.datafetcher.controllers.solversettings;

import com.gtohelper.datafetcher.models.solversettings.SolverSettings;
import com.gtohelper.domain.GameTreeData;
import com.gtohelper.utility.SaveFileHelper;
import javafx.fxml.FXML;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class SolverSettingsController {

    @FXML
    BetSettingsController betSettingsController;

    @FXML
    RangeFilesController rangeFilesController;

    SolverSettings solverSettings;

    @FXML
    private void initialize() {

    }

    public void loadModels(SaveFileHelper saveHelper) {
        solverSettings = new SolverSettings(saveHelper);
        rangeFilesController.loadModel(saveHelper);
        betSettingsController.loadModel(saveHelper);
    }

    public void saveDisplayFolderChooserCallback(Function<DirectoryChooser, File> callback) {
        rangeFilesController.saveDisplayFolderChooserCallback(callback);
    }

    public void saveBetSettingsChangedCallback(Consumer<List<String>> callback) {
        betSettingsController.saveBetSettingsChangedCallback(callback);
    }

    public GameTreeData getBetSettingByName(String name) {
        return betSettingsController.getBetSettingByName(name);
    }

}
