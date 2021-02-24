package com.gtohelper.fxml;

import com.gtohelper.datafetcher.controllers.WorkQueueController;
import com.gtohelper.domain.Work;
import com.gtohelper.utility.Popups;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class FinishedWorkListViewCell extends WorkListViewCellBase {
    MenuItem compressAndArchive = new MenuItem();
    SeparatorMenuItem separator = new SeparatorMenuItem();
    MenuItem moveToWorkQueue = new MenuItem();
    MenuItem delete = new MenuItem();

    public FinishedWorkListViewCell(WorkQueueController controller) {
        super(controller);
        initializeContextMenu();
    }

    protected void initializeContextMenu() {

        compressAndArchive.setText("Compress & Archive");
        moveToWorkQueue.setText("Move to Work Queue");
        moveToWorkQueue.setOnAction(event -> workController.moveFromFinishedToFutureWorkQueue(currentWork));
        delete.setText("Delete");
        moveToWorkQueue.setOnAction(event -> {
            boolean choice = Popups.showConfirmation(String.format("Delete work item %s's .gto file from disk? Solve CFG files will remain.", currentWork.toString()));
            if(choice)
                workController.deleteWorkFileFromDisk(currentWork);
        });

        //     delete.setOnAction(event -> );

        contextMenu.getItems().add(moveToWorkQueue);
        contextMenu.getItems().add(separator);
        contextMenu.getItems().add(compressAndArchive);
        contextMenu.getItems().add(delete);
    }

    @Override
    protected void setMenuItemEnableStates(Work work) {
        compressAndArchive.disableProperty().set(true);
    }
}
