package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.WorkQueueModel;
import com.gtohelper.domain.*;
import com.gtohelper.fxml.*;
import com.gtohelper.utility.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class WorkQueueController {
    WorkQueueModel workQueueModel;

    @FXML
    ListView<Work> finishedWork;
    ObservableList<Work> finishedWorkItems = FXCollections.observableArrayList();

    @FXML
    ListView<Work> currentWorkItem; // This is actually only ever 1 item!. Used to make the GUI simple.
    ObservableList<Work> currentWorkItems = FXCollections.observableArrayList();

    @FXML
    ListView<Work> pendingWorkQueue;
    ObservableList<Work> pendingWorkItems = FXCollections.observableArrayList();

    @FXML
    ListView<SolveTask> solveTaskListView;
    ObservableList<SolveTask> solveTaskItems = FXCollections.observableArrayList();

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

    public void loadModel(SaveFileHelper saveHelper) {
        workQueueModel = new WorkQueueModel(saveHelper, this::updateSolverStatusCallback, this::updateWorkGUI, this::updateTaskGUIForWork);
        loadFieldsFromModel();
    }

    private void initializeControls() {
        finishedWork.setItems(finishedWorkItems);
        pendingWorkQueue.setItems(pendingWorkItems);
        currentWorkItem.setItems(currentWorkItems);
        solveTaskListView.setItems(solveTaskItems);

        finishedWork.setCellFactory(listView -> new FinishedWorkListViewCell(this));
        pendingWorkQueue.setCellFactory(listView -> new PendingWorkListViewCell(this));
        currentWorkItem.setCellFactory(listView -> new CurrentWorkListViewCell(this));
        solveTaskListView.setCellFactory(listView -> new SolveTaskListViewCell(this));

        finishedWork.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> changed("finished", oldValue, newValue));
        currentWorkItem.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> changed("current", oldValue, newValue));
        pendingWorkQueue.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> changed("pending", oldValue, newValue));
        solveTaskListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateHandDataFields(newValue));

        // Do these programmatically so that SceneBuilder still renders
        taskInfoScrollPane.setVisible(false);
    }

    public void changed(String source, Work oldValue, Work newValue) {
        taskInfoScrollPane.setVisible(true); // don't want scroll bar to render with no work picked, as said above.

        if(newValue != null) {
            selectedItem = newValue;
            if(source.equals("finished")) {
                currentWorkItem.getSelectionModel().clearSelection();
                pendingWorkQueue.getSelectionModel().clearSelection();
            } else if(source.equals("current")) {
                finishedWork.getSelectionModel().clearSelection();
                pendingWorkQueue.getSelectionModel().clearSelection();
            } else if(source.equals("pending")) {
                finishedWork.getSelectionModel().clearSelection();
                currentWorkItem.getSelectionModel().clearSelection();
            }


            // The hands list GUI needs to know the work's hero ~ which is strictly independant of the solve task.
            // So we get the hero for the work and rebuild the SolveTask GUI
            solveTaskItems.clear();
            solveTaskListView.setCellFactory(listView -> new SolveTaskListViewCell(this, selectedItem.getWorkSettings().getHero().id_player));
            solveTaskItems.addAll(selectedItem.getReadonlyTaskList());
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
        GlobalSolverSettings globalSolverSettings = getGlobalSolverSettings();
        Path solverLocation = globalSolverSettings.getSolverLocation();
        Path resultsPath = globalSolverSettings.getSolverResultsFolder();

        if(solverLocation == null) {
            Popups.showError("Piosolver location not set.");
            return;
        } else if (!Files.exists(solverLocation)) {
            Popups.showError("The set Piosolver location \"" + solverLocation.toString() + "\" does not exist or is invalid");
            return;
        }

        if(resultsPath == null) {
            Popups.showError("Solve results output folder not set.");
            return;
        } else if (!Files.exists(resultsPath)) {
            Popups.showError("The solver results output location \"" + resultsPath.toString() + "\" does not exist or is invalid");
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
        workQueueModel.addWorkToPendingQueue(work);
    }

    public void updateWorkGUI() {
        Platform.runLater(() -> {
            finishedWorkItems.clear();
            finishedWorkItems.addAll(workQueueModel.getFinishedWork());

            currentWorkItems.clear();
            Work current = workQueueModel.getCurrentWork();
            if(current != null)
                currentWorkItems.add(current);

            pendingWorkItems.clear();
            pendingWorkItems.addAll(workQueueModel.getPendingWorkQueue());
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

    public void updateTaskGUIForWork(Work work) {
        if(selectedItem.equals(work))
            solveTaskListView.refresh();
    }

    public void saveGetGlobalSolverSettingsCallback(Supplier<GlobalSolverSettings> callback) {
        getGlobalSolverSettingsCallback = callback;
    }

    private GlobalSolverSettings getGlobalSolverSettings() {
        return getGlobalSolverSettingsCallback.get();
    }

    /*

     */


    // Figure out how much is done.
    private void processCompletedWork(Work work, GlobalSolverSettings solverSettings) {
        // Find all HandIDs present in our directory.
        String directory = solverSettings.getSolverResultsFolder() + "\\" + work.getWorkSettings().getName();

        for(String fileName : (new File(directory)).list((dir, name) -> name.endsWith(".cfr"))) {
            int firstDashIndex = fileName.indexOf("-");
            if(firstDashIndex == -1)
                continue;

            String handIdString = fileName.substring(0, firstDashIndex);
            int handId = Integer.parseInt(handIdString);


        }



    }

    /*
        Load work from disk.
     */

    void loadFieldsFromModel() {
        String workOrder = workQueueModel.loadTextField("pendingWorkOrder");
        String workNameOrderResults = loadWork(getGlobalSolverSettings(), workOrder);
        if(workNameOrderResults != null) {
            workQueueModel.saveTextField("pendingWorkOrder", workNameOrderResults);
            // saveAll() write back gets executed by our parent controller after all controllers load.
        }
    }

    private String loadWork(GlobalSolverSettings solverSettings, String pendingWorkNameOrder) {
        ArrayList<Work> loadedWork;
        try {
            loadedWork = StateManager.readAllWorkObjectFiles(solverSettings);
        } catch (IOException e) {
            String error = String.format("Disk input/output error occured while trying to load work files from folder %s. \n" +
                    "Check for read permissions; or, less likely, for data corruption.", solverSettings.getSolverResultsFolder());
            Logger.log(error);
            Popups.showError(error);
            return null;
        }

        // First, search for matching work names that are supposed to exist, and load them in order.
        // When found, remove from loadedWork so that they are not double counted.
        ArrayList<String> pendingWorkNameResults = new ArrayList<>();
        for(String workName : pendingWorkNameOrder.split(",")) {
            Optional<Work> searchResult = loadedWork.stream().filter(w -> w.getWorkSettings().getName().equals(workName)).findFirst();
            if(searchResult.isPresent()) {
                Work work = searchResult.get();
                fillLoadedWorkIntoQueue(work);

                pendingWorkNameResults.add(work.getWorkSettings().getName());
                loadedWork.remove(work);
            }
        }

        // Then we append all found but not 'supposed to be there' work items to the end of the queue.
        for(Work work : loadedWork) {
            fillLoadedWorkIntoQueue(work);
            pendingWorkNameResults.add(work.getWorkSettings().getName());
        }

        return String.join(",", pendingWorkNameResults);
    }

    private void fillLoadedWorkIntoQueue(Work work) {
        if(work.hasNextTask()) {
            receiveNewWork(work);
        } else {
            finishedWorkItems.add(work);
        }
    }

    /*
        Below are functions for manipulating Work, which are called by WorkListViewCellBase extenders
     */

    public void clearErrorAndQueue(Work work) {
        workQueueModel.removeWorkFromFinished(work);
        work.clearError();
        workQueueModel.addWorkToPendingQueue(work);
    }

    public void moveWorkFileToRecycle(Work work) {
        boolean success = StateManager.recycleElseDeleteWorkFile(work);
        if(success)
            workQueueModel.removeWorkFromFinished(work);
        else
            Popups.showWarning("A problem occured. Check the logging tab for details.");
    }

    public void moveWorkFolderToRecycle(Work work) {
        boolean success = StateManager.recycleElseDeleteWorkFolder(work);
        if(success)
            workQueueModel.removeWorkFromFinished(work);
        else
            Popups.showWarning("A problem occured. Check the logging tab for details.");
    }

    public void moveWorkUp(Work work) {
        workQueueModel.moveWorkUp(work);
    }

    public void moveWorkDown(Work work) {
        workQueueModel.moveWorkDown(work);
    }

    /*
        Below are functions for manipulating SolveTask
     */

    public void ignoreSolveTaskFromCurrentWork(SolveTask solve) {
        workQueueModel.setTaskStateForWork(selectedItem, solve, SolveTask.SolveTaskState.IGNORED);
    }



}
