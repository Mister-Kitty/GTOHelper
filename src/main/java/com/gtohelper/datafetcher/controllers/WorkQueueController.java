package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.WorkQueueModel;
import com.gtohelper.domain.Work;
import com.gtohelper.fxml.WorkListViewCell;
import com.gtohelper.utility.Popups;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WorkQueueController {
    WorkQueueModel workQueueModel = new WorkQueueModel(this::updateSolverStatusCallback);

    @FXML
    ListView<Work> currentWorkQueue;

    @FXML
    Button startButton, stopButton;

    Supplier<String> getSolverLocationCallback;

    public WorkQueueController() {}

    @FXML
    void initialize() {
        currentWorkQueue.setCellFactory(listView -> new WorkListViewCell());
    }

    @FXML
    public void startWorker() {
        String solverLocation = getSolverLocationCallback.get();
        if(solverLocation.isEmpty()) {
            Popups.showError("Piosolver location not set.");
            return;
        }

        if (!new File(solverLocation).exists()) {
            Popups.showError("The set Piosolver location \"" + solverLocation + "\" does not exist or is invalid");
            return;
        }

        boolean success = workQueueModel.startWorker(solverLocation);

        if(!success)
            Popups.showError("Error occured when launching Pio. See debug tab for details.");
    }

    @FXML
    public void stopWorker() {
        workQueueModel.stopWorker();
    }

    public void receiveNewWork(Work work) {
        currentWorkQueue.getItems().add(work);
        workQueueModel.receiveNewWork(work);
    }

    public void updateSolverStatusCallback(Boolean isRunning) {
        if(isRunning) {
            startButton.setDisable(true);
            stopButton.setDisable(false);
        } else {
            startButton.setDisable(false);
            stopButton.setDisable(true);
        }
    }

    public void saveGetSolverLocationCallback(Supplier<String> callback) {
        getSolverLocationCallback = callback;
    }
}
