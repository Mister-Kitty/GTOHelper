package com.gtohelper.domain;

import java.io.Serializable;

public class SolveTask implements Serializable {
    private static final long serialVersionUID = 1L;

    /*
        The existence of SolveResults isn't sufficient to say that the work is completed, as these
          objects may represent errors, deferral of computation (pending decision), or ... something.
     */

    public enum SolveTaskState implements Serializable {
        NEW,
        ERRORED,
        COMPLETED,
        IGNORED;
    }

    private SolveTaskState solveState = SolveTaskState.NEW;
    private final HandData handData;
    private SolverOutput solverOutput;
    private HandSolverAnalysis handSolverAnalysis;

    public SolveTask(HandData h) {
        handData = h;
    }

    public SolveTaskState getSolveState() { return solveState; }
    public void setSolveState(SolveTaskState state) { solveState = state; }
    public boolean hasError() { return solverOutput.hasError(); }
    public HandData getHandData() { return handData; }
    public SolverOutput getSolveResults() {
        return solverOutput;
    }

    public void saveSolveResults(SolverOutput results) {
        solverOutput = results;

        if(results.success)
            solveState = SolveTaskState.COMPLETED;
    }
}
