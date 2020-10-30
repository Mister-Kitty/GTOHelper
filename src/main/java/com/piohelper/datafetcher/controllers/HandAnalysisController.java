package com.piohelper.datafetcher.controllers;

import com.piohelper.PT4DataManager.PT4LookupDM;
import com.piohelper.datafetcher.models.HandAnalysis;
import com.piohelper.datamanager.ILookupDM;
import com.piohelper.domain.Tag;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.ArrayList;

public class HandAnalysisController {

    @FXML
    TableView tagTable;

    HandAnalysis handAnalysis = new HandAnalysis();

    String url;
    String user;
    String pass;

    public void setConnectionInfo(String url, String user, String pass) {
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    public void refreshTags() {
        try {
            ArrayList<Tag> newHandTags = handAnalysis.getHandTags(url, user, pass);
            ObservableList<Tag> t = FXCollections.observableList(newHandTags);

            tagTable.getItems().clear();
            tagTable.getItems().addAll(t);
        }  catch (SQLException ex) {
            // throwables.printStackTrace();
        }
    }


}
