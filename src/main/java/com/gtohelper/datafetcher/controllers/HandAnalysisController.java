package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.HandAnalysis;
import com.gtohelper.domain.*;
import com.gtohelper.utility.CardResolver;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableFloatValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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

    @FXML Button solveButton;

    HandAnalysis handAnalysis = new HandAnalysis();
    Player player;

    @FXML
    private void initialize()
    {
        initializeControls();
    }

    public void refreshTags(Player p) {
        try {
            player = p;
            ArrayList<Tag> newHandTags = handAnalysis.getHandTags();
            ObservableList<Tag> t = FXCollections.observableList(newHandTags);

            tagTable.getItems().clear();
            tagTable.getItems().addAll(t);
        }  catch (SQLException ex) {
        }
    }

    @FXML
    private void selectAll() {
        handsTable.getSelectionModel().selectAll();
    }

    @FXML
    private void solveSelected() {
       List<HandData> handsToSolve = handsTable.getSelectionModel().getSelectedItems();
        solveHandsCallback.accept(handsToSolve);
    }

    private Consumer<List<HandData>> solveHandsCallback;
    public void saveSolveHandsCallback(Consumer<List<HandData>> callback) {
        solveHandsCallback = callback;
    }

    private void initializeControls() {

        handsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        handsTable.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
                if(newValue == null)
                    solveButton.disableProperty().setValue(true);
                else
                    solveButton.disableProperty().setValue(false);
            }
        );

        // I could annotate the domain objects to make this more elegant... I may do it latter.
        tagTableIdColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Tag, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Tag, String> p) {
                return new SimpleStringProperty("" + p.getValue().id_tag);
            }
        });

        tagTableTagColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Tag, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Tag, String> p) {
                return new SimpleStringProperty(p.getValue().tag);
            }
        });

        tagTable.setRowFactory(tv -> {
            TableRow<Tag> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (! row.isEmpty() && event.getButton()== MouseButton.PRIMARY
                        && event.getClickCount() == 1) {

                    Tag clickedRow = row.getItem();
                    try {
                        ArrayList<HandData> handSummaries = handAnalysis.getHandSummariesByTag(clickedRow.id_tag, player.id_player);
                        ObservableList<HandData> t = FXCollections.observableList(handSummaries);

                        handsTable.getItems().clear();
                        handsTable.getItems().addAll(t);
                    } catch (SQLException throwables) {
                        //throwables.printStackTrace();
                    }
                }
            });
            return row ;
        });

        handsTableDateColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<HandData, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<HandData, String> p) {
                return new SimpleStringProperty(p.getValue().date_played.toString());
            }
        });

        handsTableCWonColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<HandData, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<HandData, String> p) {
                return new SimpleStringProperty("" + p.getValue().amt_pot);
            }
        });

        handsTableCardsColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<HandData, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<HandData, String> p) {
                String result = CardResolver.resolveToString(p.getValue().holecard_1) + " " +
                    CardResolver.resolveToString(p.getValue().holecard_2);

                return new SimpleStringProperty(result);
            }
        });

        handsTableRunoutColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<HandData, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<HandData, String> p) {
                String runout = CardResolver.resolveToString(p.getValue().card_1) + " " +
                        CardResolver.resolveToString(p.getValue().card_2) + " " +
                        CardResolver.resolveToString(p.getValue().card_3) + " " +
                        CardResolver.resolveToString(p.getValue().card_4) + " " +
                        CardResolver.resolveToString(p.getValue().card_5);
                return new SimpleStringProperty(runout);
            }
        });
    }
}
