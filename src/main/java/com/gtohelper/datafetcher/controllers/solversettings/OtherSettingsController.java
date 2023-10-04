package com.gtohelper.datafetcher.controllers.solversettings;

import com.gtohelper.datafetcher.models.solversettings.OtherSettingsModel;
import com.gtohelper.domain.GlobalSolverSettings;
import com.gtohelper.domain.RakeData;
import com.gtohelper.utility.Logger;
import com.gtohelper.utility.Popups;
import com.gtohelper.utility.SaveFileHelper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Function;

public class OtherSettingsController {

    @FXML
    TextField solverLocation;

    @FXML
    TextField viewerLocation;

    @FXML
    TextField rakeLocation;

    @FXML
    TextField solveResultsFolder;

    // These should really enumerate versions... so, hold an INT containing version number.
    @FXML
    RadioButton solverV1Radio, solverV2Radio;

    DirectoryChooser folderChooser = new DirectoryChooser();
    FileChooser fileChooser = new FileChooser();
    ToggleGroup solverVersionToggleGroup = new ToggleGroup();

    OtherSettingsModel otherSettingsModel;

    private Function<FileChooser, File> displayFileChooserCallback;
    public void saveDisplayFileChooserCallback(Function<FileChooser, File> callback) {
        displayFileChooserCallback = callback;
    }

    private Function<DirectoryChooser, File> displayFolderChooserCallback;
    public void saveDisplayFolderChooserCallback(Function<DirectoryChooser, File> callback) {
        displayFolderChooserCallback = callback;
    }

    public void loadModel(SaveFileHelper saveHelper) {
        folderChooser.setTitle("Select Save Folder");
        otherSettingsModel = new OtherSettingsModel(saveHelper);
        loadFieldsFromModel();

        solverV1Radio.setToggleGroup(solverVersionToggleGroup);
        solverV2Radio.setToggleGroup(solverVersionToggleGroup);

        solverVersionToggleGroup.selectedToggleProperty().addListener((observableValue, oldValue, newValue) -> {
            if(oldValue != newValue)
                selectSolverVersion();
        });
    }

    void loadFieldsFromModel() {
        String solver = otherSettingsModel.loadTextField("solverLocation");
        solverLocation.setText(solver);

        String viewer = otherSettingsModel.loadTextField("viewerLocation");
        viewerLocation.setText(viewer);

        String rake = otherSettingsModel.loadTextField("rakeLocation");
        Path rakePathAbsPath = Paths.get(rake).toAbsolutePath();
        if(!Files.exists(rakePathAbsPath)) {
            // If it doesn't exist, like on first launch, we gotta regenerate it from Resources
            ClassLoader classLoader = OtherSettingsController.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("DefaultRakeFile.csv");
            Path defaultRakeFilePath = Paths.get("DefaultRakeFile.csv");

            try {
                Files.copy(inputStream, defaultRakeFilePath, StandardCopyOption.REPLACE_EXISTING);
                rakeLocation.setText(defaultRakeFilePath.toString());
            } catch (IOException e) {
                Popups.showError(String.format("Failed to create default rake file at %s.\n" +
                        "Check write permission and/or run GTOHelper as Admin", defaultRakeFilePath.toString()));
                throw new RuntimeException(e);
            }
        } else {
            rakeLocation.setText(rakePathAbsPath.toString());
        }

        String resultsFolder = otherSettingsModel.loadTextField("solveResultsFolder");
        Path resultsFolderAbsPath = Paths.get(resultsFolder).toAbsolutePath();
        if(!Files.exists(resultsFolderAbsPath)) {
            try {
                Path resultPath = Files.createDirectory(resultsFolderAbsPath);
                solveResultsFolder.setText(resultPath.toAbsolutePath().toString());
            } catch (IOException e) {
                Popups.showError(String.format("Failed to create solver results folder at %s.\n" +
                                "Check write permission and/or run GTOHelper as Admin", resultsFolderAbsPath.toString()));
                throw new RuntimeException(e);
            }
        } else {
            solveResultsFolder.setText(resultsFolderAbsPath.toString());
        }

        String isV2 = otherSettingsModel.loadTextField("isV2");
        boolean isV2Boolean = Boolean.parseBoolean(isV2);
        if(isV2Boolean)
            solverV2Radio.setSelected(true);
        else
            solverV1Radio.setSelected(true);
    }

    private void selectSolverFile(File file) throws IOException {
        solverLocation.setText(file.getCanonicalPath());
        otherSettingsModel.saveTextField("solverLocation", file.getAbsolutePath());
        otherSettingsModel.saveAll();
    }

    private void selectViewerFile(File file) throws IOException {
        viewerLocation.setText(file.getCanonicalPath());
        otherSettingsModel.saveTextField("viewerLocation", file.getAbsolutePath());
        otherSettingsModel.saveAll();
    }

    private void selectRakeFile(File file) throws IOException {
        rakeLocation.setText(file.getCanonicalPath());
        otherSettingsModel.saveTextField("rakeLocation", file.getAbsolutePath());
        otherSettingsModel.saveAll();
    }

    private void selectResultsFolder(File folder) throws IOException {
        solveResultsFolder.setText(folder.getCanonicalPath());
        otherSettingsModel.saveTextField("solveResultsFolder", folder.getAbsolutePath());
        otherSettingsModel.saveAll();
    }

    // passing in the newly selected version isn't really needed, although version INT could be passed in if the aforemeantioned refactor takes place
    private void selectSolverVersion() {
        boolean isV2 = solverV2Radio.isSelected();
        if(isV2)
            otherSettingsModel.saveTextField("isV2", "true");
        else
            otherSettingsModel.saveTextField("isV2", "false");

        try {
            otherSettingsModel.saveAll();
        } catch (IOException e) {
            Logger.log(e);
        }
    }

    @FXML
    private void onSolverChooseFilePress() throws IOException {
        File defaultFolder = new File(solverLocation.getText());

        if(defaultFolder.exists())
            fileChooser.setInitialDirectory(new File(defaultFolder.getParent()));

        File result = displayFileChooserCallback.apply(fileChooser);

        if(result != null)
            selectSolverFile(result);
    }

    @FXML
    private void onViewerChooseFilePress() throws IOException {
        File defaultFolder = new File(viewerLocation.getText());

        if(defaultFolder.exists())
            fileChooser.setInitialDirectory(new File(defaultFolder.getParent()));

        File result = displayFileChooserCallback.apply(fileChooser);

        if(result != null)
            selectViewerFile(result);
    }

    @FXML
    private void onRakeChooseFilePress() throws IOException {
        File defaultFolder = new File(rakeLocation.getText());

        if(defaultFolder.exists())
            fileChooser.setInitialDirectory(new File(defaultFolder.getParent()));

        File result = displayFileChooserCallback.apply(fileChooser);

        if(result != null)
            selectRakeFile(result);
    }

    @FXML
    private void onResultsChooseFolderPress() throws IOException {
        File defaultFolder = new File(solveResultsFolder.getText());

        if(defaultFolder.exists())
            folderChooser.setInitialDirectory(defaultFolder);

        File result = displayFolderChooserCallback.apply(folderChooser);
        if(result != null)
            selectResultsFolder(result);
    }

    public GlobalSolverSettings getGlobalSolverSettings() {
        GlobalSolverSettings settings = new GlobalSolverSettings();

        settings.setSolverLocation(Paths.get(solverLocation.getText()));
        settings.setViewerLocation(Paths.get(viewerLocation.getText()));
        settings.setRakeLocation(Paths.get(rakeLocation.getText()));
        settings.setSolverResultsFolder(Paths.get(solveResultsFolder.getText()));
        settings.setIsV2(solverV2Radio.isSelected());

        return settings;
    }

    public RakeData loadRakeData() {
        File rakeFile = new File(rakeLocation.getText());
        if(!rakeFile.exists()) {
            String error = String.format("Specified rake file %s does not exist.", rakeFile.getAbsolutePath());
            Logger.log(error);
            Popups.showError(error);
            return null;
        }

        String fileData = null;
        try {
            fileData = loadFile(rakeFile);
        } catch (IOException e) {
            String error = String.format("Input/output error trying to read rake file %s.", rakeFile.getAbsolutePath());
            Logger.log(error);
            Logger.log(e);
            Popups.showError(error);
            return null;
        }

        RakeData rakeData = new RakeData();
        for(String line : fileData.split("\n")) {
            if(line.startsWith("#"))
                continue;

            String[] commaSplit = line.split(",");
            float bbLimit = Float.parseFloat(commaSplit[0]);
            float rakePercent = Float.parseFloat(commaSplit[1]);
            float twoPlayerCap = Float.parseFloat(commaSplit[2]);
            float fourPlayerCap = Float.parseFloat(commaSplit[3]);
            float fullPlayerCap = Float.parseFloat(commaSplit[4]);

            rakeData.addRakeRow(bbLimit, rakePercent, twoPlayerCap, fourPlayerCap, fullPlayerCap);
        }

        return rakeData;
    }

    private String loadFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();

        return new String(data, "UTF-8");
    }
}
