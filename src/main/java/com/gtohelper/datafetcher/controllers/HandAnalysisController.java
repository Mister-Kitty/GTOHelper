package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.HandAnalysisModel;
import com.gtohelper.domain.*;
import com.gtohelper.fxml.Board;
import com.gtohelper.fxml.Hand;
import com.gtohelper.utility.CardResolver;
import com.gtohelper.utility.SaveFileHelper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class HandAnalysisController {

    @FXML
    TableView<Tag> tagTable;
    @FXML TableColumn<Tag, String> tagTableIdColumn;
    @FXML TableColumn<Tag, String> tagTableTagColumn;

    @FXML
    TableView<HandData> handsTable;
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

    @FXML
    private void initialize()
    {
        initializeControls();
    }

    public void loadModel(SaveFileHelper saveHelper) {
        handAnalysisModel = new HandAnalysisModel(saveHelper);
        loadFieldsFromModel();
    }

    public void refreshTags(Player p) {
        try {
            player = p;
            ArrayList<Tag> newHandTags = handAnalysisModel.getHandTags();
            ObservableList<Tag> t = FXCollections.observableList(newHandTags);

            tagTable.getItems().clear();
            tagTable.getItems().addAll(t);
        }  catch (SQLException ex) {
        }
    }

    public void refreshBetSettings(List<String> betSettings) {
        betSizingsChoiceBox.getItems().clear();
        betSizingsChoiceBox.getItems().addAll(betSettings);
        if(betSettings.size() > 0)
            betSizingsChoiceBox.getSelectionModel().select(0);
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

    private void initializeControls() {
        /*
            Work settings controls here.
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
            Table controls here.
         */
        // I could annotate the domain objects to make this more elegant... I may do it latter.
        tagTableIdColumn.setCellValueFactory(p -> new SimpleStringProperty("" + p.getValue().id_tag));
        tagTableTagColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().tag));
        tagTable.setRowFactory(tv -> {
            TableRow<Tag> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (! row.isEmpty() && event.getButton()== MouseButton.PRIMARY
                        && event.getClickCount() == 1) {

                    Tag clickedRow = row.getItem();
                    try {
                        ArrayList<HandData> handSummaries = handAnalysisModel.getHandSummariesByTag(clickedRow.id_tag, player.id_player);
                        ObservableList<HandData> t = FXCollections.observableList(handSummaries);

                        handsTable.getItems().clear();
                        handsTable.getItems().addAll(t);
                    } catch (SQLException throwables) {
                        // todo: log and popup error.
                        throwables.printStackTrace();
                    }
                }
            });
            return row;
        });

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

    void loadFieldsFromModel() {


    }
}
