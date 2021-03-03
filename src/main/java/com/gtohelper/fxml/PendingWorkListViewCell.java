package com.gtohelper.fxml;

import com.gtohelper.datafetcher.controllers.WorkQueueController;
import com.gtohelper.domain.Work;
import com.gtohelper.utility.Popups;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;


public class PendingWorkListViewCell extends WorkListViewCellBase {
    MenuItem moveUp = new MenuItem();
    MenuItem moveDown = new MenuItem();
    SeparatorMenuItem separator = new SeparatorMenuItem();
    MenuItem delete = new MenuItem();
    MenuItem deleteAndClean = new MenuItem();


    public PendingWorkListViewCell(WorkQueueController controller) {
        super(controller);
        initializeContextMenu();
    }

    protected void initializeContextMenu() {
        moveUp.setText("Move up");
        moveUp.setOnAction(event -> workController.moveWorkUp(thisWork));

        moveDown.setText("Move down");
        moveDown.setOnAction(event -> workController.moveWorkDown(thisWork));

        delete.setText("Delete");
        delete.setOnAction(event -> {
            boolean choice = Popups.showConfirmation(String.format("Move work item %s's .gto file to the Recycle Bin? Solve CFG files will remain.", thisWork.toString()));
            if(choice)
                workController.moveWorkFileToRecycle(thisWork);
        });

        deleteAndClean.setText("Delete and remove folder");
        deleteAndClean.setOnAction(event -> {
            boolean choice = Popups.showConfirmation(String.format("Move work item %s's solve folder and contents to the Recyble Bin?", thisWork.toString()));
            if(choice)
                workController.moveWorkFolderToRecycle(thisWork);
        });

        contextMenu.getItems().add(moveUp);
        contextMenu.getItems().add(moveDown);
        contextMenu.getItems().add(separator);
        contextMenu.getItems().add(delete);
        contextMenu.getItems().add(deleteAndClean);
    }

    @Override
    protected void setMenuItemEnableStates(Work work) {
        if(getIndex() > 0)
            moveUp.disableProperty().set(false);
        else
            moveUp.disableProperty().set(true);

        if(getIndex() < getListView().getItems().size() - 1)
            moveDown.disableProperty().set(false);
        else
            moveDown.disableProperty().set(true);

    }
}
