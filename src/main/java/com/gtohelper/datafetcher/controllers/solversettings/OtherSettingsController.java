package com.gtohelper.datafetcher.controllers.solversettings;

import com.gtohelper.datafetcher.models.solversettings.OtherSettingsModel;
import com.gtohelper.domain.GlobalSolverSettings;
import com.gtohelper.domain.RakeData;
import com.gtohelper.utility.Logger;
import com.gtohelper.utility.Popups;
import com.gtohelper.utility.SaveFileHelper;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
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

    @FXML
    TextField solveResultsBackupFolder;

    DirectoryChooser folderChooser = new DirectoryChooser();
    FileChooser fileChooser = new FileChooser();

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
    }

    void loadFieldsFromModel() {
        String solver = otherSettingsModel.loadTextField("solverLocation");
        solverLocation.setText(solver);

        String viewer = otherSettingsModel.loadTextField("viewerLocation");
        viewerLocation.setText(viewer);

        String rake = otherSettingsModel.loadTextField("rakeLocation");
        rakeLocation.setText(rake);

        String resultsFolder = otherSettingsModel.loadTextField("solveResultsFolder");
        solveResultsFolder.setText(resultsFolder);

        String resultsBackupFolder = otherSettingsModel.loadTextField("solveResultsBackupFolder");
        solveResultsBackupFolder.setText(resultsBackupFolder);
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

    private void selectResultsBackupFolder(File folder) throws IOException {
        solveResultsBackupFolder.setText(folder.getCanonicalPath());
        otherSettingsModel.saveTextField("solveResultsBackupFolder", folder.getAbsolutePath());
        otherSettingsModel.saveAll();
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

    @FXML
    private void onResultsBackupChooseFolderPress() throws IOException {
        File defaultFolder = new File(solveResultsBackupFolder.getText());

        if(defaultFolder.exists())
            folderChooser.setInitialDirectory(defaultFolder);

        File result = displayFolderChooserCallback.apply(folderChooser);
        if(result != null)
            selectResultsBackupFolder(result);
    }

    public GlobalSolverSettings getGlobalSolverSettings() {
        GlobalSolverSettings settings = new GlobalSolverSettings();

        settings.setSolverLocation(Paths.get(solverLocation.getText()));
        settings.setViewerLocation(Paths.get(viewerLocation.getText()));
        settings.setRakeLocation(Paths.get(rakeLocation.getText()));
        settings.setSolverResultsFolder(Paths.get(solveResultsFolder.getText()));
        settings.setSolverResultsArchiveFolder(Paths.get(solveResultsBackupFolder.getText()));

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
