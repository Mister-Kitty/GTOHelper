package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.WorkQueueModel;
import com.gtohelper.domain.Work;
import com.gtohelper.fxml.WorkItem;
import com.gtohelper.fxml.WorkListViewCell;
import com.gtohelper.utility.Popups;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.stage.StageStyle;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WorkQueueController {
    WorkQueueModel workQueueModel = new WorkQueueModel(this::updateSolverStatusCallback, this::updateGUI);

    @FXML
    ListView<Work> finishedWork;
    ObservableList<Work> finishedWorkItems = FXCollections.observableArrayList();

    @FXML
    ListView<Work> currentWorkItem;
    ObservableList<Work> currentWorkItems = FXCollections.observableArrayList();

    @FXML
    ListView<Work> futureWorkQueue;
    ObservableList<Work> futureWorkItems = FXCollections.observableArrayList();

    @FXML
    Button startButton, stopButton;

    Supplier<String> getSolverLocationCallback;

    public WorkQueueController() {}

    @FXML
    void initialize() {
        initializeControls();
    }

    private void initializeControls() {
        finishedWork.setItems(finishedWorkItems);
        futureWorkQueue.setItems(futureWorkItems);
        currentWorkItem.setItems(currentWorkItems);

        futureWorkQueue.setCellFactory(listView -> new WorkListViewCell());

 //       finishedWork.getSelectionModel().selectedItemProperty().addListener();
    }

    public void onSelectedItemChange(ObservableValue<Work> observable, Work oldValue, Work newValue) {
        // Your action here
        //System.out.println("Selected item: " + );
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
        workQueueModel.receiveNewWork(work);
    }

    public void updateGUI() {
        Platform.runLater(() -> {
            finishedWorkItems.clear();
            finishedWorkItems.addAll(workQueueModel.getFinishedWork());

            currentWorkItems.clear();
            Work current = workQueueModel.getCurrentWork();
            if(current != null)
                currentWorkItems.add(current);

            futureWorkItems.clear();
            futureWorkItems.addAll(workQueueModel.getFutureWorkQueue());
        });
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
