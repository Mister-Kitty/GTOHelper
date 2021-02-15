package com.gtohelper.domain;

import com.gtohelper.utility.CardResolver;
import sun.plugin.dom.exception.InvalidStateException;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Work implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<SolveTask> tasks;
    private Ranges ranges;
    private BettingOptions bettingOptions;
    private RakeData rakeData;
    private WorkSettings workSettings;

    private String error;
    private boolean isCompleted = false;
    private int currentWorkIndex = 0;

    private transient Consumer<Work> progressCallback;

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

    /*
        SolveTask navigating functions
    */

    public boolean hasNextTask() {
        return getCompletedTaskCount() <= getTotalTaskCount();
    }

    public SolveTask nextTask() {
        // We have a nextTask if there exist any non-completed tasks. But we want to return tasks in a ring/loop.
        for(int nextWorkIndex = nextTasksIndex(currentWorkIndex); nextWorkIndex != currentWorkIndex; nextWorkIndex = nextTasksIndex(currentWorkIndex)) {

            // This is a lazy, shitty way of doing this. I could refactor this later.
            if(!tasks.get(nextWorkIndex).isSolveCompleted()) {
                currentWorkIndex = nextWorkIndex;
                return tasks.get(nextWorkIndex);
            }
        }

        throw new InvalidStateException("No incomplete tasks found. This is likely a corrupt Work file, but could be a programming error.");
    }

    private int nextTasksIndex(int index) {
        return (index + 1) % tasks.size();
    }

    public SolveTask getCurrentTask() {
        return tasks.get(currentWorkIndex);
    }

    /*
        Properties/functions for GUI progress/status display.
     */

    public String getCurrentHand() {
        return CardResolver.getHandStringForPlayer(workSettings.hero, getCurrentTask().getHandData());
    }

    public String getCurrentBoard() {
        return CardResolver.getBoardString(getCurrentTask().getHandData());
    }

    public List<HandData> getHandDataList() {
        return tasks.stream().map(t -> t.getHandData()).collect(Collectors.toList());
    }

    public int getTotalTaskCount() {
        return tasks.size();
    }

    public int getCompletedTaskCount() {
        return (int) tasks.stream().filter(t -> t.isSolveCompleted()).count();
    }

    public int getTasksWithErrorCount() {
        return (int) tasks.stream().filter(t -> t.hasError()).count();
    }

    /*
        Data/Object field accessors
     */

    public WorkSettings getWorkSettings() { return workSettings; }

    public Ranges getRanges() { return ranges; }

    public BettingOptions getBettingOptions() { return bettingOptions; }

    public RakeData getRakeData() { return rakeData; }

    /*
        Other Accessor functions
     */


    public ArrayList<SolveTask> getTasks() {
        return tasks;
    }

    public int getTotalTasks() {
        return tasks.size();
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public boolean getHasError() { return error != null && !error.isEmpty(); }

    public void setError(String error) { this.error = error; }

    public void setProgressCallback(Consumer<Work> callback) {
        progressCallback = callback;
    }

    public Work(List<SolveTask> w, WorkSettings settings, Ranges r, BettingOptions b, RakeData rake) {
        assert w.size() != 0;

        workSettings = settings;
        tasks = new ArrayList<>(w);
        ranges = r;
        bettingOptions = b;
        rakeData = rake;
    }

    public void workSucceeded() {
        afterWorkAttemptedReported();
    }

    public void workFailed() {
        afterWorkAttemptedReported();
    }

    public void workSkipped() {
        afterWorkAttemptedReported();
    }

    // Awkward name... just call this after workSuccess() etc.
    private void afterWorkAttemptedReported() {
        if(!hasNextTask()) {
            isCompleted = true;
        } else {
            nextTask();
        }

        if(progressCallback != null)
            progressCallback.accept(this);
    }

    public String getFileNameForSolve(SolveTask solve) {
        return String.format("%d - %s - %s.cfr", solve.getHandData().id_hand,
                CardResolver.getHandStringForPlayer(workSettings.hero, solve.getHandData()),
                CardResolver.getBoardString(solve.getHandData()));
    }

    @Override
    public String toString() {
        return workSettings.getName();
    }
}
