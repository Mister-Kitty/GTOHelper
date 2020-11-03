package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.HandAnalysis;
import com.gtohelper.domain.HandSummary;
import com.gtohelper.domain.Tag;
import com.gtohelper.utility.CardResolver;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableFloatValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;

import java.sql.SQLException;
import java.util.ArrayList;

public class HandAnalysisController {

    @FXML
    TableView<Tag> tagTable;
    @FXML TableColumn<Tag, String> tagTableIdColumn;
    @FXML TableColumn<Tag, String> tagTableTagColumn;

    @FXML
    TableView<HandSummary> handsTable;
    @FXML TableColumn<HandSummary, String> handsTableDateColumn;
    @FXML TableColumn<HandSummary, String> handsTableCWonColumn;
    @FXML TableColumn<HandSummary, String> handsTableCardsColumn;
    @FXML TableColumn<HandSummary, String> handsTableRunoutColumn;


    HandAnalysis handAnalysis = new HandAnalysis();

    @FXML
    private void initialize()
    {
        initializeControls();
    }

    public void refreshTags() {
        try {
            ArrayList<Tag> newHandTags = handAnalysis.getHandTags();
            ObservableList<Tag> t = FXCollections.observableList(newHandTags);

            tagTable.getItems().clear();
            tagTable.getItems().addAll(t);
        }  catch (SQLException ex) {
        }
    }

    private void initializeControls() {

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
                        ArrayList<HandSummary> handSummaries = handAnalysis.getHandSummariesByTag(clickedRow.id_tag);
                        ObservableList<HandSummary> t = FXCollections.observableList(handSummaries);

                        handsTable.getItems().clear();
                        handsTable.getItems().addAll(t);
                    } catch (SQLException throwables) {
                        //throwables.printStackTrace();
                    }
                }
            });
            return row ;
        });

        handsTableDateColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<HandSummary, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<HandSummary, String> p) {
                return new SimpleStringProperty(p.getValue().date_played.toString());
            }
        });

        handsTableCWonColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<HandSummary, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<HandSummary, String> p) {
                return new SimpleStringProperty("" + p.getValue().amt_pot);
            }
        });

        handsTableCardsColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<HandSummary, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<HandSummary, String> p) {
                return new SimpleStringProperty("" + p.getValue().hand);
            }
        });

        handsTableRunoutColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<HandSummary, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<HandSummary, String> p) {
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
