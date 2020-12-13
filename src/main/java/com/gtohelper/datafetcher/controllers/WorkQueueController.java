package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.WorkQueueModel;
import com.gtohelper.domain.Work;
import com.gtohelper.fxml.WorkListViewCell;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class WorkQueueController {
    WorkQueueModel workQueueModel = new WorkQueueModel();

    @FXML
    ListView<Work> currentWorkQueue;

    public WorkQueueController() {}

    @FXML
    void initialize() {
        currentWorkQueue.setCellFactory(listView -> new WorkListViewCell());
    }

    public void receiveNewWork(Work work) {
        currentWorkQueue.getItems().add(work);
        workQueueModel.receiveNewWork(work);
    }
}
