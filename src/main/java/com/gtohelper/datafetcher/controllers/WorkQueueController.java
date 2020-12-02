package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.WorkQueue;
import com.gtohelper.domain.Work;
import com.gtohelper.fxml.WorkListViewCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.util.*;

public class WorkQueueController {

    WorkQueue workQueue = new WorkQueue();

    @FXML
    ListView<Work> currentWorkQueue;


    ArrayList<Work> stringSet = new ArrayList<>();
    ObservableList observableList = FXCollections.<Work>observableArrayList();

    public WorkQueueController() {

    }

    @FXML
    void initialize() {
        stringSet.add(new Work(new ArrayList<>()));
        observableList.setAll(stringSet);
        currentWorkQueue.setItems(observableList);

        currentWorkQueue.setCellFactory(new Callback<ListView<Work>, ListCell<Work>>()
        {
            @Override
            public ListCell<Work> call(ListView<Work> listView)
            {
                return new WorkListViewCell();
            }
        });
    }


   // public volatile PriorityBlockingQueue<Work> currentWorkQueue;
  //  public volatile PriorityBlockingQueue<Work> finishedWorkQueue;

   // WorkQueueController

    public void receiveNewWork(Work work) {
        workQueue.receiveNewWork(work);
    }


}
