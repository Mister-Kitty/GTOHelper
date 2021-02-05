package com.gtohelper.datafetcher.models;

import com.gtohelper.domain.*;
import com.gtohelper.solver.ISolver;
import com.gtohelper.solver.PioSolver;
import com.gtohelper.utility.CardResolver;
import com.gtohelper.utility.Logger;

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
        futureWorkQueue = new PriorityBlockingQueue<Work>(defaultInitialCapacity, leastWorkToDoFirst);
        finishedWork = new PriorityBlockingQueue<Work>(defaultInitialCapacity, leastWorkToDoFirst);
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

    public boolean startWorker(String solverLocation) {
        assert worker == null;

        updateSolverStatusCallback.accept(true);
        worker = new QueueWorker(solverLocation);
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
        private volatile boolean stopRequested = false;
        private Work current;
        public Work getCurrent() { return current; }
        private boolean fatalErrorOccured = false;

        public QueueWorker(String solverLocation) {
            try {
                solver = new PioSolver();
                solver.connectAndInit(solverLocation);
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
                        Logger.log("Error occured communicating with Pio. Perhaps the process crashed? Stopping...\n" +
                                e.toString());
                        fatalErrorOccured = true;
                        stopSolver();
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
                    }
                }
            } finally {
                updateSolverStatusCallback.accept(false);
                worker = null;
            }
        }

        private void doWork(Work work) {
            Ranges ranges = work.getRanges();
            RakeData rakeData = work.getRakeData();

            while(!work.isCompleted() && !stopRequested) {
                SolveData currentSolve = work.getCurrentTask();
                String saveFolder = currentSolve.getSolverSettings().getSolveSaveLocation() + "\\" + work.name + "\\";
                String fileName = currentSolve.getHandData().id_hand + "-" + work.getCurrentHand() + "-" + work.getCurrentBoard() + ".cfr";

                try {
                   SolveResults results = dispatchSolve(currentSolve, ranges, rakeData);
                    work.getCurrentTask().saveSolveResults(results);

                    if(results.success)
                        solver.dumpTree("\"" + saveFolder + fileName + "\"", "no_rivers");

                } catch (IOException e) {
                    e.printStackTrace();
               }
/*
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
*/

                if(!stopRequested)
                    work.workFinished();
            }
        }

        private SolveResults dispatchSolve(SolveData solve, Ranges ranges, RakeData rakeData) throws IOException {
            SolveResults results = new SolveResults();

            RangeData oopRange = ranges.getRangeForHand(solve.getHandData().oopPlayer);
            RangeData ipRange = ranges.getRangeForHand(solve.getHandData().ipPlayer);

            if(ipRange == null || oopRange == null) {
                Logger.log(String.format("Could not resolve range files for handId = %d (aka board %s, %d bet pot, with aggressor seats %s)",
                        solve.getHandData().id_hand, CardResolver.getBoardString(solve.getHandData()),
                        solve.getHandData().highestPreflopBetLevel, solve.getHandData().str_aggressors_p));
                return results;
            }

            HandData handData = solve.getHandData();
            int pot = handData.getValueAsChips(handData.amt_pot_f);
            float solveAccuracy = solve.getSolverSettings().getAccuracyInChips(pot);
            boolean oopIsBiggestStack = (handData.oopPlayer.amt_before >= handData.ipPlayer.amt_before);
            float effectiveStack = oopIsBiggestStack ? handData.oopPlayer.amt_before : handData.ipPlayer.amt_before;

            solver.setRange("IP", ipRange.toString());
            solver.setRange("OOP", oopRange.toString());

            solver.setBoard(CardResolver.getFlopString(handData));
            solver.setPotAndAccuracy(0, 0, pot, solveAccuracy);
            solver.setEffectiveStack(handData.getValueAsChips(effectiveStack));

            BettingOptions bettingOptions = solve.getBettingOptions();

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

            String builtTreeResults = solver.setBuiltTreeAsActive();
            if(builtTreeResults.startsWith("ERROR")) {
                Logger.log(Logger.Channel.PIO, String.format("Error building tree. Reason: \n  %s", builtTreeResults));
                return results;
            }

            // Rake is after tree building for some reason.
            if(solve.getSolverSettings().getUseRake()) {
                float percent = rakeData.getRakeForBB(handData.cnt_players);
                float dollarCap = rakeData.getCapForBB(handData.amt_bb, handData.cnt_players);
                float chipCap = handData.getValueAsChips(dollarCap);

                solver.setRake(percent, chipCap);
            } else {
                solver.setRake(0f, 0f);
            }

            String treeSize = solver.getEstimateSchematicTree();

            String showMemory = solver.getShowMemory();

            String calc = solver.getCalcResults();

            solver.go();

            String calcResults = solver.waitForSolve();

            if(!stopRequested)
                results.success = true;

            return results;
        }
    }

    // Priority queue strategies that can be set by user.
    Comparator<Work> leastWorkToDoFirst = (work1, work2) -> {
        int work1Size = Math.min(work1.overridePriority, work1.getTotalWorkItems());
        int work2Size = Math.min(work2.overridePriority, work2.getTotalWorkItems());

        return Integer.compare(work1Size, work2Size);
    };


    /*

     */

    public void loadStateFromFile() {






    }

    public void saveStateToFile() {
        /*
            So, what do we need to save?
            Well, each Work should have it's own

         */


    }
}
