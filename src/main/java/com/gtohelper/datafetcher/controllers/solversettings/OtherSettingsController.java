package com.gtohelper.datafetcher.controllers.solversettings;

import com.gtohelper.datafetcher.models.solversettings.OtherSettingsModel;
import com.gtohelper.utility.SaveFileHelper;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

public class OtherSettingsController {

    @FXML
    TextField solverLocation;

    FileChooser fileChooser = new FileChooser();

    OtherSettingsModel otherSettingsModel;

    private Function<FileChooser, File> displayFileChooserCallback;
    public void saveDisplayFileChooserCallback(Function<FileChooser, File> callback) {
        displayFileChooserCallback = callback;
    }

    public void loadModel(SaveFileHelper saveHelper) {
        otherSettingsModel = new OtherSettingsModel(saveHelper);
        loadFieldsFromModel();
    }

    void loadFieldsFromModel() {
        String solver = otherSettingsModel.loadTextField("solverLocation");
        solverLocation.setText(solver);

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("EXE files (*.exe)", "*.exe");
        fileChooser.getExtensionFilters().add(extFilter);
    }

    private void selectFile(File file) throws IOException {
        solverLocation.setText(file.getAbsolutePath());
        otherSettingsModel.saveTextField("solverLocation", file.getAbsolutePath());
        otherSettingsModel.saveAll();
    }

    @FXML
    private void onChooseFilePress() throws IOException {
        File defaultFolder = new File(solverLocation.getText());

        if(defaultFolder.exists())
            fileChooser.setInitialDirectory(new File(defaultFolder.getParent()));

        File result = displayFileChooserCallback.apply(fileChooser);

        if(result != null)
            selectFile(result);
    }

    public String getSolverLocation() {
        return solverLocation.getText();
    }
}
