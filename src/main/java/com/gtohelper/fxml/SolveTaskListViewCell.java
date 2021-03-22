package com.gtohelper.fxml;

import com.gtohelper.datafetcher.controllers.WorkQueueController;
import com.gtohelper.datafetcher.controllers.WorkQueueController.QueueType;
import com.gtohelper.domain.SolveTask;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class SolveTaskListViewCell extends ListCell<SolveTask> {
    ContextMenu contextMenu = new ContextMenu();
    MenuItem ignore = new MenuItem();
    MenuItem clearErrorIgnore = new MenuItem();

    WorkQueueController workController;
    QueueType source;
    SolveTask thisSolveTask;
    int heroId;

    boolean isSolving = false;

    public SolveTaskListViewCell(WorkQueueController controller, QueueType sourceQueue, int heroPlayerId) {
        this(controller);
        heroId = heroPlayerId;
        source = sourceQueue;
        initializeContextMenu();
    }

    // I think we can actually just remove the Controller call to this.... whatever.
    public SolveTaskListViewCell(WorkQueueController controller) {
        super();
        workController = controller;
    }

    private void initializeContextMenu() {
        ignore.setText("Skip / ignore");
        ignore.setOnAction(event ->  workController.ignoreSolveTask(thisSolveTask, source));

        clearErrorIgnore.setText("Clear error / ignore state");
        clearErrorIgnore.setOnAction(event ->  workController.clearErrorIgnoreSolveTask(thisSolveTask, source));

        contextMenu.getItems().add(ignore);
        contextMenu.getItems().add(clearErrorIgnore);
    }

    @Override
    public void updateItem(SolveTask task, boolean empty) {
        super.updateItem(task, empty);
        clearSolveTaskStyleClasses();

        if (empty || task == null) {
            setGraphic(null);
            setContextMenu(null);
            thisSolveTask = null;
        } else {
            if (task.getSolveState() == SolveTask.SolveTaskState.COMPLETED)
                getStyleClass().add("solve-task-completed");
            else if (task.getSolveState() == SolveTask.SolveTaskState.ERRORED)
                getStyleClass().add("solve-task-errored");
            else if (task.getSolveState() == SolveTask.SolveTaskState.SKIPPED)
                getStyleClass().add("solve-task-skipped");
            else if (task.getSolveState() == SolveTask.SolveTaskState.CFG_FOUND)
                getStyleClass().add("solve-task-cfg-found");

            /*
                This is rather lazy. Really, this should be in a fxml....
             */
            HBox box = new HBox();
            Board b = new Board(task.getHandData());
            Text dash = new Text(" - ");
            Hand h = new Hand(task.getHandData().getHandDataForPlayer(heroId));

            box.getChildren().add(h);
            box.getChildren().add(dash);
            box.getChildren().add(b);

            isSolving = workController.getCurrentlySolvingTask() != null && workController.getCurrentlySolvingTask().equals(task);
            if(isSolving) {
                ProgressIndicator p = new ProgressIndicator();
                p.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                p.setMaxSize(16, 16); // Should be setScale(), but that function isn't behaving well

                box.getChildren().add(new Text("  "));
                box.getChildren().add(p);
            }

            thisSolveTask = task;
            setContextMenu(contextMenu);
            setMenuItemEnableStates(task);
            setGraphic(box);
        }
    }

    private void clearSolveTaskStyleClasses() {
        getStyleClass().removeIf(style -> style.startsWith("solve-task"));
    }

    private void setMenuItemEnableStates(SolveTask task) {
        ignore.disableProperty().set(true);
        clearErrorIgnore.disableProperty().set(true);

        SolveTask.SolveTaskState solveState = task.getSolveState();
        if(isSolving) {
            return;
        } else if (solveState == SolveTask.SolveTaskState.NEW) {
            ignore.disableProperty().set(false);
        } else if (solveState == SolveTask.SolveTaskState.CFG_FOUND) {
            ignore.disableProperty().set(false);
        } else if (solveState == SolveTask.SolveTaskState.SKIPPED) {
            clearErrorIgnore.disableProperty().set(false);
        } else if (solveState == SolveTask.SolveTaskState.COMPLETED) {

        } else if (solveState == SolveTask.SolveTaskState.ERRORED) {
            ignore.disableProperty().set(false);
            clearErrorIgnore.disableProperty().set(false);
        }
    }
}
