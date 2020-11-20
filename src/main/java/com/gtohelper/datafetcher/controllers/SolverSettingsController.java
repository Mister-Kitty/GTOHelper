package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.SolverSettings;
import com.gtohelper.domain.Player;
import com.gtohelper.domain.Tag;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.function.Function;

public class SolverSettingsController {

    @FXML
    TreeTableView<String> fileTable;
    @FXML TreeTableColumn fileTableFilePathColumn;
    @FXML TreeTableColumn fileTableBoundActionColumn;

    @FXML
    TreeTableView<String> actionTable;
    @FXML TreeTableColumn<String, String> actionTableActionColumn;

    @FXML
    TextField solverLocation;

    DirectoryChooser folderChooser = new DirectoryChooser();
    SolverSettings settings = new SolverSettings();

    private Function<DirectoryChooser, File> displayFolderChooserCallback;
    public void saveDisplayFolderChooserCallback(Function<DirectoryChooser, File> callback) {
        displayFolderChooserCallback = callback;
    }

    @FXML
    private void initialize() {
        initializeControls();
        actionTable.setRoot(buildActionTableItems());
        folderChooser.setTitle("Select Range Folder");
        // todo: remove this and read it from config.prop
        //

    }

    @FXML
    private void onChooseFolderPress() {
        File result;
        try {
            File defaultFolder = new File("C:\\PioSolver Edge\\Ranges");
            folderChooser.setInitialDirectory(defaultFolder);
            result = displayFolderChooserCallback.apply(folderChooser);
        } catch(Exception e) {
            //if the default/loaded fails, we load up our current executing directory.
            File executingFolder = new File(""); // AFAIK this is the executing folder...?
            folderChooser.setInitialDirectory(executingFolder);
            result = displayFolderChooserCallback.apply(folderChooser);
        }

        selectFolder(result);
    }

    private void selectFolder(File folder) {




    }


    private void initializeControls() {
        actionTableActionColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<String, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<String, String> p) {
                return new SimpleStringProperty(p.getValue().getValue());
            }
        });


    }

    private TreeItem<String> buildActionTableItems() {
        TreeItem<String> sixMax = new TreeItem<String>("6 max positions");


        TreeItem<String> rfi = new TreeItem<String>("RFI");
        addSixMaxSeatsToTree(rfi);
        sixMax.getChildren().add(rfi);

        TreeItem<String> vsRfi = new TreeItem<String>("vs RFI");
        addSixMaxSeatsToVSTree(vsRfi);
        sixMax.getChildren().add(vsRfi);

        TreeItem<String> vs3bet = new TreeItem<String>("vs 3Bet");
        addSixMaxSeatsVS3betToTree(vs3bet);
        sixMax.getChildren().add(vs3bet);

        TreeItem<String> vs4bet = new TreeItem<String>("vs 4Bet");
        addSixMaxSeatsToVSTree(vs4bet);
        sixMax.getChildren().add(vs4bet);

        return sixMax;
    }

    private void addSixMaxSeatsToTree(TreeItem<String> root) {
        TreeItem<String> LJ = new TreeItem<String>("LJ");
        root.getChildren().add(LJ);

        TreeItem<String> HJ = new TreeItem<String>("HJ");
        root.getChildren().add(HJ);

        TreeItem<String> CO = new TreeItem<String>("CO");
        root.getChildren().add(CO);

        TreeItem<String> BU = new TreeItem<String>("BU");
        root.getChildren().add(BU);

        TreeItem<String> SB = new TreeItem<String>("SB");
        root.getChildren().add(SB);

        TreeItem<String> BB = new TreeItem<String>("BB");
        root.getChildren().add(BB);

    }

    private void addSixMaxSeatsToVSTree(TreeItem<String> root) {
        TreeItem<String> SB = new TreeItem<String>("vs SB");
        addCallRaiseToNode(addNewNodeToNode(SB, "BB"));
        root.getChildren().add(SB);

        TreeItem<String> BU = new TreeItem<String>("vs BU");
        addCallRaiseToNode(addNewNodeToNode(BU, "SB"));
        addCallRaiseToNode(addNewNodeToNode(BU, "BB"));
        root.getChildren().add(BU);

        TreeItem<String> CO = new TreeItem<String>("vs CO");
        addCallRaiseToNode(addNewNodeToNode(CO, "BU"));
        addCallRaiseToNode(addNewNodeToNode(CO, "SB"));
        addCallRaiseToNode(addNewNodeToNode(CO, "BB"));
        root.getChildren().add(CO);

        TreeItem<String> HJ = new TreeItem<String>("vs HJ");
        addCallRaiseToNode(addNewNodeToNode(HJ, "CO"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "BU"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "SB"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "BB"));
        root.getChildren().add(HJ);

        TreeItem<String> LJ = new TreeItem<String>("vs LJ");
        addCallRaiseToNode(addNewNodeToNode(LJ, "HJ"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "CO"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "BU"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "SB"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "BB"));
        root.getChildren().add(LJ);
    }

    private void addSixMaxSeatsVS3betToTree(TreeItem<String> root) {
        TreeItem<String> SB = new TreeItem<String>("SB");
        addCallRaiseToNode(addNewNodeToNode(SB, "vs BB"));
        root.getChildren().add(SB);

        TreeItem<String> BU = new TreeItem<String>("BU");
        addCallRaiseToNode(addNewNodeToNode(BU, "vs SB"));
        addCallRaiseToNode(addNewNodeToNode(BU, "vs BB"));
        root.getChildren().add(BU);

        TreeItem<String> CO = new TreeItem<String>("CO");
        addCallRaiseToNode(addNewNodeToNode(CO, "vs BU"));
        addCallRaiseToNode(addNewNodeToNode(CO, "vs SB"));
        addCallRaiseToNode(addNewNodeToNode(CO, "vs BB"));
        root.getChildren().add(CO);

        TreeItem<String> HJ = new TreeItem<String>("HJ");
        addCallRaiseToNode(addNewNodeToNode(HJ, "vs CO"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "vs BU"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "vs SB"));
        addCallRaiseToNode(addNewNodeToNode(HJ, "vs BB"));
        root.getChildren().add(HJ);

        TreeItem<String> LJ = new TreeItem<String>("LJ");
        addCallRaiseToNode(addNewNodeToNode(LJ, "vs HJ"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "vs CO"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "vs BU"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "vs SB"));
        addCallRaiseToNode(addNewNodeToNode(LJ, "vs BB"));
        root.getChildren().add(LJ);
    }

    private TreeItem<String> addNewNodeToNode(TreeItem<String> parent, String childString) {
        TreeItem<String> child = new TreeItem<String>(childString);
        parent.getChildren().add(child);
        return child;
    }

    private void addCallRaiseToNode(TreeItem<String> parent) {
        addNewNodeToNode(parent, "Call");
        addNewNodeToNode(parent, "Raise");
    }

}
