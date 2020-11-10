package com.gtohelper.datafetcher.controllers;

import com.gtohelper.domain.HandData;

import java.util.ArrayList;
import java.util.List;

public class WorkQueueController {

    ArrayList<HandData> currentWork;
    public void receiveNewWork(List<HandData> newWork) {
        currentWork = new ArrayList<HandData>(newWork);
    }



}
