package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.HandAnalysisModel;
import com.gtohelper.domain.*;
import com.gtohelper.fxml.Board;
import com.gtohelper.fxml.Hand;
import com.gtohelper.utility.SaveFileHelper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class HandAnalysisController {

    @FXML
    TableView<Tag> tagTable;
    ObservableList<Tag> tagTableItems = FXCollections.observableArrayList();
    @FXML TableColumn<Tag, String> tagTableIdColumn;
    @FXML TableColumn<Tag, String> tagTableTagColumn;

    @FXML
    TableView<SessionBundle> sessionTable;
    ObservableList<SessionBundle> sessionTableItems = FXCollections.observableArrayList();
    @FXML TableColumn<SessionBundle, String> sessionTableDateColumn;
    @FXML TableColumn<SessionBundle, String> sessionTableLengthColumn;
    @FXML TableColumn<SessionBundle, String> sessionTableHandsColumn;
    @FXML TableColumn<SessionBundle, String> sessionTableMoneyColumn;

    @FXML
    TableView<HandData> handsTable;
    ObservableList<HandData> handsTableItems = FXCollections.observableArrayList();
    @FXML TableColumn<HandData, String> handsTableDateColumn;
    @FXML TableColumn<HandData, String> handsTableCWonColumn;
    @FXML TableColumn<HandData, Hand> handsTableHandColumn;
    @FXML TableColumn<HandData, Board> handsTableBoardColumn;

    @FXML TextField workName;
    @FXML ChoiceBox<String> betSizingsChoiceBox;
    @FXML CheckBox rakeHands;
    @FXML RadioButton percentPotRadio, bbOneHundredRadio;
    @FXML TextField percentPotField, bbOneHundredField;

    @FXML Button solveButton;
    ToggleGroup toggleGroup = new ToggleGroup();

    HandAnalysisModel handAnalysisModel;
    Player player;
    Site site;

    @FXML
    private void initialize() {
        initializeControls();
    }

    public void loadModel(SaveFileHelper saveHelper) {
        handAnalysisModel = new HandAnalysisModel(saveHelper);
        loadFieldsFromModel();
    }

    public void onConnectionSuccessStateReceive(Site site, Player player) {
        this.site = site;
        this.player = player;

        try {
            tagTableItems.clear();
            tagTableItems.addAll(handAnalysisModel.getHandTags());

            sessionTableItems.clear();
            sessionTableItems.addAll(handAnalysisModel.getAllSessionBundles(site.id_site, player.id_player));
        }  catch (SQLException ex) {
        }
    }

    public void refreshBetSettings(List<String> betSettings) {
        betSizingsChoiceBox.getItems().clear();
        betSizingsChoiceBox.getItems().addAll(betSettings);
        if(betSettings.size() > 0)
            betSizingsChoiceBox.getSelectionModel().select(0);
    }

    private Work.WorkSettings buildWorkSettings() {
        String workItemName = workName.getText();
        String betSettingName = betSizingsChoiceBox.getSelectionModel().getSelectedItem();
        boolean useRake = rakeHands.isSelected();
        float percentField;
        if(percentPotRadio.isSelected()) {
            percentField = Float.parseFloat(percentPotField.getText());
            return new Work.WorkSettings(workItemName, player, percentField, 0f, useRake, betSettingName);

        } else {
            percentField = Float.parseFloat(bbOneHundredField.getText());
            return new Work.WorkSettings(workItemName, player, 0f, percentField, useRake, betSettingName);
        }
    }

    private BiConsumer<List<HandData>, Work.WorkSettings> solveHandsCallback;
    public void saveSolveHandsCallback(BiConsumer<List<HandData>, Work.WorkSettings> callback) {
        solveHandsCallback = callback;
    }

    void loadFieldsFromModel() {


    }

    /*
        Controls & GUI interaction functions below
     */

    private void initializeControls() {
        /*
            Settings controls start here.
         */
        workName.textProperty().addListener((observableValue, oldValue, newValue) -> updateSolveButtonDisabledState());
        percentPotRadio.setToggleGroup(toggleGroup);
        percentPotField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            percentPotRadio.setSelected(true);
            updateSolveButtonDisabledState();
        });
        bbOneHundredRadio.setToggleGroup(toggleGroup);
        bbOneHundredField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            bbOneHundredRadio.setSelected(true);
            updateSolveButtonDisabledState();
        });
        toggleGroup.selectedToggleProperty().addListener((observableValue, oldValue, newValue) -> updateSolveButtonDisabledState());

        /*
            Then we'll start with Tag table
         */
        tagTableIdColumn.setCellValueFactory(p -> new SimpleStringProperty("" + p.getValue().id_tag));
        tagTableTagColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().tag));
        tagTable.setItems(tagTableItems);
        tagTable.setRowFactory(tv -> {
            TableRow<Tag> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(row.isEmpty() && event.getButton()== MouseButton.PRIMARY) {
                    handsTable.getItems().clear();
                    tagTable.getSelectionModel().clearSelection();
                } else if (event.getButton()== MouseButton.PRIMARY && event.getClickCount() == 1) {

                    Tag clickedRow = row.getItem();
                    try {
                        ArrayList<HandData> handSummaries = handAnalysisModel.getHandDataByTag(clickedRow.id_tag, player.id_player);
                        handsTableItems.clear();
                        handsTableItems.addAll(handSummaries);

                        ArrayList<SessionBundle> allSessionBundles = handAnalysisModel.getAllSessionBundles(site.id_site, player.id_player);
                        ArrayList<HandData> getHandDataByTaggedHandsInSessions = handAnalysisModel.getHandDataByTaggedHandsInSessions(allSessionBundles, 100, 7);

                    } catch (SQLException throwables) {
                        // todo: log and popup error.
                        throwables.printStackTrace();
                    }
                }
            });
            return row;
        });

        /*
            Session table
         */
        sessionTable.setItems(sessionTableItems);
        sessionTableDateColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getMinSessionStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        sessionTableHandsColumn.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().getHandCount())));
        sessionTableMoneyColumn.setCellValueFactory(p -> new SimpleStringProperty(new BigDecimal(p.getValue().getAmountWon()).setScale(2, RoundingMode.HALF_UP).toString()));
        sessionTableLengthColumn.setCellValueFactory(p -> new SimpleStringProperty(
                // No elegant way to display this apparently. Use this weird Stack Overflow suggestion.
                String.format("%d:%02d",
                        p.getValue().getDuration().getSeconds()/3600,
                        (p.getValue().getDuration().getSeconds()%3600)/60)
        ));


        /*
            Hands table
         */
        handsTable.setItems(handsTableItems);
        handsTable.setFixedCellSize(24.0); // This is a bypass around how the TableView seems to blow up the height of Board objects
        handsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        handsTable.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> updateSolveButtonDisabledState());
        handsTableDateColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().date_played.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        handsTableCWonColumn.setCellValueFactory(p -> new SimpleStringProperty("" + p.getValue().amt_pot));
        handsTableHandColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper(new Hand(p.getValue().getHandDataForPlayer(player.id_player))));
        handsTableHandColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<HandData, Hand> call(TableColumn<HandData, Hand> param) {
                return new TableCell<HandData, Hand>() {
                    @Override
                    public void updateItem(Hand hand, boolean empty) {
                        super.updateItem(hand, empty);

                        if (empty || hand == null) {
                            setGraphic(null);
                        } else {
                            setGraphic(hand);
                        }
                    }
                };
            }
        });
        handsTableBoardColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper(new Board(p.getValue())));
        handsTableBoardColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<HandData, Board> call(TableColumn<HandData, Board> param) {
                return new TableCell<HandData, Board>() {
                    @Override
                    public void updateItem(Board board, boolean empty) {
                        super.updateItem(board, empty);

                        if (empty || board == null) {
                            setGraphic(null);
                        } else {
                            setGraphic(board);
                        }
                    }
                };
            }
        });

    }

    @FXML
    private void selectAll() {
        handsTable.getSelectionModel().selectAll();
    }

    @FXML
    private void solveSelected() {
        List<HandData> handsToSolve = handsTable.getSelectionModel().getSelectedItems();
        solveHandsCallback.accept(handsToSolve, buildWorkSettings());
    }

    private void updateSolveButtonDisabledState() {
        if(areAllSolveFieldsValid())
            solveButton.disableProperty().setValue(false);
        else
            solveButton.disableProperty().setValue(true);
    }

    private boolean areAllSolveFieldsValid() {
        if(!workName.getText().isEmpty() && !handsTable.getSelectionModel().getSelectedItems().isEmpty() &&
                !betSizingsChoiceBox.getSelectionModel().getSelectedItem().isEmpty()) {
            if(toggleGroup.getSelectedToggle() != null) {
                RadioButton button = (RadioButton) toggleGroup.getSelectedToggle();
                if(button == percentPotRadio) { // note: object reference address comparison is intentional
                    if(!percentPotField.getText().isEmpty()) {
                        try { Float.parseFloat(percentPotField.getText());  } catch (NumberFormatException e) { return false; }
                        return true;
                    }
                } else {
                    if(!bbOneHundredField.getText().isEmpty()) {
                        try { Float.parseFloat(bbOneHundredField.getText());  } catch (NumberFormatException e) { return false; }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
