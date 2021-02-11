package com.gtohelper.domain;

import com.gtohelper.utility.CardResolver;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Work implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<SolveData> workTasks;
    private Ranges ranges;
    private BettingOptions bettingOptions;
    private RakeData rakeData;
    private WorkSettings workSettings;

    private boolean isCompleted = false;
    private transient int currentWorkIndex = 0;
    transient Consumer<Work> progressCallback;

    public static class WorkSettings implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private final Player hero;
        private boolean usePercentPotOverBBPerHundred;
        private float percentOfPotAccuracy;
        private float bbPerHundredAccuracy;
        private boolean useRake;
        private String betSettingName;
        private final int chipsPerBB = 100;
        //String rakeFileName;
        //String rangeFileGroupName;

        public WorkSettings(String name, Player hero, float percentOfPotAccuracy, float bbPerHundredAccuracy, boolean useRake, String betSettingsName) {
            this.name = name;
            this.hero = hero;

            assert percentOfPotAccuracy != bbPerHundredAccuracy;
            if(percentOfPotAccuracy != 0) {
                this.percentOfPotAccuracy = percentOfPotAccuracy;
                usePercentPotOverBBPerHundred = true;
            } else {
                this.bbPerHundredAccuracy = bbPerHundredAccuracy;
                usePercentPotOverBBPerHundred = false;
            }

            this.useRake = useRake;
            this.betSettingName = betSettingsName;
        }

        public String getName() { return name; }
        public boolean getUsePercentPotOverBBPerHundred() { return usePercentPotOverBBPerHundred; }
        public float getPercentOfPotAccuracy() { return percentOfPotAccuracy; }
        public float getbbPerHundredAccuracy() { return bbPerHundredAccuracy; }
        public boolean getUseRake() { return useRake; }
        public String getBetSettingName() { return betSettingName; }
        public int getChipsPerBB() { return chipsPerBB; }

    }

    public WorkSettings getWorkSettings() { return workSettings; }

    public int getCurrentWorkIndex() {
        return currentWorkIndex;
    }

    public int getTotalWorkItems() {
        return workTasks.size();
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setProgressCallback(Consumer<Work> callback) {
        progressCallback = callback;
    }

    public SolveData getCurrentTask() {
        return workTasks.get(currentWorkIndex);
    }

    public Ranges getRanges() { return ranges; }

    public BettingOptions getBettingOptions() { return bettingOptions; }

    public RakeData getRakeData() { return rakeData; }

    public String getCurrentHand() {
        return CardResolver.getHandStringForPlayer(workSettings.hero, getCurrentTask().handData);
    }

    public String getCurrentBoard() {
        return CardResolver.getBoardString(getCurrentTask().handData);
    }

    public List<HandData> getHandDataList() {
        return workTasks.stream().map(t -> t.getHandData()).collect(Collectors.toList());
    }

    public Work(List<SolveData> w, WorkSettings settings, Ranges r, BettingOptions b, RakeData rake) {
        assert w.size() != 0;
        workSettings = settings;
        workTasks = new ArrayList<>(w);
        ranges = r;
        bettingOptions = b;
        rakeData = rake;
    }

    public void workSucceeded(SolveData currentSolve) {
        assert getCurrentTask().getSolveResults() != null;
        currentWorkIndex++;
        if(getCurrentWorkIndex() >= getTotalWorkItems())
            isCompleted = true;
        if(progressCallback != null)
            progressCallback.accept(this);
    }

    public void workFailed(SolveData currentSolve) {
 /*       assert getCurrentTask().getSolveResults() != null;
        currentWorkIndex++;
        if(getCurrentWorkIndex() >= getTotalWorkItems())
            isCompleted = true;
        if(progressCallback != null)
            progressCallback.accept(this);
            */

    }

    @Override
    public String toString() {
        return workSettings.getName();
    }
}
