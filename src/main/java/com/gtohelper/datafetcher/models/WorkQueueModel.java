package com.gtohelper.datafetcher.models;

import com.gtohelper.domain.*;
import com.gtohelper.solver.ISolver;
import com.gtohelper.solver.PioSolver;
import com.gtohelper.utility.CardResolver;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

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


                try {
                    dispatchSolve(currentSolve, ranges);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            /*try {
                String calcResults = solver.waitForSolve();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

                SolveResults results = new SolveResults();
                currentSolve.saveSolveResults(results);
                work.workFinished();
            }
        }

        private void dispatchSolve(SolveData solve, Ranges ranges) throws InterruptedException, IOException {

            Thread.sleep(1000);

            RangeData oopRange = ranges.getRangeForHand(solve.getHandData().oopPlayer);
            RangeData ipRange = ranges.getRangeForHand(solve.getHandData().ipPlayer);
/*
            solver.setRange("IP", ipRange.toString());
            solver.setRange("OOP", oopRange.toString());

            solver.setBoard(CardResolver.getBoardString(solve.handData));
            solver.setPotAndAccuracy(0, 0, 185, 1.628F);
            solver.setEffectiveStack(910);

            int allInThresholdPercent = 100;
            int allInOnlyIfLessThanNPercent = 500;
            final boolean forceOOPBet = false;
            final boolean forceOOPCheckIPBet = false;
            solver.setGameTreeOptions(allInThresholdPercent, allInOnlyIfLessThanNPercent, forceOOPBet, forceOOPCheckIPBet);

            final boolean flopIso = true;
            final boolean turnIso = false;
            solver.setIsomorphism(flopIso, turnIso);

            solver.setIPFlop(false, false, "52", "2.5x");
            solver.setOOPFlop(false, "52", "2.5x");

            solver.setIPTurn(false, false, "52", "3x");
            solver.setOOPTurn(false, "52", "3x", "");

            solver.setIPRiver(false, false, "52", "3x");
            solver.setOOPRiver(false, "52", "3x", "");

            solver.clearLines();
            solver.buildTree();


            solver.setBuiltTreeAsActive();

            String treeSize = solver.getEstimateSchematicTree();

            String showMemory = solver.getShowMemory();

            String calc = solver.getCalcResults();

            solver.go();

*/
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
