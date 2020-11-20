package com.gtohelper.datafetcher.models;

import com.gtohelper.domain.SolveData;
import com.gtohelper.domain.Work;
import com.gtohelper.solver.ISolver;
import com.gtohelper.solver.PioSolver;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

public class WorkQueue {
    Thread worker = new QueueWorker();
    PriorityBlockingQueue<Work> currentWorkQueue;

    public WorkQueue() {
        currentWorkQueue = new PriorityBlockingQueue<Work>();
        worker.start();
    }

    public void receiveNewWork(Work work) {
        currentWorkQueue.add(work);
    }

    private class QueueWorker extends Thread {

        ISolver solver = new PioSolver();
        public QueueWorker() {}

        public void run(){

            try {
                solver.connectAndInit("C:\\PioSolver Edge\\PioSOLVER-edge.exe");
            } catch (IOException e) {
                // todo: log error.
                return;
            }

            while(true) {
                Work current = currentWorkQueue.poll();
                doWork(current);
            }

        }

        private void doWork(Work work) {
            for(SolveData solve : work.getWorkList()) {
                dispatchSolve(solve);

            }



        }

        private void dispatchSolve(SolveData solve) {
            /*
                solver.setRange("IP", IPRange1);
                solver.setRange("OOP", OOPRange1);

                    solver.setBoard(test1Board);
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
        String calcResults = solver.waitForSolve();
             */

        }
    }


}
