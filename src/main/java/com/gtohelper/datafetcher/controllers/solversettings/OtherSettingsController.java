package com.gtohelper.datafetcher.controllers.solversettings;

import com.gtohelper.datafetcher.models.solversettings.OtherSettingsModel;
import com.gtohelper.domain.RakeData;
import com.gtohelper.utility.SaveFileHelper;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Function;

public class OtherSettingsController {

    @FXML
    TextField solverLocation;

    @FXML
    TextField viewerLocation;

    @FXML
    TextField rakeLocation;

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

        String viewer = otherSettingsModel.loadTextField("viewerLocation");
        viewerLocation.setText(viewer);

        String rake = otherSettingsModel.loadTextField("rakeLocation");
        rakeLocation.setText(rake);
    }

    private void selectSolverFile(File file) throws IOException {
        solverLocation.setText(file.getAbsolutePath());
        otherSettingsModel.saveTextField("solverLocation", file.getAbsolutePath());
        otherSettingsModel.saveAll();
    }

    private void selectViewerFile(File file) throws IOException {
        viewerLocation.setText(file.getAbsolutePath());
        otherSettingsModel.saveTextField("viewerLocation", file.getAbsolutePath());
        otherSettingsModel.saveAll();
    }

    private void selectRakeFile(File file) throws IOException {
        rakeLocation.setText(file.getAbsolutePath());
        otherSettingsModel.saveTextField("rakeLocation", file.getAbsolutePath());
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

    public String getSolverLocation() {
        return solverLocation.getText();
    }
    public String getViewerLocation() {
        return viewerLocation.getText();
    }

    public RakeData loadRakeData() {
        File rakeFile = new File(rakeLocation.getText());
        if(!rakeFile.exists())
            return null;

        String fileData = null;
        try {
            fileData = loadFile(rakeFile);
        } catch (IOException e) {
            // todo: log error
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