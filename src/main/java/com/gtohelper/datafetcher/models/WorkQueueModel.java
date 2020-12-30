package com.gtohelper.datafetcher.models;

import com.gtohelper.domain.*;
import com.gtohelper.solver.ISolver;
import com.gtohelper.solver.PioSolver;
import com.gtohelper.utility.CardResolver;

import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

public class WorkQueueModel {
    QueueWorker worker = new QueueWorker();
    final int defaultInitialCapacity = 15;
    public volatile PriorityBlockingQueue<Work> currentWorkQueue;
    public volatile PriorityBlockingQueue<Work> finishedWorkQueue;

    public WorkQueueModel() {
        currentWorkQueue = new PriorityBlockingQueue<>(defaultInitialCapacity, leastWorkToDoFirst);
        worker.start();
    }

    public void receiveNewWork(Work work) {
        // This call must come before the work is added.
        boolean isAPriority = isThisNewWorkAPriority(work);

        currentWorkQueue.add(work);

        if(isAPriority)
            worker.interrupt();
    }

    private boolean isThisNewWorkAPriority(Work work) {
        if(worker.getCurrent() == null)
            return false;

        if(currentWorkQueue.comparator().compare(work, worker.getCurrent()) < 0)
            return true;

        return false;
    }

    protected class QueueWorker extends Thread {
        public QueueWorker() {}

        ISolver solver = new PioSolver();

        Work current = null;
        public Work getCurrent() {
            return current;
        }

        public void run() {
            try {
                solver.connectAndInit("C:\\PioSolver Edge\\PioSOLVER-edge.exe");
            } catch (IOException e) {
                // todo: log error. Also, try a relaunch of this thread if the solver path is updated.
                return;
            }

            while(true) {
                current = null;
                try {
                    current = currentWorkQueue.take();
                    solver.waitForReady();
                    doWork(current);
                } catch(IOException e) {
                    //todo: log e.
                    return;
                } catch (InterruptedException e) {
                    // Our work was interrupted by a new task of higher priority.
                    // send clear_state commands to the solver & abort it's current work.
                    try {
                        solver.waitForReady();
                    } catch (InterruptedException | IOException ex) {
                        ex.printStackTrace();
                        // critical problem. todo: kill ourselves and restart after logging
                    }

                    // Save our Work progress and reinsert it back into the queue ...
                    if(current != null) {
                        currentWorkQueue.add(current);
                    }

                    // and when we continue, we break out and start working on the new item.
                    continue;
                }
            }

            // solver.disconnect();
        }

        private void doWork(Work work) throws InterruptedException {
            Ranges ranges = work.getRanges();


            while(!work.isCompleted()) {
                SolveData currentSolve = work.getCurrentTask();
                String saveFolder = currentSolve.getSolverSettings().getSolveSaveLocation() + "\\" + work.name + "\\";
                String fileName = currentSolve.getHandData().limit_name + "-" + CardResolver.getBoardString(currentSolve.getHandData()) +
                        "-" + currentSolve.getHandData().id_hand;

                try {
                    dispatchSolve(currentSolve, ranges);
                    solver.dumpTree("\"" + saveFolder + fileName + "\"", "no_rivers");

                } catch (IOException e) {
                    e.printStackTrace();
                }

                SolveResults results = new SolveResults();
                currentSolve.saveSolveResults(results);
                work.workFinished();
            }
        }

        private void dispatchSolve(SolveData solve, Ranges ranges) throws InterruptedException, IOException {
            RangeData oopRange = ranges.getRangeForHand(solve.getHandData().oopPlayer);
            RangeData ipRange = ranges.getRangeForHand(solve.getHandData().ipPlayer);

            if(ipRange == null || oopRange == null) {
                //todo log error
                return;
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

            solver.setBuiltTreeAsActive();

            String treeSize = solver.getEstimateSchematicTree();

            String showMemory = solver.getShowMemory();

            String calc = solver.getCalcResults();

            solver.go();

            String calcResults = solver.waitForSolve();
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
