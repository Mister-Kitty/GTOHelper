package com.gtohelper.datafetcher.controllers.solversettings;

import com.gtohelper.datafetcher.models.solversettings.SolverSettings;
import com.gtohelper.utility.SaveFileHelper;
import javafx.fxml.FXML;
import javafx.stage.DirectoryChooser;

import java.io.File;
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

}
