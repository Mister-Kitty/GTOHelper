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


        //updateFieldsFromWork(work);
    }

    private void updateFieldsFromWork(Work w) {
        titleLabel.setText(w.name);
        //ETALabel.setText()
        currentHandText.setText(w.getCurrentHand() + " - " + w.getCurrentBoard());
        progressBar.setProgress(w.getCurrentWorkIndex() + w.getTotalWorkItems());
        handXofYText.setText("Hand " + w.getCurrentWorkIndex() + " of " + w.getTotalWorkItems());
    }

    public GridPane getRootGridPane() {
        return rootGridPane;
    }

}