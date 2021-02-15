package com.gtohelper.datafetcher.models;

import com.gtohelper.domain.*;
import com.gtohelper.solver.ISolver;
import com.gtohelper.solver.PioSolver;
import com.gtohelper.utility.CardResolver;
import com.gtohelper.utility.Logger;
import com.gtohelper.utility.Popups;
import com.gtohelper.utility.StateManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;

/*
    updateGUICallback.run() MUST be called after every change to finishedWork and futureWorkQueue.
 */

public class WorkQueueModel {
    private QueueWorker worker;
    final int defaultInitialCapacity = 15;
    Consumer<Boolean> updateSolverStatusCallback;
    Runnable updateGUICallback;
    private PriorityBlockingQueue<Work> finishedWork;
    private PriorityBlockingQueue<Work> futureWorkQueue;

    public WorkQueueModel(Consumer<Boolean> solverStatusCallback, Runnable updateGUI) {
        futureWorkQueue = new PriorityBlockingQueue<>(defaultInitialCapacity, leastWorkToDoFirst);
        finishedWork = new PriorityBlockingQueue<>(defaultInitialCapacity, leastWorkToDoFirst);
        updateSolverStatusCallback = solverStatusCallback;
        updateGUICallback = updateGUI;
    }

    public ArrayList<Work> getFinishedWork() {
        return new ArrayList(finishedWork);
    }

    public ArrayList<Work> getFutureWorkQueue() {
        return new ArrayList(futureWorkQueue);
    }

    public Work getCurrentWork() {
        Work result = null;
        if(worker != null)
            result = worker.getCurrent();

        return result;
    }

    public void receiveNewWork(Work work) {
        futureWorkQueue.add(work);
        updateGUICallback.run();
    }

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
                solver = new PioSolver();
                solver.connectAndInit(solverSettings.getSolverLocation());
            } catch (IOException e) {
                Logger.log(Logger.Channel.PIO, "Error occured launching Pio. Perhaps the executable address changed or we do not have read permission?\n" +
                        e.toString());
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
                    current = null;
                    try {
                        current = futureWorkQueue.take();
                        updateGUICallback.run();
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
                    }

                    if (stopRequested) {
                        // Save our Work progress and reinsert it back into the queue ...
                        if (current != null && !current.isCompleted()) {
                            futureWorkQueue.add(current);
                            updateGUICallback.run();
                        }

                        try {
                            solver.shutdown();
                            // We await no confirmation. Is that okay?
                        } catch (IOException ioException) {
                            // Ignore exception as we're shutting down anyway.
                        }

                        return;
                    } else {
                        finishedWork.add(current);
                        updateGUICallback.run();
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

            String saveFolderName = solverSettings.getWorkResultsFolder(work);
            File saveFolder = new File(saveFolderName);

            while(!work.isCompleted() && !stopRequested) {
                // workSuccess() & workfailed() increment the internal Work.CurrentTask. As such, ONLY use currentSolve. Do not call getCurrentTask again!
                SolveTask currentTask = work.getCurrentTask();
                String fileName = work.getFileNameForSolve(currentTask);

                /*
                    Preliminary valid state checks.
                 */
                if(new File(saveFolder, fileName).exists()) {
                    // Because we check for new save files upon startup, this should only happen if someone pastes something in at runtime.
                    Logger.log(String.format("For work %s the solve results file %s already exists. Skipping.", work.toString(), fileName));
                    // More to do here -> getSolveResults().foundNewSolveFile();
                    work.workSkipped();
                    continue;
                }

                /*
                    Do the actual solving
                 */
                SolverOutput results = dispatchSolve(currentTask, settings, ranges, bettingOptions, rakeData);

                /*
                    Back out of saving state early if we are told to stop
                 */
                if(stopRequested)
                    return;

                /*
                    Save state and dump results. workSuccess() and workFailed() will increment internal work.CurrentTask.
                 */
                currentTask.saveSolveResults(results);
                if(results.success) {
                    solver.dumpTree("\"" + saveFolderName + fileName + "\"", "no_rivers");
                    work.workSucceeded();
                } else {
                    work.workFailed();
                }

                /*
                    Save the WorkObject and continue to next task. Fail if we cannot write.
                 */
                boolean saveSuccess = StateManager.saveExistingWorkObject(work, saveFolder);
                if(!saveSuccess) {
                    String errorString = String.format("File read/write error while trying to update work %s's data file.\n " +
                            "Since progress can not be saved, computation on this work is being halted.", work.toString());
                    Logger.log(errorString);
                    work.setError(errorString);
                    return;
                }
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
            float effectiveStack = handData.getIPandOOPEffective();

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
            solver.setEffectiveStack(handData.getValueAsChips(effectiveStack, settings.getChipsPerBB()));

            int allInThresholdPercent = 100;
            int allInOnlyIfLessThanNPercent = 500;
            final boolean forceOOPBet = false;
            final boolean forceOOPCheckIPBet = false;
            solver.setGameTreeOptions(allInThresholdPercent, allInOnlyIfLessThanNPercent, forceOOPBet, forceOOPCheckIPBet);

            final boolean flopIso = true;
            final boolean turnIso = false;
            solver.setIsomorphism(flopIso, turnIso);

            solver.setIPFlop(bettingOptions.IPFlop.getCanAllIn(), !bettingOptions.IPFlop.getCan3Bet(),
                    bettingOptions.IPFlop.getBets().getInitialString(), bettingOptions.IPFlop.getRaises().getInitialString());
            solver.setOOPFlop(bettingOptions.OOPFlop.getCanAllIn(), bettingOptions.OOPFlop.getBets().getInitialString(),
                    bettingOptions.OOPFlop.getDonks().getInitialString(), bettingOptions.OOPFlop.getRaises().getInitialString());

            solver.setIPTurn(bettingOptions.IPTurn.getCanAllIn(), !bettingOptions.IPTurn.getCan3Bet(),
                    bettingOptions.IPTurn.getBets().getInitialString(), bettingOptions.IPTurn.getRaises().getInitialString());
            solver.setOOPTurn(bettingOptions.OOPTurn.getCanAllIn(), bettingOptions.OOPTurn.getBets().getInitialString(),
                    bettingOptions.OOPTurn.getDonks().getInitialString(), bettingOptions.OOPTurn.getRaises().getInitialString());

            solver.setIPRiver(bettingOptions.IPRiver.getCanAllIn(), !bettingOptions.IPRiver.getCan3Bet(),
                    bettingOptions.IPRiver.getBets().getInitialString(), bettingOptions.IPRiver.getRaises().getInitialString());
            solver.setOOPRiver(bettingOptions.OOPRiver.getCanAllIn(), bettingOptions.OOPRiver.getBets().getInitialString(),
                    bettingOptions.OOPRiver.getDonks().getInitialString(), bettingOptions.OOPRiver.getRaises().getInitialString());

            solver.clearLines();
            solver.buildTree();

            results.setSetBuildTreeAsActive(solver.setBuiltTreeAsActive());
            if(results.getSetBuildTreeAsActive().startsWith("ERROR")) {
                String error = String.format("Error building tree. Reason: \n  %s", results.getSetBuildTreeAsActive());
                Logger.log(Logger.Channel.PIO, error);
                results.setError(error);
                return results;
            }

            // Rake is after tree building for some reason.
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
    }

    // Priority queue strategies that can be set by user.
    Comparator<Work> leastWorkToDoFirst = Comparator.comparingInt(Work::getTotalTaskCount);
}
