package com.gtohelper.fxml;

import com.gtohelper.datafetcher.controllers.WorkQueueController;
import com.gtohelper.domain.Work;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;

public abstract class WorkListViewCellBase extends ListCell<Work> {
    WorkQueueController workController;
    ContextMenu contextMenu = new ContextMenu();
    Work thisWork;

    public WorkListViewCellBase(WorkQueueController controller) {
        super();
        workController = controller;
    }

    @Override
    public void updateItem(Work work, boolean empty)
    {
        super.updateItem(work,empty);

        if (empty || work == null) {
            setGraphic(null);
            setContextMenu(null);
            thisWork = null;
        } else {
            WorkItem data = new WorkItem(work);
            thisWork = work;
            setContextMenu(contextMenu);
            refreshMenuItemEnableStates(work);
            setGraphic(data.getRootGridPane());
        }
    }

    public abstract void refreshMenuItemEnableStates(Work work);
}