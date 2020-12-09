package com.gtohelper.datafetcher.controllers.solversettings;

import com.gtohelper.datafetcher.models.solversettings.RangeFiles;
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
    TreeTableView<ActionPosition> actionPositionTable;
    @FXML TreeTableColumn<ActionPosition, String> actionPositionTableActionColumn;

    @FXML
    TextField rangeFolderLocation;

    @FXML
    Button bindButton;

    @FXML
    Button saveBindingsButton;

    // Use 2 maps to simulate a bidirectional mapping
    HashMap<ActionPosition, File> actionToRangeFileMap = new HashMap<>();
    HashMap<File, ActionPosition> rangeFileToActionMap = new HashMap<>();

    DirectoryChooser folderChooser = new DirectoryChooser();

    RangeFiles rangeFiles;
    public void loadModel(SaveFileHelper saveHelper) {
        rangeFiles = new RangeFiles(saveHelper);
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
        File result;
        try {
            File defaultFolder = new File(rangeFolderLocation.getText());
            folderChooser.setInitialDirectory(defaultFolder);
            result = displayFolderChooserCallback.apply(folderChooser);
        } catch(Exception e) {
            //if the default/loaded solver location fails, we load up our current executing directory.
            File executingFolder = new File(""); // AFAIK this is the executing folder...?
            folderChooser.setInitialDirectory(executingFolder);
            result = displayFolderChooserCallback.apply(folderChooser);
        }

        selectFolder(result);
    }

    @FXML
    private void onBindButtonPress() {
        File selectedFile = rangeFileTable.getSelectionModel().getSelectedItem().getValue();
        ActionPosition selectedAction = actionPositionTable.getSelectionModel().getSelectedItem().getValue();

        actionToRangeFileMap.put(selectedAction, selectedFile);
        rangeFileToActionMap.put(selectedFile, selectedAction);

        rangeFileTable.refresh();
    }

    private void selectFolder(File folder) {
        rangeFolderLocation.setText(folder.getAbsolutePath());
        rangeFileTable.setRoot(new FileTreeItem(folder));
        rangeFiles.saveTextField("rangeFolderLocation", folder.getAbsolutePath());
    }

    private void initializeControls() {
        actionPositionTableActionColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValue().actionString));
        rangeFileTableFilePathColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValue().getName()));

        rangeFileTableBoundActionColumn.setCellValueFactory(p -> {
            ActionPosition boundAction = rangeFileToActionMap.get(new File(p.getValue().getValue().getAbsolutePath()));

            if(boundAction == null)
                return new SimpleStringProperty("");
            else
                return new SimpleStringProperty(boundAction.fullActionString);
        });

        // We can't bind on the selected item's isLeaf property because the selected item keeps changing. So we do it the hard way.
        rangeFileTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            TreeItem<ActionPosition> actionTableItem = actionPositionTable.getSelectionModel().getSelectedItem();

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
                String relativePath = getFilePathRelativeToFolder(rangeFolderLocation.getText(), v.getAbsolutePath());
                rangeFiles.saveTextField(k.fullActionString, relativePath);
            });
            rangeFiles.saveAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFilePathRelativeToFolder(String folderPath, String filePath) {
        return filePath.substring(folderPath.length());
    }

    void loadFieldsFromModel() {
        String savedFolder = rangeFiles.loadTextField("rangeFolderLocation");

        selectFolder(new File(savedFolder));

        HashMap<String, String> valuesToLoad = rangeFiles.getAllOurSavedValues();

        // We have (actionString, fileLocation).
        // Note that rangeFolderLocation must be set before we dump our map
        valuesToLoad.forEach((k, v) -> {
            if(k.equals("rangeFolderLocation") || k.contains(" "))
                return;

            ActionPosition newK = new ActionPosition(k);
            String relativeLocation = rangeFolderLocation.getText() + v;

            try {
                File newV = new File(relativeLocation);

                actionToRangeFileMap.put(newK, newV);
                rangeFileToActionMap.put(newV, newK);
            } catch(Exception e) {
                //todo: log and pro
            }
        });
    }

    /*
        Data and objects for the Ranges tab
     */

    static class ActionPosition {
        public String actionString;
        public String fullActionString;
        public final static String delimiter = "-";

        public ActionPosition(String fullAction) {
            fullActionString = fullAction;
            actionString = getChildActionFromFullAction();
        }

        public ActionPosition(ActionPosition parentAction, String action) {
            actionString = action;
            fullActionString = parentAction.fullActionString + delimiter + action;
        }

        private String getChildActionFromFullAction() {
            int lastDelimiterIndex = fullActionString.lastIndexOf(delimiter);
            if(lastDelimiterIndex == -1)
                return fullActionString;

            return fullActionString.substring(lastDelimiterIndex + 1);
        }
    }


    private TreeItem<ActionPosition> buildActionTableItems() {
        TreeItem<ActionPosition> sixMax = new TreeItem<>(new ActionPosition("6Max"));

        TreeItem<ActionPosition> rfi = buildChildActionTreeItem(sixMax, "RFI");
        addSixMaxSeatsToTree(rfi);
        sixMax.getChildren().add(rfi);

        TreeItem<ActionPosition> vsRfi = buildChildActionTreeItem(sixMax, "vRFI");
        addSixMaxSeatsToVSTree(vsRfi);
        sixMax.getChildren().add(vsRfi);

        TreeItem<ActionPosition> vs3bet = buildChildActionTreeItem(sixMax, "v3Bet");
        addSixMaxSeatsVS3betToTree(vs3bet);
        sixMax.getChildren().add(vs3bet);

        TreeItem<ActionPosition> vs4bet = buildChildActionTreeItem(sixMax, "v4Bet");
        addSixMaxSeatsToVSTree(vs4bet);
        sixMax.getChildren().add(vs4bet);

        return sixMax;
    }

    private void addSixMaxSeatsToTree(TreeItem<ActionPosition> root) {
        TreeItem<ActionPosition> LJ = buildChildActionTreeItem(root, "LJ");
        root.getChildren().add(LJ);

        TreeItem<ActionPosition> HJ = buildChildActionTreeItem(root, "HJ");
        root.getChildren().add(HJ);

        TreeItem<ActionPosition> CO = buildChildActionTreeItem(root, "CO");
        root.getChildren().add(CO);

        TreeItem<ActionPosition> BU = buildChildActionTreeItem(root, "BTN");
        root.getChildren().add(BU);

        TreeItem<ActionPosition> SB = buildChildActionTreeItem(root, "SB");
        root.getChildren().add(SB);
    }

    private void addSixMaxSeatsToVSTree(TreeItem<ActionPosition> root) {
        TreeItem<ActionPosition> SB = buildChildActionTreeItem(root, "vSB");
        addCallRaiseToNode(addNewNodeToNode(SB, "BB"));
        root.getChildren().add(SB);

        TreeItem<ActionPosition> BU = buildChildActionTreeItem(root, "vBTN");
        addCallRaiseToNode(addNewNodeToNode(BU, "SB"));
        addCallRaiseToNode(addNewNodeToNode(BU, "BB"));
        root.getChildren().add(BU);

        TreeItem<ActionPosition> CO = buildChildActionTreeItem(root, "vCO");
        addCallRaiseToNode(addNewNodeToNode(CO, "BTN"));
        addCallRaiseToNode(addNewNodeToNode(CO, "SB"));
        addCallRaiseToNode(addNewNodeToNode(CO, "BB"));
        root.getChildren().add(CO);

        TreeItem<ActionPosition> HJ = buildChildActionTreeItem(root, "vHJ");
        addCallRaiseToNode(addNewNodeToNode(HJ, "CO"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "BTN"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "SB"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "BB"));
        root.getChildren().add(HJ);

        TreeItem<ActionPosition> LJ = buildChildActionTreeItem(root, "vLJ");
        addCallRaiseToNode(addNewNodeToNode(LJ, "HJ"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "CO"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "BTN"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "SB"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "BB"));
        root.getChildren().add(LJ);
    }

    private void addSixMaxSeatsVS3betToTree(TreeItem<ActionPosition> root) {
        TreeItem<ActionPosition> SB = buildChildActionTreeItem(root, "SB");
        addCallRaiseToNode(addNewNodeToNode(SB, "vBB"));
        root.getChildren().add(SB);

        TreeItem<ActionPosition> BU = buildChildActionTreeItem(root, "BTN");
        addCallRaiseToNode(addNewNodeToNode(BU, "vSB"));
        addCallRaiseToNode(addNewNodeToNode(BU, "vBB"));
        root.getChildren().add(BU);

        TreeItem<ActionPosition> CO = buildChildActionTreeItem(root, "CO");
        addCallRaiseToNode(addNewNodeToNode(CO, "vBTN"));
        addCallRaiseToNode(addNewNodeToNode(CO, "vSB"));
        addCallRaiseToNode(addNewNodeToNode(CO, "vBB"));
        root.getChildren().add(CO);

        TreeItem<ActionPosition> HJ = buildChildActionTreeItem(root, "HJ");
        addCallRaiseToNode(addNewNodeToNode(HJ, "vCO"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "vBTN"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "vSB"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "vBB"));
        root.getChildren().add(HJ);

        TreeItem<ActionPosition> LJ = buildChildActionTreeItem(root, "LJ");
        addCallRaiseToNode(addNewNodeToNode(LJ, "vHJ"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "vCO"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "vBTN"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "vSB"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "vBB"));
        root.getChildren().add(LJ);
    }

    private TreeItem<ActionPosition> addNewNodeToNode(TreeItem<ActionPosition> parent, String childString) {
        TreeItem<ActionPosition> child = buildChildActionTreeItem(parent, childString);
        parent.getChildren().add(child);
        return child;
    }

    private void addCallRaiseToNode(TreeItem<ActionPosition> parent) {
        addNewNodeToNode(parent, "Call");
        addNewNodeToNode(parent, "Raise");
    }

    private TreeItem<ActionPosition> buildChildActionTreeItem(TreeItem<ActionPosition> parent, String childString) {
        return new TreeItem<>(new ActionPosition(parent.getValue(), childString));
    }
}
