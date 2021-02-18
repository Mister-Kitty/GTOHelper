package com.gtohelper.fxml;


import com.gtohelper.domain.Work;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.io.IOException;

public class WorkItem {
    @FXML
    Text name;
    @FXML
    Text ETA;
    @FXML
    Text handsCompleted;
    @FXML
    Text handsErrored;
    @FXML
    ProgressBar progressBar;

    @FXML
    GridPane rootGridPane;

    Work work;

    public WorkItem(Work w) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/gtohelper/fxml/WorkItem.fxml"));
        fxmlLoader.setController(this);

        // The fxml elements haven't been loaded yet, so we save and fill the data in Initialize below.
        work = w;

        try
        {
            fxmlLoader.load();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void initialize() {
        updateFieldsFromWork(work);
        work.setProgressCallbackToWorkGUI(this::updateFieldsFromWork);
    }

    // we could hook our fields up to these properties directly ~ but doing so would require we declare and
    // bind fxml into the Work domain object. Instead we'll just do a callback.
    public void updateFieldsFromWork(Work w) {
        name.setText(w.getWorkSettings().getName());

        if(w.isCompleted())
            ETA.setText("Work completed");
        else
            ETA.setText("ETA (work in progress):");

        handsCompleted.setText(w.getCompletedTaskCount() + " of " + w.getTotalTasks() + " hands completed");
        handsErrored.setText(w.getErroredTaskCount() + " hands with errors");

        float progress = ((float)w.getCompletedTaskCount()) / ((float)w.getTotalTasks());
        progressBar.setProgress(progress);
    }

    public GridPane getRootGridPane() {
        return rootGridPane;
    }
}