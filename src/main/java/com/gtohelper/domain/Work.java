package com.gtohelper.domain;

import com.gtohelper.utility.CardResolver;

import java.util.*;

public class Work {
    ArrayList<SolveData> workTasks;

    public String name = "Review Hands for Dec 16";
    private int currentWorkIndex = 0;
    public int overridePriority = 9999;

    public int getCurrentWorkIndex() {
        return currentWorkIndex;
    }

    public int getTotalWorkItems() {
        return workTasks.size();
    }

    //public bool

    public SolveData getCurrentTask() {
        return workTasks.get(currentWorkIndex);
    }

    public String getCurrentHand() {
        return CardResolver.getHandString(getCurrentTask().handData);
    }

    public String getCurrentBoard() {
        return CardResolver.getBoardString(getCurrentTask().handData);
    }

    public Work(List<SolveData> w) {
        workTasks = new ArrayList<>(w);
    }

    public void workFinished() {
        assert getCurrentTask().getSolveResults() != null;

        currentWorkIndex++;
    }

}
