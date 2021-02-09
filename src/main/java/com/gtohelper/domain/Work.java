package com.gtohelper.domain;

import com.gtohelper.utility.CardResolver;
import javafx.concurrent.WorkerStateEvent;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Work implements Serializable {
    private ArrayList<SolveData> workTasks;
    private Ranges ranges;
    private BettingOptions bettingOptions;
    private RakeData rakeData;
    private WorkSettings workSettings;

    private int currentWorkIndex = 0;
    public int overridePriority = 9999;
    transient Consumer<Work> progressCallback;

    public static class WorkSettings implements Serializable {
        private String name;
        private final Player hero;
        private float percentOfPotAccuracy;
        private boolean useRake;
        private String betSettingName;
        //String rakeFileName;
        //String rangeFileGroupName;

        public WorkSettings(String name, Player hero, float percentOfPotAccuracy, boolean rake, String betSettingsName) {
            this.name = name;
            this.hero = hero;
            this.percentOfPotAccuracy = percentOfPotAccuracy;
            this.useRake = rake;
            this.betSettingName = betSettingsName;
        }

        public String getName() { return name; }
        public float getPercentOfPotAccuracy() { return percentOfPotAccuracy; }
        public boolean getUseRake() { return useRake; }
        public String getBetSettingName() { return betSettingName; }
    }

    public WorkSettings getWorkSettings() { return workSettings; }

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

    public void workFinished() {
        assert getCurrentTask().getSolveResults() != null;
        currentWorkIndex++;
        if(progressCallback != null)
            progressCallback.accept(this);
    }

    @Override
    public String toString() {
        return workSettings.getName();
    }
}
