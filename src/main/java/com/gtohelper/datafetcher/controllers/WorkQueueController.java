package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.WorkQueue;
import com.gtohelper.domain.Work;
import com.gtohelper.fxml.WorkListViewCell;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class WorkQueueController {
    WorkQueue workQueue = new WorkQueue();

    @FXML
    ListView<Work> currentWorkQueue;

    public WorkQueueController() {}

    @FXML
    void initialize() {
        currentWorkQueue.setCellFactory(listView -> new WorkListViewCell());
    }

    public void receiveNewWork(Work work) {
        currentWorkQueue.getItems().add(work);
        workQueue.receiveNewWork(work);
    }
}
