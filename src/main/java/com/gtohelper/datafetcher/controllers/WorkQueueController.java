package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.WorkQueueModel;
import com.gtohelper.domain.*;
import com.gtohelper.fxml.WorkListViewCell;
import com.gtohelper.utility.CardResolver;
import com.gtohelper.utility.Popups;
import com.gtohelper.utility.StateManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    ListView<SolveTask> taskList;
    ObservableList<SolveTask> handsListItems = FXCollections.observableArrayList();

    @FXML
    ScrollPane taskInfoScrollPane;

    @FXML
    Work selectedItem;

    @FXML
    TextField handID, datePlayed, limit, potInBB, PFBetLevel, BBEffective, solveSuitability;
    @FXML
    TextField OOPName, OOPSeat, OOPHand, OOPAction;
    @FXML
    TextField IPName, IPSeat, IPHand, IPAction;

    @FXML
    Text taskStateHeader, taskStateText1, taskStateText2, taskStateText3, taskStateText4;
    @FXML
    TextField taskStateField1, taskStateField2, taskStateField3, taskStateField4;
    @FXML
    Hyperlink viewInBrowser, viewInPioViewer;

    @FXML
    Button startButton, stopButton;

    Supplier<GlobalSolverSettings> getGlobalSolverSettingsCallback;

    public WorkQueueController() {}

    @FXML
    void initialize() {
        initializeControls();
    }

    private void initializeControls() {
        finishedWork.setItems(finishedWorkItems);
        futureWorkQueue.setItems(futureWorkItems);
        currentWorkItem.setItems(currentWorkItems);
        taskList.setItems(handsListItems);

        finishedWork.setCellFactory(listView -> new WorkListViewCell());
        futureWorkQueue.setCellFactory(listView -> new WorkListViewCell());
        currentWorkItem.setCellFactory(listView -> new WorkListViewCell());
        taskList.setCellFactory(new Callback<ListView<SolveTask>, ListCell<SolveTask>>() {
            @Override
            public ListCell<SolveTask> call(ListView<SolveTask> param) {
                return new ListCell<SolveTask>() {
                    @Override
                    public void updateItem(SolveTask task, boolean empty)
                    {
                        super.updateItem(task,empty);
                        if (empty || task == null)
                            setText(null);
                        else {

                            if(task.getSolveState() == SolveTask.SolveTaskState.COMPLETED)
                                getStyleClass().add("solve-task-completed");
                            else if (task.getSolveState() == SolveTask.SolveTaskState.ERRORED)
                                getStyleClass().add("solve-task-errored");
                            else if (task.getSolveState() == SolveTask.SolveTaskState.IGNORED)
                                getStyleClass().add("solve-task-ignored");

                            setText(CardResolver.getBoardString(task.getHandData()));
                        }
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
        taskList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateHandDataFields(newValue));

        // Do these programmatically so that SceneBuilder still renders
        taskInfoScrollPane.setVisible(false);
    }

    public void changed(String source, Work oldValue, Work newValue) {
        taskInfoScrollPane.setVisible(true);

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
            handsListItems.addAll(selectedItem.getReadonlyTaskList());
        }
    }

    private void updateHandDataFields(SolveTask task) {
        if(task == null) {
            handID.setText(""); datePlayed.setText(""); limit.setText(""); potInBB.setText(""); PFBetLevel.setText("");
            BBEffective.setText(""); solveSuitability.setText("");
            OOPName.setText(""); OOPSeat.setText(""); OOPHand.setText(""); OOPAction.setText("");
            IPName.setText(""); IPSeat.setText(""); IPHand.setText(""); IPAction.setText("");

            setSolveStateFieldsVisibility(false);
        } else {
            HandData handData = task.getHandData();
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
            OOPAction.setText(handData.oopPlayer.getCompactedActionStrings("  |  "));
            IPName.setText(handData.ipPlayer.player_name);
            IPSeat.setText(handData.ipPlayer.seat.toString());
            IPHand.setText(CardResolver.getHandString(handData.ipPlayer));
            IPAction.setText(handData.ipPlayer.getCompactedActionStrings("  |  "));

            if(task.getSolveState() == SolveTask.SolveTaskState.COMPLETED)
                setCompletedSolveStateFields();
            else if(task.getSolveState() == SolveTask.SolveTaskState.IGNORED)
                setIgnoredSolveStateFields();
            else if(task.getSolveState() == SolveTask.SolveTaskState.NEW)
                setNewSolveStateFields();
            else if(task.getSolveState() == SolveTask.SolveTaskState.ERRORED)
                setErroredSolveStateFields();
        }
    }

    private void setSolveStateFieldsVisibility(boolean visibility) {
        taskStateText1.setVisible(visibility); taskStateText2.setVisible(visibility); taskStateText3.setVisible(visibility); taskStateText4.setVisible(visibility);
        taskStateField1.setVisible(visibility); taskStateField2.setVisible(visibility); taskStateField3.setVisible(visibility); taskStateField4.setVisible(visibility);
    }

    private void setCompletedSolveStateFields() {

    }

    private void setIgnoredSolveStateFields() {

    }

    private void setNewSolveStateFields() {

    }

    private void setErroredSolveStateFields() {

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

    public void loadWork(GlobalSolverSettings solverSettings) {
        ArrayList<Work> loadedWork = StateManager.readAllWorkObjectFiles(solverSettings);
        for(Work work : loadedWork) {
            if(work.isCompleted()) {
                finishedWorkItems.add(work);
            } else {
                receiveNewWork(work);
            }
        }
    }

    // Figure out how much is done.
    private void processCompletedWork(Work work, GlobalSolverSettings solverSettings) {
        // Find all HandIDs present in our directory.
        String directory = solverSettings.getSolveResultsFolder() + "\\" + work.getWorkSettings().getName();

        for(String fileName : (new File(directory)).list((dir, name) -> name.endsWith(".cfr"))) {
            int firstDashIndex = fileName.indexOf("-");
            if(firstDashIndex == -1)
                continue;

            String handIdString = fileName.substring(0, firstDashIndex);
            int handId = Integer.parseInt(handIdString);


        }



    }
}
