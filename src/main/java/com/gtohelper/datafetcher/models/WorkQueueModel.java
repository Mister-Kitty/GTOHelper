package com.gtohelper.datafetcher.models;

import com.gtohelper.domain.*;
import com.gtohelper.solver.ISolver;
import com.gtohelper.solver.PioSolver;
import com.gtohelper.utility.CardResolver;

import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

public class WorkQueueModel {
    QueueWorker worker;
    final int defaultInitialCapacity = 15;
    public volatile PriorityBlockingQueue<Work> currentWorkQueue;
    public volatile PriorityBlockingQueue<Work> finishedWorkQueue;

    public WorkQueueModel() {
        currentWorkQueue = new PriorityBlockingQueue<>(defaultInitialCapacity, leastWorkToDoFirst);
    }

    public void receiveNewWork(Work work) {
        currentWorkQueue.add(work);
    }

    public void startWorker(String solverLocation) {
        assert worker == null;

        worker = new QueueWorker(solverLocation);
        worker.start();
    }

    public void stopWorker() {
        assert worker != null;

        if(worker != null) {
            worker.stopSolver();
        }
    }

    protected class QueueWorker extends Thread {
        private ISolver solver;
        private volatile boolean stopRequested = false;
        private Work current;
        public Work getCurrent() { return current; }

        public QueueWorker(String solverLocation) {
            try {
                solver = new PioSolver();
                solver.connectAndInit(solverLocation);
            } catch (IOException e) {
                // todo: log error.
                return;
            }
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
            while(true) {
                current = null;
                try {
                    current = currentWorkQueue.take();
                    solver.waitForReady();
                    doWork(current);
                } catch(IOException e) {
                    //todo: log e. Work on next item should continue
                } catch (InterruptedException e) {
                    // stopRequest has already been set.
                }

                if(stopRequested) {
                    // Save our Work progress and reinsert it back into the queue ...
                    if(current != null) {
                        currentWorkQueue.add(current);
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
        }

        private void doWork(Work work) {
            Ranges ranges = work.getRanges();

            while(!work.isCompleted() && !stopRequested) {
                SolveData currentSolve = work.getCurrentTask();
                String saveFolder = currentSolve.getSolverSettings().getSolveSaveLocation() + "\\" + work.name + "\\";
                String fileName = currentSolve.getHandData().limit_name + "-" + CardResolver.getBoardString(currentSolve.getHandData()) +
                        "-" + currentSolve.getHandData().id_hand;

                try {
                    SolveResults results = dispatchSolve(currentSolve, ranges);
                    work.getCurrentTask().saveSolveResults(results);

                    if(results.success)
                        solver.dumpTree("\"" + saveFolder + fileName + "\"", "no_rivers");

                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(!stopRequested)
                    work.workFinished();
            }
        }

        private SolveResults dispatchSolve(SolveData solve, Ranges ranges) throws IOException {
            SolveResults results = new SolveResults();

            RangeData oopRange = ranges.getRangeForHand(solve.getHandData().oopPlayer);
            RangeData ipRange = ranges.getRangeForHand(solve.getHandData().ipPlayer);

            if(ipRange == null || oopRange == null) {
                //todo log error
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
                // Todo: Log error. Likely insufficient ram.
                return results;
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
    Comparator<Work> leastWorkToDoFirst = new Comparator<Work>() {
        @Override
        public int compare(Work work1, Work work2) {
            int work1Size = Math.min(work1.overridePriority, work1.getTotalWorkItems());
            int work2Size = Math.min(work2.overridePriority, work2.getTotalWorkItems());

            return Integer.compare(work1Size, work2Size);
        }
    };
}
