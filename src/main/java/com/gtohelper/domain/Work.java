package com.gtohelper.domain;

import com.gtohelper.utility.CardResolver;

import java.util.*;
import java.util.function.Consumer;

public class Work {
    ArrayList<SolveData> workTasks;
    Ranges ranges;

    public String name = "Review Hands for Dec 16";
    private int currentWorkIndex = 0;
    public int overridePriority = 9999;
    Consumer<Work> progressCallback;

    public int getCurrentWorkIndex() {
        return currentWorkIndex;
    }

    public int getTotalWorkItems() {
        return workTasks.size();
    }

    public boolean isCompleted() {
        return getCurrentWorkIndex() >= getTotalWorkItems();
    }

    public void setProgressCallback(Consumer<Work> callback) {
        progressCallback = callback;
    }

    public SolveData getCurrentTask() {
        return workTasks.get(currentWorkIndex);
    }

    public String getCurrentHand() {
        return CardResolver.getHandString(getCurrentTask().handData);
    }

    public String getCurrentBoard() {
        return CardResolver.getBoardString(getCurrentTask().handData);
    }

    public Work(List<SolveData> w, Ranges r) {
        assert w.size() != 0;
        workTasks = new ArrayList<>(w);
        ranges = r;
    }

    public void workFinished() {
        assert getCurrentTask().getSolveResults() != null;
        currentWorkIndex++;
        if(progressCallback != null)
            progressCallback.accept(this);
    }
}
