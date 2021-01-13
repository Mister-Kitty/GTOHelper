package com.gtohelper.fxml;

import com.gtohelper.domain.Work;
import javafx.scene.control.ListCell;

public class WorkListViewCell extends ListCell<Work>
{
    @Override
    public void updateItem(Work work, boolean empty)
    {
        super.updateItem(work,empty);

        if (empty || work == null) {
            setGraphic(null);
        } else {
            WorkItem data = new WorkItem(work);
            setGraphic(data.getRootGridPane());
        }
    }
}