package com.gtohelper.fxml;

import com.gtohelper.datafetcher.controllers.WorkQueueController;
import com.gtohelper.domain.Work;
import com.gtohelper.utility.Popups;
import javafx.scene.control.MenuItem;

public class CurrentWorkListViewCell extends WorkListViewCellBase {

    MenuItem moveToPendingWorkQueue = new MenuItem();
    MenuItem delete = new MenuItem();

    public CurrentWorkListViewCell(WorkQueueController controller) {
        super(controller);
        initializeContextMenu();
    }

    protected void initializeContextMenu() {
        moveToPendingWorkQueue.setText("Move to pending queue (stops solver)");
        moveToPendingWorkQueue.setOnAction(event -> workController.moveFromCurrentToPendingWorkQueue(currentWork));

        delete.setText("Delete");
        delete.setOnAction(event -> {
            boolean choice = Popups.showConfirmation(String.format("Delete work item %s's .gto file from disk? Solve CFG files will remain.", currentWork.toString()));
            if(choice)
                workController.moveWorkFileToRecycle(currentWork);
        });


        contextMenu.getItems().add(moveToPendingWorkQueue);
        contextMenu.getItems().add(delete);
    }

    @Override
    protected void setMenuItemEnableStates(Work work) {


    }
}
