package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.HandAnalysisModel;
import com.gtohelper.domain.*;
import com.gtohelper.utility.CardResolver;
import com.gtohelper.utility.SaveFileHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;

public class HandAnalysisController {

    @FXML
    TableView<Tag> tagTable;
    @FXML TableColumn<Tag, String> tagTableIdColumn;
    @FXML TableColumn<Tag, String> tagTableTagColumn;

    @FXML
    TableView<HandData> handsTable;
    @FXML TableColumn<HandData, String> handsTableDateColumn;
    @FXML TableColumn<HandData, String> handsTableCWonColumn;
    @FXML TableColumn<HandData, String> handsTableCardsColumn;
    @FXML TableColumn<HandData, String> handsTableRunoutColumn;

    @FXML
    ChoiceBox<String> scheduleChoiceBox;

    @FXML
    ChoiceBox<String> betSizingsChoiceBox;

    @FXML Button solveButton;

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
        String workItemName = "Hands for Dec 16";
        String betSettingName = betSizingsChoiceBox.getSelectionModel().getSelectedItem();
        boolean useRake = true;
        float percentPot = 0.5f;

        return new Work.WorkSettings(workItemName, player, percentPot, useRake, betSettingName);
    }

    private BiConsumer<List<HandData>, Work.WorkSettings> solveHandsCallback;
    public void saveSolveHandsCallback(BiConsumer<List<HandData>, Work.WorkSettings> callback) {
        solveHandsCallback = callback;
    }

    private void initializeControls() {
        scheduleChoiceBox.getItems().add("Now");
        scheduleChoiceBox.getSelectionModel().select("Now");

        handsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        handsTable.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if(newValue == null)
                solveButton.disableProperty().setValue(true);
            else
                solveButton.disableProperty().setValue(false);
        });

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
            return row ;
        });

        handsTableDateColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().date_played.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        handsTableCWonColumn.setCellValueFactory(p -> new SimpleStringProperty("" + p.getValue().amt_pot));
        handsTableCardsColumn.setCellValueFactory(p -> new SimpleStringProperty(CardResolver.getHandStringForPlayer(player, p.getValue())));
        handsTableRunoutColumn.setCellValueFactory(p -> new SimpleStringProperty(CardResolver.getBoardString(p.getValue())));
    }

    void loadFieldsFromModel() {


    }
}
