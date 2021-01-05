package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.WorkQueueModel;
import com.gtohelper.domain.Work;
import com.gtohelper.fxml.WorkListViewCell;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WorkQueueController {
    WorkQueueModel workQueueModel = new WorkQueueModel();

    @FXML
    ListView<Work> currentWorkQueue;

    Supplier<String> getSolverLocationCallback;

    public WorkQueueController() {}

    @FXML
    void initialize() {
        currentWorkQueue.setCellFactory(listView -> new WorkListViewCell());
    }

    @FXML
    public void startWorker() {
        String solverLocation = getSolverLocationCallback.get();
        workQueueModel.startWorker(solverLocation);
    }

    @FXML
    public void stopWorker() {
        workQueueModel.stopWorker();
    }

    public void receiveNewWork(Work work) {
        currentWorkQueue.getItems().add(work);
        workQueueModel.receiveNewWork(work);
    }

    public void saveGetSolverLocationCallback(Supplier<String> callback) {
        getSolverLocationCallback = callback;
    }
}
