package com.gtohelper.fxml;

import com.gtohelper.datafetcher.controllers.WorkQueueController;
import com.gtohelper.domain.Work;
import com.gtohelper.utility.Popups;
import javafx.scene.control.MenuItem;

public class CurrentWorkListViewCell extends WorkListViewCellBase {
    MenuItem delete = new MenuItem();
    MenuItem deleteAndClean = new MenuItem();

    public CurrentWorkListViewCell(WorkQueueController controller) {
        super(controller);
        initializeContextMenu();
    }

    protected void initializeContextMenu() {
        /*
        delete.setText("Delete");
        delete.setOnAction(event -> {
            boolean choice = Popups.showConfirmation(String.format("Move work item %s's .gto file to the Recycle Bin? Solve CFG files will remain.", thisWork.toString()));
            if(choice)
                workController.moveFinishedWorkFileToRecycle(thisWork);
        });

        deleteAndClean.setText("Delete and remove folder");
        deleteAndClean.setOnAction(event -> {
            boolean choice = Popups.showConfirmation(String.format("Move work item %s's solve folder and contents to the Recyble Bin?", thisWork.toString()));
            if(choice)
                workController.moveFinishedWorkFolderToRecycle(thisWork);
        });

        contextMenu.getItems().add(delete);
        contextMenu.getItems().add(deleteAndClean);

         */
    }

    @Override
    protected void setMenuItemEnableStates(Work work) {


    }
}
