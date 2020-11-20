package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.WorkQueue;
import com.gtohelper.domain.SolveData;
import com.gtohelper.domain.Work;
import com.gtohelper.solver.ISolver;
import com.gtohelper.solver.PioSolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class WorkQueueController {

    WorkQueue workQueue = new WorkQueue();


    public void receiveNewWork(Work work) {
        workQueue.receiveNewWork(work);
    }


}
