package com.gtohelper.datafetcher.controllers.solversettings;

import com.gtohelper.datafetcher.models.solversettings.RangeFilesModel;
import com.gtohelper.domain.Ranges;
import com.gtohelper.utility.FileTreeItem;
import com.gtohelper.utility.SaveFileHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Function;

public class RangeFilesController {

    @FXML
    TreeTableView<File> rangeFileTable;
    @FXML TreeTableColumn<File, String> rangeFileTableFilePathColumn;
    @FXML TreeTableColumn<File, String> rangeFileTableBoundActionColumn;

    @FXML
    TreeTableView<String> actionPositionTable;
    @FXML TreeTableColumn<String, String> actionPositionTableActionColumn;

    @FXML
    TextField rangeFolderLocation;

    @FXML
    Button bindButton;

    @FXML
    Button saveBindingsButton;

    // Use 2 maps to simulate a bidirectional mapping
    HashMap<String, File> actionToRangeFileMap = new HashMap<>();
    HashMap<File, String> rangeFileToActionMap = new HashMap<>();

    DirectoryChooser folderChooser = new DirectoryChooser();

    RangeFilesModel rangeFilesModel;
    public void loadModel(SaveFileHelper saveHelper) {
        rangeFilesModel = new RangeFilesModel(saveHelper);
        loadFieldsFromModel();
    }

    private Function<DirectoryChooser, File> displayFolderChooserCallback;
    public void saveDisplayFolderChooserCallback(Function<DirectoryChooser, File> callback) {
        displayFolderChooserCallback = callback;
    }

    @FXML
    private void initialize() {
        initializeControls();
        actionPositionTable.setRoot(buildActionTableItems());
        folderChooser.setTitle("Select Range Folder");
    }

    @FXML
    private void onChooseFolderPress() {
        File defaultFolder = new File(rangeFolderLocation.getText());

        if(defaultFolder.exists())
            folderChooser.setInitialDirectory(defaultFolder);

        File result = displayFolderChooserCallback.apply(folderChooser);
        if(result != null)
            selectFolder(result);
    }

    @FXML
    private void onBindButtonPress() {
        File selectedFile = rangeFileTable.getSelectionModel().getSelectedItem().getValue();
        String selectedAction = actionPositionTable.getSelectionModel().getSelectedItem().getValue();

        actionToRangeFileMap.put(selectedAction, selectedFile);
        rangeFileToActionMap.put(selectedFile, selectedAction);

        rangeFileTable.refresh();
    }

    private void selectFolder(File folder) {
        rangeFolderLocation.setText(folder.getAbsolutePath());
        rangeFileTable.setRoot(new FileTreeItem(folder));
        rangeFilesModel.saveTextField("rangeFolderLocation", folder.getPath());
    }

    private void initializeControls() {
        actionPositionTableActionColumn.setCellValueFactory(p -> new SimpleStringProperty(getChildActionFromFullAction(p.getValue().getValue())));
        rangeFileTableFilePathColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValue().getName()));

        rangeFileTableBoundActionColumn.setCellValueFactory(p -> {
            String boundAction = rangeFileToActionMap.get(new File(p.getValue().getValue().getAbsolutePath()));

            if(boundAction == null)
                return new SimpleStringProperty("");
            else
                return new SimpleStringProperty(boundAction);
        });

        // We can't bind on the selected item's isLeaf property because the selected item keeps changing. So we do it the hard way.
        rangeFileTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            TreeItem<String> actionTableItem = actionPositionTable.getSelectionModel().getSelectedItem();

            if(newValue == null) // Unsure why this triggers. Will look into it.
                return;

            bindButton.disableProperty().setValue(!newValue.isLeaf() || actionTableItem == null || !actionTableItem.isLeaf());
        });
        actionPositionTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            TreeItem<File> fileTableItem = rangeFileTable.getSelectionModel().getSelectedItem();
            bindButton.disableProperty().setValue(!newValue.isLeaf() || fileTableItem == null || !fileTableItem.isLeaf());
        });
    }

    @FXML
    private void onSaveBindingsButtonPress() {
        try {
            actionToRangeFileMap.forEach((k, v) -> {
                String relativePath = getFilePathRelativeToFolder(rangeFolderLocation.getText(), v.getPath());
                rangeFilesModel.saveTextField(k, relativePath);
            });
            rangeFilesModel.saveAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFilePathRelativeToFolder(String folderPath, String filePath) {
        return filePath.substring(folderPath.length());
    }

    public Ranges loadRangeFiles() {
        return rangeFilesModel.loadRangeFiles(actionToRangeFileMap);
    }

    void loadFieldsFromModel() {
        String savedFolder = rangeFilesModel.loadTextField("rangeFolderLocation");

        selectFolder(new File(savedFolder));

        HashMap<String, String> valuesToLoad = rangeFilesModel.getAllOurSavedValues();

        // We have (actionString, fileLocation).
        // Note that rangeFolderLocation must be set before we dump our map
        valuesToLoad.forEach((k, v) -> {
            if(k.equals("rangeFolderLocation") || k.contains(" "))
                return;

            String relativeLocation = rangeFolderLocation.getText() + v;

            try {
                File newV = new File(relativeLocation);

                actionToRangeFileMap.put(k, newV);
                rangeFileToActionMap.put(newV, k);
            } catch(Exception e) {
                //todo: log and pro
            }
        });
    }

    /*
        Data and objects for the Ranges tab
     */

    public final static String delimiter = "-";

    private String getChildActionFromFullAction(String fullAction) {
        int lastDelimiterIndex = fullAction.lastIndexOf(delimiter);
        if(lastDelimiterIndex == -1)
            return fullAction;

        return fullAction.substring(lastDelimiterIndex + 1);
    }

    private TreeItem<String> buildActionTableItems() {
        TreeItem<String> sixMax = new TreeItem<>("6Max");

        TreeItem<String> rfi = buildChildActionTreeItem(sixMax, "RFI");
        addSixMaxSeatsToTree(rfi);
        sixMax.getChildren().add(rfi);

        TreeItem<String> vsRfi = buildChildActionTreeItem(sixMax, "vRFI");
        addSixMaxSeatsToVSTree(vsRfi);
        sixMax.getChildren().add(vsRfi);

        TreeItem<String> vs3bet = buildChildActionTreeItem(sixMax, "v3Bet");
        addSixMaxSeatsVS3betToTree(vs3bet);
        sixMax.getChildren().add(vs3bet);

        TreeItem<String> vs4bet = buildChildActionTreeItem(sixMax, "v4Bet");
        addSixMaxSeatsToVSTree(vs4bet);
        sixMax.getChildren().add(vs4bet);

        return sixMax;
    }

    private void addSixMaxSeatsToTree(TreeItem<String> root) {
        TreeItem<String> LJ = buildChildActionTreeItem(root, "LJ");
        root.getChildren().add(LJ);

        TreeItem<String> HJ = buildChildActionTreeItem(root, "HJ");
        root.getChildren().add(HJ);

        TreeItem<String> CO = buildChildActionTreeItem(root, "CO");
        root.getChildren().add(CO);

        TreeItem<String> BU = buildChildActionTreeItem(root, "BTN");
        root.getChildren().add(BU);

        TreeItem<String> SB = buildChildActionTreeItem(root, "SB");
        root.getChildren().add(SB);
    }

    private void addSixMaxSeatsToVSTree(TreeItem<String> root) {
        TreeItem<String> SB = buildChildActionTreeItem(root, "vSB");
        addCallRaiseToNode(addNewNodeToNode(SB, "BB"));
        root.getChildren().add(SB);

        TreeItem<String> BU = buildChildActionTreeItem(root, "vBTN");
        addCallRaiseToNode(addNewNodeToNode(BU, "SB"));
        addCallRaiseToNode(addNewNodeToNode(BU, "BB"));
        root.getChildren().add(BU);

        TreeItem<String> CO = buildChildActionTreeItem(root, "vCO");
        addCallRaiseToNode(addNewNodeToNode(CO, "BTN"));
        addCallRaiseToNode(addNewNodeToNode(CO, "SB"));
        addCallRaiseToNode(addNewNodeToNode(CO, "BB"));
        root.getChildren().add(CO);

        TreeItem<String> HJ = buildChildActionTreeItem(root, "vHJ");
        addCallRaiseToNode(addNewNodeToNode(HJ, "CO"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "BTN"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "SB"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "BB"));
        root.getChildren().add(HJ);

        TreeItem<String> LJ = buildChildActionTreeItem(root, "vLJ");
        addCallRaiseToNode(addNewNodeToNode(LJ, "HJ"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "CO"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "BTN"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "SB"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "BB"));
        root.getChildren().add(LJ);
    }

    private void addSixMaxSeatsVS3betToTree(TreeItem<String> root) {
        TreeItem<String> SB = buildChildActionTreeItem(root, "SB");
        addCallRaiseToNode(addNewNodeToNode(SB, "vBB"));
        root.getChildren().add(SB);

        TreeItem<String> BU = buildChildActionTreeItem(root, "BTN");
        addCallRaiseToNode(addNewNodeToNode(BU, "vSB"));
        addCallRaiseToNode(addNewNodeToNode(BU, "vBB"));
        root.getChildren().add(BU);

        TreeItem<String> CO = buildChildActionTreeItem(root, "CO");
        addCallRaiseToNode(addNewNodeToNode(CO, "vBTN"));
        addCallRaiseToNode(addNewNodeToNode(CO, "vSB"));
        addCallRaiseToNode(addNewNodeToNode(CO, "vBB"));
        root.getChildren().add(CO);

        TreeItem<String> HJ = buildChildActionTreeItem(root, "HJ");
        addCallRaiseToNode(addNewNodeToNode(HJ, "vCO"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "vBTN"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "vSB"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "vBB"));
        root.getChildren().add(HJ);

        TreeItem<String> LJ = buildChildActionTreeItem(root, "LJ");
        addCallRaiseToNode(addNewNodeToNode(LJ, "vHJ"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "vCO"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "vBTN"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "vSB"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "vBB"));
        root.getChildren().add(LJ);
    }

    private TreeItem<String> addNewNodeToNode(TreeItem<String> parent, String childString) {
        TreeItem<String> child = buildChildActionTreeItem(parent, childString);
        parent.getChildren().add(child);
        return child;
    }

    private void addCallRaiseToNode(TreeItem<String> parent) {
        addNewNodeToNode(parent, "Call");
        addNewNodeToNode(parent, "Raise");
    }

    private TreeItem<String> buildChildActionTreeItem(TreeItem<String> parent, String childString) {
        return new TreeItem<>(parent.getValue() + delimiter + childString);
    }
}
