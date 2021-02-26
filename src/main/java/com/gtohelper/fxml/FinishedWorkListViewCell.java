package com.gtohelper.fxml;

import com.gtohelper.datafetcher.controllers.WorkQueueController;
import com.gtohelper.domain.Work;
import com.gtohelper.utility.Popups;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;


public class FinishedWorkListViewCell extends WorkListViewCellBase {
    MenuItem compressAndArchive = new MenuItem();
    MenuItem moveToPendingWorkQueue = new MenuItem();
    SeparatorMenuItem separator = new SeparatorMenuItem();
    MenuItem delete = new MenuItem();
    MenuItem deleteAndClean = new MenuItem();


    public FinishedWorkListViewCell(WorkQueueController controller) {
        super(controller);
        initializeContextMenu();
    }

    protected void initializeContextMenu() {
        compressAndArchive.setText("Compress & archive (work in progress)");

        moveToPendingWorkQueue.setText("Move to pending queue");
        moveToPendingWorkQueue.setOnAction(event -> workController.clearErrorAndQueue(currentWork));

        delete.setText("Delete");
        delete.setOnAction(event -> {
            boolean choice = Popups.showConfirmation(String.format("Move work item %s's .gto file to the Recycle Bin? Solve CFG files will remain.", currentWork.toString()));
            if(choice)
                workController.moveWorkFileToRecycle(currentWork);
        });

        deleteAndClean.setText("Delete and clean work");
        deleteAndClean.setOnAction(event -> {
            boolean choice = Popups.showConfirmation(String.format("Move work item %s's solve folder and contents to the Recyble Bin?", currentWork.toString()));
            if(choice)
                workController.moveWorkFolderToRecycle(currentWork);
        });

        contextMenu.getItems().add(compressAndArchive);
        contextMenu.getItems().add(moveToPendingWorkQueue);
        contextMenu.getItems().add(separator);
        contextMenu.getItems().add(delete);
        contextMenu.getItems().add(deleteAndClean);
    }

    @Override
    protected void setMenuItemEnableStates(Work work) {
        if(currentWork.hasError() || currentWork.hasNextTask())
            moveToPendingWorkQueue.disableProperty().set(false);
        else
            moveToPendingWorkQueue.disableProperty().set(true);

        compressAndArchive.disableProperty().set(true);
    }
}
