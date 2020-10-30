package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.HandAnalysis;
import com.gtohelper.domain.Tag;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.sql.SQLException;
import java.util.ArrayList;

public class HandAnalysisController {

    @FXML
    TableView<Tag> tagTable;

    @FXML TableColumn<Tag, String> tagTableIdColumn;
    @FXML TableColumn<Tag, String> tagTableTagColumn;

    HandAnalysis handAnalysis = new HandAnalysis();

    @FXML
    private void initialize()
    {

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
}
