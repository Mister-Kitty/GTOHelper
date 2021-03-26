package com.gtohelper.domain;

import com.gtohelper.utility.CardResolver;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class Work implements Serializable {
    private static final long serialVersionUID = 1L;
    private final ArrayList<SolveTask> tasks;
    private final Ranges ranges;
    private final BettingOptions bettingOptions;
    private final RakeData rakeData;
    private final WorkSettings workSettings;

    private String error;
    private transient Path saveFileLocation;
    private transient int currentTaskIndex = -1;

    // To be clear, the first updates the work GUI and the second updates the task GUI.
    private transient Consumer<Work> progressCallbackToWorkItemGUI;

    public static class WorkSettings implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String name;
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
        public Player getHero() { return hero; }
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
        return getNewTaskCount() + getCFGFoundTaskCount() > 0;
    }

    public SolveTask nextTask() {
        if(!hasNextTask())
            throw new IllegalStateException("No incomplete tasks found. This may be a corrupt work file.");

        // We have a nextTask if there exist any non-completed tasks. But we want to return tasks in a ring/loop.
        for(int nextTaskIndex = nextTasksIndex(currentTaskIndex); ; nextTaskIndex = nextTasksIndex(nextTaskIndex)) {

            if(tasks.get(nextTaskIndex).getSolveState() == SolveTask.SolveTaskState.NEW ||
                    tasks.get(nextTaskIndex).getSolveState() == SolveTask.SolveTaskState.CFG_FOUND) {
                currentTaskIndex = nextTaskIndex;
                return tasks.get(nextTaskIndex);
            }
        }
    }

    public void resetTaskIndex() {
        currentTaskIndex = -1;
    }

    private int nextTasksIndex(int index) {
        return (index + 1) % tasks.size();
    }
    private SolveTask getCurrentTask() {
        return tasks.get(currentTaskIndex);
    }

    /*
        Properties/functions for GUI progress/status display.
     */

    public List<SolveTask> getReadonlyTaskList() {
        return Collections.unmodifiableList(tasks);
    }

    public String getCurrentHand() {
        return CardResolver.getHandStringForPlayer(workSettings.hero, getCurrentTask().getHandData());
    }

    public String getCurrentBoard() {
        return CardResolver.getBoardString(getCurrentTask().getHandData());
    }

    public int getTotalTaskCount() {
        return tasks.size();
    }

    public int getErroredTaskCount() {
        return (int) tasks.stream().filter(t -> t.getSolveState() == SolveTask.SolveTaskState.ERRORED).count();
    }

    public int getIgnoreTaskCount() {
        return (int) tasks.stream().filter(t -> t.getSolveState() == SolveTask.SolveTaskState.SKIPPED).count();
    }

    public int getCompletedTaskCount() {
        return (int) tasks.stream().filter(t -> t.getSolveState() == SolveTask.SolveTaskState.COMPLETED).count();
    }

    public int getCFGFoundTaskCount() {
        return (int) tasks.stream().filter(t -> t.getSolveState() == SolveTask.SolveTaskState.CFG_FOUND).count();
    }

    public int getNewTaskCount() {
        return (int) tasks.stream().filter(t -> t.getSolveState() == SolveTask.SolveTaskState.NEW).count();
    }

    public void skipRemainingTasks() {
        tasks.forEach(t -> {
            if(t.getSolveState() == SolveTask.SolveTaskState.NEW || t.getSolveState() == SolveTask.SolveTaskState.CFG_FOUND)
                t.setSolveState(SolveTask.SolveTaskState.SKIPPED);
        });
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
    public String getName() { return workSettings.getName(); }

    public ArrayList<SolveTask> getTasks() {
        return tasks;
    }

    public int getTotalTasks() {
        return tasks.size();
    }

    public boolean hasError() { return error != null && !error.isEmpty(); }
    public void clearError() { error = null; }
    public void setError(String error) { this.error = error; }
    public String getError() { return error; }

    public void setSaveFileLocation(Path saveFileLocation) { this.saveFileLocation = saveFileLocation; }
    public Path getSaveFileLocation() { return saveFileLocation; }

    public void setProgressCallbackToWorkItemGUI(Consumer<Work> callback) {
        progressCallbackToWorkItemGUI = callback;
    }
    public void refreshWorkItemGUI() {
        if(progressCallbackToWorkItemGUI != null)
            progressCallbackToWorkItemGUI.accept(this);
    }

    public Work(List<SolveTask> w, WorkSettings settings, Ranges r, BettingOptions b) {
        this(w, settings, r, b, null);
    }

    public Work(List<SolveTask> w, WorkSettings settings, Ranges r, BettingOptions b, RakeData rake) {
        assert w.size() != 0;

        workSettings = settings;
        tasks = new ArrayList<>(w);
        ranges = r;
        bettingOptions = b;
        rakeData = rake;
    }

    public void taskSucceeded(SolveTask currentTask) {
        afterTaskAttemptedReported(currentTask);
    }

    public void taskFailed(SolveTask currentTask) {
        afterTaskAttemptedReported(currentTask);
    }

    // Awkward name... just call this after workSuccess() etc.
    private void afterTaskAttemptedReported(SolveTask currentTask) {
        refreshWorkItemGUI();
    }

    public String getFileNameForSolve(SolveTask solve) {
        return String.format("%d - %s - %s.cfr", solve.getHandData().id_hand,
                CardResolver.getHandStringForPlayer(workSettings.getHero(), solve.getHandData()),
                CardResolver.getBoardString(solve.getHandData()));
    }

    @Override
    public String toString() {
        return workSettings.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Work)) {
            return false;
        }

        return ((Work) obj).getWorkSettings().getName().equals(this.getWorkSettings().getName());
    }
}
