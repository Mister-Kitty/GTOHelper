package com.gtohelper.datafetcher.models;

import com.gtohelper.database.Database;
import com.gtohelper.datamanager.ILookupDM;
import com.gtohelper.domain.*;
import com.gtohelper.pt4datamanager.PT4LookupDM;
import com.gtohelper.solver.GameTree;
import com.gtohelper.solver.ISolver;
import com.gtohelper.solver.PioSolverV1;
import com.gtohelper.solver.PioSolverV2;
import com.gtohelper.utility.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/*
    updateGUICallback.run() MUST be called after every change to finishedWork and pendingWorkQueue.
 */

public class WorkQueueModel extends Saveable {
    private QueueWorker worker;
    Consumer<Boolean> updateSolverStatusCallback;
    Consumer<SolveTask> updateStartedOnTaskCallback;
    Consumer<Work> updateWorkAsFinishedCallback;
    Runnable updateWorkGUICallback;
    Consumer<Work> updateTaskGUIForWorkCallback;

    // No indexable java.concurrent structure with .take() exists. So
    // we use the BlockingQueue and lock it for rebuilds to move item indexes ~ which are only valid if the Queue size > 2.
    private Object pendingQueueLock = new Object();
    private LinkedBlockingQueue<Work> pendingWorkQueue;

    public WorkQueueModel(SaveFileHelper saveHelper, Consumer<Boolean> solverStatusCallback, Consumer<SolveTask> startedOnTaskCallback,
                          Consumer<Work> deliverFinishedWork, Runnable updateWorkGUI, Consumer<Work> updateTaskGUIForWork) {
        super(saveHelper, "WorkQueue");
        pendingWorkQueue = new LinkedBlockingQueue<>();
        updateSolverStatusCallback = solverStatusCallback;
        updateStartedOnTaskCallback = startedOnTaskCallback;
        updateWorkAsFinishedCallback = deliverFinishedWork;
        updateWorkGUICallback = updateWorkGUI;
        updateTaskGUIForWorkCallback = updateTaskGUIForWork;
    }

    public ArrayList<Work> getPendingWorkQueue() {
        return new ArrayList(pendingWorkQueue);
    }

    public Work getCurrentWork() {
        Work result = null;
        if(worker != null)
            result = worker.getCurrent();

        return result;
    }

    @Override
    public HashMap<String, String> getDefaultValues() {
        HashMap<String, String> values = new HashMap<>();
        values.put("pendingWorkOrder", "");
        return values;
    }

    /*
        GUI manipulation code below. Gets called from controller in most cases.
     */

    public void addWorkToPendingQueue(Work work) {
        pendingWorkQueue.add(work);
        updateWorkGUICallback.run();
    }

    public void moveWorkUp(Work work) {
        synchronized (pendingQueueLock) {
            if(pendingWorkQueue.size() < 2) {
                // Race condition between button press and worker.
                assert false;
                return;
            }

            ArrayList<Work> newQueue = getPendingWorkQueue();
            int workIndex = newQueue.indexOf(work);
            Collections.swap(newQueue, workIndex, workIndex - 1);

            pendingWorkQueue.clear();
            pendingWorkQueue.addAll(newQueue);
        }
        updateWorkGUICallback.run();
    }

    public void moveWorkDown(Work work) {
        synchronized (pendingQueueLock) {
            if(pendingWorkQueue.size() < 2) {
                // Race condition between button press and worker.
                assert false;
                return;
            }

            ArrayList<Work> newQueue = getPendingWorkQueue();
            int workIndex = newQueue.indexOf(work);
            Collections.swap(newQueue, workIndex, workIndex + 1);

            pendingWorkQueue.clear();
            pendingWorkQueue.addAll(newQueue);
        }
        updateWorkGUICallback.run();
    }

    public String getHandHistory(int handId) throws SQLException {
        try (Connection con = Database.getConnection()) {

            ILookupDM lookupDM = new PT4LookupDM(con);
            return lookupDM.getsHandHistory(handId);
        }
    }

    /*

     */

    public boolean removeWorkFromPending(Work work) {
        synchronized (pendingQueueLock) {
        boolean success = pendingWorkQueue.remove(work);
            updateWorkGUICallback.run();
            return success;
        }
    }

    public boolean movePendingWorkFileToRecycle(Work work) {
        synchronized (pendingQueueLock) {
            // Race condition of someone clicking the context menu and the work moving up the queue...
            // This may actually never happen or be possible (because the context menu may close on item change), but whatever...
            if(!pendingWorkQueue.contains(work)) {
                assert false;
                return false;
            }

            boolean success = StateManager.recycleElseDeleteWorkFile(work);
            if (success)
                pendingWorkQueue.remove(work);
            else
                Popups.showWarning("A problem occured. Check the logging tab for details.");

            updateWorkGUICallback.run();
            return success;
        }
    }

    public boolean movePendingWorkFolderToRecycle(Work work) {
        synchronized (pendingQueueLock) {
            if(!pendingWorkQueue.contains(work)) {
                assert false;
                return false;
            }

            boolean success = StateManager.recycleElseDeleteWorkFolder(work);
            if(success)
                pendingWorkQueue.remove(work);
            else
                Popups.showWarning("A problem occured. Check the logging tab for details.");

            updateWorkGUICallback.run();
            return success;
        }
    }

    public void setTaskStateForWork(Work work, SolveTask task, SolveTask.SolveTaskState state) {
        /*
            This is trivial because we share object references to the underlying SolveTask instance.
            This function is being built out for the sake of modularity and the easy of future separation into separate programs.
         */
        assert work.getTasks().contains(task);

        task.setSolveState(state);
        boolean saveSuccess = StateManager.saveExistingWorkObject(work);
        if(!saveSuccess) {
            String errorString = "Disk error while trying to save the updated task's state.\n"
                    + "Check write permissions. Program can continue but state may be different upon restart.";
            Logger.log(errorString);
            Popups.showWarning(errorString);
        }

        updateTaskGUIForWorkCallback.accept(work);
        updateWorkGUICallback.run();
    }

    /*
        Worker manipulation functions
     */

    public boolean startWorker(GlobalSolverSettings solverSettings) {
        assert worker == null;

        updateSolverStatusCallback.accept(true);
        worker = new QueueWorker(solverSettings);
        if(worker.hasFatalErrorOccured()) {
            updateSolverStatusCallback.accept(false);
            worker = null;
            return false;
        }

        worker.start();
        return true;
    }

    public void stopWorker() {
        worker.stopSolver();
    }

    /*
        Only worker code below. Other utility, etc, functions should be added above.
     */

    protected class QueueWorker extends Thread {
        private ISolver solver;
        GlobalSolverSettings solverSettings;
        private volatile boolean stopRequested = false;
        private Work current;
        public Work getCurrent() { return current; }
        private boolean fatalErrorOccured = false;

        public QueueWorker(GlobalSolverSettings solveSettings) {
            solverSettings = solveSettings;
            try {
                if(solverSettings.getIsV2())
                    solver = new PioSolverV2();
                else
                    solver = new PioSolverV1();

                solver.connectAndInitToSolver(solverSettings.getSolverLocation().toString());
            } catch (IOException e) {
                String error = "Error occured launching Pio. Perhaps the executable address changed or we do not have read permission? See log for error details.";
                Logger.log(e);
                Popups.showError(error);
                fatalErrorOccured = true;
            }
        }

        public boolean hasFatalErrorOccured() {
            return fatalErrorOccured;
        }

        public void stopSolver() {
            try {
                stopRequested = true;
                solver.stop(); // Breaks from active solve
                worker.interrupt(); // Breaks from Queue wait.
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (NullPointerException ex) {
                // Possible if solver.stop() completes and worker gets scheduled, finishes (and sets worker=null),
                // and then we run. If that happens, catch and ignore this exception as we've shut down.
            }
        }

        public void run() {
            try {
                while (true) {
                    try {
                        synchronized (pendingQueueLock) {
                            current = pendingWorkQueue.take();
                        }
                        updateWorkGUICallback.run();
                        solver.waitForReady();
                        doWork(current);
                    } catch (IOException e) {
                        String ioError = "IOError occurred communicating with Pio. Perhaps the process crashed? Stopping...";
                        Logger.log(ioError);
                        Popups.showError(ioError);
                        fatalErrorOccured = true;
                        stopSolver();
                        return;
                    } catch (InterruptedException e) {
                        // stopRequest has already been set.
                        Logger.log(e); // should be removed. Here for the breakpoint
                    }

                    updateStartedOnTaskCallback.accept(null);
                    if (stopRequested) {
                        synchronized (pendingQueueLock) {
                            if(current != null) // seems possible if .take() is inturrupted.
                                pendingWorkQueue.add(current);
                        }
                        updateWorkGUICallback.run();


                        try {
                            solver.shutdown();
                            // We await no confirmation. Is that okay?
                        } catch (IOException ioException) {
                            // Ignore exception as we're shutting down anyway.
                        }

                        return;
                    } else {
                        updateWorkAsFinishedCallback.accept(current);
                        current = null;
                        updateWorkGUICallback.run();
                    }
                }
            } finally {
                updateSolverStatusCallback.accept(false);
                worker = null;
            }
        }

        // IOExceptions should only come from Pio, not file.
        private void doWork(Work work) throws IOException {
            Work.WorkSettings settings = work.getWorkSettings();
            Ranges ranges = work.getRanges();
            BettingOptions bettingOptions = work.getBettingOptions();
            RakeData rakeData = work.getRakeData();

            Path saveFolderName = solverSettings.getWorkResultsFolder(work);

            while(work.hasNextTask() && !stopRequested) {
                SolveTask currentTask = work.nextTask();
                String cfgFileName = work.getFileNameForSolve(currentTask);
                Path fullFilePath = saveFolderName.resolve(cfgFileName);

                /*
                    Do the actual solving
                 */
                SolverOutput results;
                boolean solveFileFound;
                updateStartedOnTaskCallback.accept(currentTask);
                if(currentTask.getSolveState() == SolveTask.SolveTaskState.CFG_FOUND) {
                    // Note that we allow any cfg file name as long as it starts with the handid. So we use the file name found
                    // (and stored) in the SolveState rather than the generated filename.
                    solveFileFound = true;
                    results = loadSolve(currentTask.getSolverOutput().solveFile);
                } else if(Files.exists(fullFilePath)) {
                    // Given our file check on startup, we should only this this if someone pastes a file in during runtime.
                    Logger.log(String.format("For work %s the solve results file %s already exists. Loading & computing results.", work.toString(), cfgFileName));
                    solveFileFound = true;
                    results = loadSolve(fullFilePath);
                } else {
                    solveFileFound = false;
                    results = dispatchSolve(currentTask, settings, ranges, bettingOptions, rakeData);
                }

                /*
                    Back out of saving state early if we are told to stop
                 */
                if(stopRequested) {
                    // Stopping the task should reset it's work index
                    work.resetTaskIndex();
                    return;
                }

                /*
                    Save state and dump results. workSuccess() and workFailed() will increment internal work.CurrentTask.
                 */
                currentTask.saveSolverOutput(results);
                if(results.success) {
                    if(!solveFileFound)
                        solver.dumpTree("\"" + fullFilePath.toAbsolutePath() + "\"", "no_rivers");
                    work.taskSucceeded(currentTask);
                } else {
                    work.taskFailed(currentTask);
                }

                /*
                    Save the WorkObject and continue to next task. Fail if we cannot write.
                 */
                boolean saveSuccess = StateManager.saveExistingWorkObject(work);
                if(!saveSuccess) {
                    String errorString = String.format("File read/write error while trying to update work %s's data file.\n " +
                            "Since progress can not be saved, computation on this work is being halted.", work.toString());
                    Logger.log(errorString);
                    work.setError(errorString);
                    return;
                }

                updateTaskGUIForWorkCallback.accept(current);
            }

        }

        private SolverOutput dispatchSolve(SolveTask solve, Work.WorkSettings settings, Ranges ranges, BettingOptions bettingOptions, RakeData rakeData) throws IOException {
            SolverOutput results = new SolverOutput();

            RangeData oopRange = ranges.getRangeForHand(solve.getHandData().oopPlayer);
            RangeData ipRange = ranges.getRangeForHand(solve.getHandData().ipPlayer);

            if(ipRange == null || oopRange == null) {
                String error = String.format("Could not resolve range files for handId = %d (aka board %s, %d bet pot, with aggressor seats %s)",
                        solve.getHandData().id_hand, CardResolver.getBoardString(solve.getHandData()),
                        solve.getHandData().highestPreflopBetLevel, solve.getHandData().str_aggressors_p);
                Logger.log(error);
                results.setError(error);
                return results;
            }

            HandData handData = solve.getHandData();
            int pot = handData.getValueAsChips(handData.amt_pot_f, settings.getChipsPerBB());
            float effectiveStackBlinds = handData.getIPandOOPEffective();
            int effectiveStackChips = handData.getValueAsChips(effectiveStackBlinds, settings.getChipsPerBB());

            float solveAccuracy;
            if(settings.getUsePercentPotOverBBPerHundred()) {
                solveAccuracy = (settings.getPercentOfPotAccuracy() / 100) * pot;
            } else {
                solveAccuracy = (settings.getbbPerHundredAccuracy() / 100 ) * settings.getChipsPerBB();
            }

            solver.setRange("IP", ipRange.toString());
            solver.setRange("OOP", oopRange.toString());

            solver.setBoard(CardResolver.getFlopString(handData));
            solver.setPotAndAccuracy(0, 0, pot, solveAccuracy);
            solver.setEffectiveStack(effectiveStackChips);

            final boolean flopIso = true;
            final boolean turnIso = false;
            solver.setIsomorphism(flopIso, turnIso);

            solver.clearLines();

            // Send add_lines to solver.
            GameTree tree = new GameTree();
            HandSolveSettings handSolveSettings = new HandSolveSettings(handData, pot, effectiveStackChips);

            tree.buildGameTree(bettingOptions, handSolveSettings);
            for(String leaf : tree.getAllInLeaves(bettingOptions)) {
                solver.addLine(leaf);
            }

            results.setBuildTree(solver.buildTree());
            if(results.getBuildTree().startsWith("ERROR")) {
                String error = String.format("Error building tree. Reason: \n  %s", results.getBuildTree());
                Logger.log(Logger.Channel.SOLVER, error);
                results.setError(error);
                return results;
            }

            // Rake is after tree building (in pio) for some reason.
            if(settings.getUseRake() && rakeData != null) {
                float percent = rakeData.getRakeForBB(handData.cnt_players);
                float dollarCap = rakeData.getCapForBB(handData.amt_bb, handData.cnt_players);
                float chipCap = handData.getValueAsChips(dollarCap, settings.getChipsPerBB());

                solver.setRake(percent, chipCap);
            } else {
                solver.setRake(0f, 0f);
            }

            results.setEstimateSchematicTree(solver.getEstimateSchematicTree());

            results.setShowMemory(solver.getShowMemory());

            results.setCalcResults(solver.getCalcResults());

            solver.go();

            String calcResults = solver.waitForSolve();

            if(!stopRequested)
                results.success = true;

            return results;
        }

        private SolverOutput loadSolve(Path solveFile) {
            SolverOutput results = new SolverOutput();

            try {
                // We wrap in try/catch instead of throwing the IOException because we should continue other work if the file is corrupt.
                solver.loadTree(solveFile.toString());
            } catch (IOException e) {
                String error = String.format("Input/output error while loading solve from file %s. It's likely that the file is garbage.");
                Logger.log(Logger.Channel.SOLVER, error);
                Logger.log(Logger.Channel.SOLVER, e.getMessage());
                results.setError(error);
                return results;
            }

            if(!stopRequested)
                results.success = true;

            return results;
        }
    }

}
