package com.gtohelper.fxml;

import com.gtohelper.datafetcher.controllers.WorkQueueController;
import com.gtohelper.domain.SolveTask;
import com.gtohelper.domain.Work;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class SolveTaskListViewCell extends ListCell<SolveTask> {
    ContextMenu contextMenu = new ContextMenu();
    MenuItem ignore = new MenuItem();

    WorkQueueController workController;
    SolveTask thisSolveTask;
    int heroId;

    public SolveTaskListViewCell(WorkQueueController controller, int heroPlayerId) {
        this(controller);
        heroId = heroPlayerId;
    }

    public SolveTaskListViewCell(WorkQueueController controller) {
        super();
        workController = controller;
        initializeContextMenu();
    }

    private void initializeContextMenu() {
        ignore.setText("Skip / ignore");
        ignore.setOnAction(event ->  workController.ignoreSolveTaskFromCurrentWork(thisSolveTask));

        contextMenu.getItems().add(ignore);
    }

    @Override
    public void updateItem(SolveTask task, boolean empty) {
        super.updateItem(task, empty);

        if (empty || task == null) {
            setGraphic(null);
            setContextMenu(null);
            thisSolveTask = null;
        } else {

            if (task.getSolveState() == SolveTask.SolveTaskState.COMPLETED)
                getStyleClass().add("solve-task-completed");
            else if (task.getSolveState() == SolveTask.SolveTaskState.ERRORED)
                getStyleClass().add("solve-task-errored");
            else if (task.getSolveState() == SolveTask.SolveTaskState.IGNORED)
                getStyleClass().add("solve-task-ignored");
            else if (task.getSolveState() == SolveTask.SolveTaskState.CFG_FOUND)
                getStyleClass().add("solve-task-cfg-found");

            /*
                This is rather lazy. Really, this should be in a fxml....
             */
            HBox box = new HBox();
            Board b = new Board(task.getHandData());
            Text space = new Text(" - ");
            Hand h = new Hand(task.getHandData().getHandDataForPlayer(heroId));

            box.getChildren().add(h);
            box.getChildren().add(space);
            box.getChildren().add(b);

            thisSolveTask = task;
            setContextMenu(contextMenu);
            setMenuItemEnableStates(task);
            setGraphic(box);
        }
    }

    private void setMenuItemEnableStates(SolveTask task) {
        if(task.getSolveState() == SolveTask.SolveTaskState.COMPLETED)
            ignore.disableProperty().set(true);
        else
            ignore.disableProperty().set(false);



    }
}
