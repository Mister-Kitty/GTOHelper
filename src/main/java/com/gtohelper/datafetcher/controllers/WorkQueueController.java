package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.WorkQueueModel;
import com.gtohelper.domain.GlobalSolverSettings;
import com.gtohelper.domain.HandData;
import com.gtohelper.domain.Work;
import com.gtohelper.fxml.WorkItem;
import com.gtohelper.fxml.WorkListViewCell;
import com.gtohelper.utility.CardResolver;
import com.gtohelper.utility.Popups;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import javax.swing.event.ChangeEvent;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WorkQueueController {
    WorkQueueModel workQueueModel = new WorkQueueModel(this::updateSolverStatusCallback, this::updateGUI);

    @FXML
    ListView<Work> finishedWork;
    ObservableList<Work> finishedWorkItems = FXCollections.observableArrayList();

    @FXML
    ListView<Work> currentWorkItem; // This is actually only ever 1 item!. Used to make the GUI simple.
    ObservableList<Work> currentWorkItems = FXCollections.observableArrayList();

    @FXML
    ListView<Work> futureWorkQueue;
    ObservableList<Work> futureWorkItems = FXCollections.observableArrayList();

    @FXML
    ListView<HandData> handsList;
    ObservableList<HandData> handsListItems = FXCollections.observableArrayList();

    @FXML
    Work selectedItem;

    @FXML
    TextField handID, datePlayed, limit, potInBB, PFBetLevel, BBEffective, solveSuitability;
    @FXML
    TextField OOPName, OOPSeat, OOPHand, OOPPFAction;
    @FXML
    TextField IPName, IPSeat, IPHand, IPPFAction;
    @FXML
    Button startButton, stopButton;

    Supplier<GlobalSolverSettings> getGlobalSolverSettingsCallback;

    public WorkQueueController() {}

    @FXML
    void initialize() {
        initializeControls();
        loadAllWorkState();
    }

    private void initializeControls() {
        finishedWork.setItems(finishedWorkItems);
        futureWorkQueue.setItems(futureWorkItems);
        currentWorkItem.setItems(currentWorkItems);
        handsList.setItems(handsListItems);

        finishedWork.setCellFactory(listView -> new WorkListViewCell());
        futureWorkQueue.setCellFactory(listView -> new WorkListViewCell());
        currentWorkItem.setCellFactory(listView -> new WorkListViewCell());
        handsList.setCellFactory(new Callback<ListView<HandData>, ListCell<HandData>>() {
            @Override
            public ListCell<HandData> call(ListView<HandData> param) {
                return new ListCell<HandData>() {
                    @Override
                    public void updateItem(HandData handData, boolean empty)
                    {
                        super.updateItem(handData,empty);
                        if (empty || handData == null)
                            setText(null);
                        else
                            setText(CardResolver.getBoardString(handData));
                    }
                };
            }
        });

        finishedWork.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> changed("finished", oldValue, newValue));
        currentWorkItem.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> changed("current", oldValue, newValue));
        futureWorkQueue.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> changed("future", oldValue, newValue));
        handsList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateHandDataFields(newValue));
    }

    public void changed(String source, Work oldValue, Work newValue) {
        if(newValue != null) {
            selectedItem = newValue;
            if(source.equals("finished")) {
                currentWorkItem.getSelectionModel().clearSelection();
                futureWorkQueue.getSelectionModel().clearSelection();
            } else if(source.equals("current")) {
                finishedWork.getSelectionModel().clearSelection();
                futureWorkQueue.getSelectionModel().clearSelection();
            } else if(source.equals("future")) {
                finishedWork.getSelectionModel().clearSelection();
                currentWorkItem.getSelectionModel().clearSelection();
            }

            handsListItems.clear();
            handsListItems.addAll(selectedItem.getHandDataList());
        }
    }

    private void updateHandDataFields(HandData handData) {
        if(handData == null) {
            handID.setText(""); datePlayed.setText(""); limit.setText(""); potInBB.setText(""); PFBetLevel.setText("");
            BBEffective.setText(""); solveSuitability.setText("");
            OOPName.setText(""); OOPSeat.setText(""); OOPHand.setText(""); OOPPFAction.setText("");
            IPName.setText(""); IPSeat.setText(""); IPHand.setText(""); IPPFAction.setText("");
        } else {
            handID.setText(""+handData.id_hand);
            datePlayed.setText(handData.date_played.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            limit.setText(handData.limit_name);
            potInBB.setText(""+handData.getAsNumberOfBB(handData.amt_pot, 2));
            PFBetLevel.setText(""+handData.highestPreflopBetLevel);
            BBEffective.setText(""+handData.getAsNumberOfBB(handData.getIPandOOPEffective(), 2));
            solveSuitability.setText(handData.solveabilityLevel.toString());
            OOPName.setText(handData.oopPlayer.player_name);
            OOPSeat.setText(handData.oopPlayer.seat.toString());
            OOPHand.setText(CardResolver.getHandString(handData.oopPlayer));
            OOPPFAction.setText(handData.oopPlayer.p_action);
            IPName.setText(handData.ipPlayer.player_name);
            IPSeat.setText(handData.ipPlayer.seat.toString());
            IPHand.setText(CardResolver.getHandString(handData.oopPlayer));
            IPPFAction.setText(handData.ipPlayer.p_action);
        }
    }

    @FXML
    public void startWorker() {
        GlobalSolverSettings globalSolverSettings = getGlobalSolverSettingsCallback.get();
        String solverLocation = globalSolverSettings.getSolverLocation();

        if(globalSolverSettings.getSolverLocation().isEmpty()) {
            Popups.showError("Piosolver location not set.");
            return;
        }

        if(globalSolverSettings.getSolveResultsFolder().isEmpty()) {
            Popups.showError("Solve results output folder not set.");
            return;
        }

        if (!new File(solverLocation).exists()) {
            Popups.showError("The set Piosolver location \"" + solverLocation + "\" does not exist or is invalid");
            return;
        }

        boolean success = workQueueModel.startWorker(globalSolverSettings);

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


    public void saveGetGlobalSolverSettingsCallback(Supplier<GlobalSolverSettings> callback) {
        getGlobalSolverSettingsCallback = callback;
    }

    /*

     */

    private void saveAllWorkState() {


    }

    private void loadAllWorkState() {


    }
}
