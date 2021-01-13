package com.gtohelper.fxml;


import com.gtohelper.domain.Work;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.io.IOException;

public class WorkItem {

    @FXML
    GridPane rootGridPane;

    @FXML
    Label titleLabel;

    @FXML
    Label ETALabel;

    @FXML
    Text currentHandText;

    @FXML
    ProgressBar progressBar;

    @FXML
    Text handXofYText;

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
        work.setProgressCallback(this::updateFieldsFromWork);
    }

    // we could hook our fields up to these properties directly ~ but doing so would require we declare and
    // bind fxml into the Work domain object. Instead we'll just do a callback.
    public void updateFieldsFromWork(Work w) {
        titleLabel.setText(w.name);
        if(w.isCompleted()) {
            currentHandText.setText("Completed");
            handXofYText.setText("");
        } else {
            currentHandText.setText(w.getCurrentHand() + " - " + w.getCurrentBoard());
            handXofYText.setText("Hand " + (w.getCurrentWorkIndex() + 1) + " of " + w.getTotalWorkItems());
        }
        progressBar.setProgress(w.getCurrentWorkIndex() / w.getTotalWorkItems());
    }

    public GridPane getRootGridPane() {
        return rootGridPane;
    }
}