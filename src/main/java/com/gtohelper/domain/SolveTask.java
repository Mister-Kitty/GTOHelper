package com.gtohelper.domain;

import java.io.Serializable;

public class SolveTask implements Serializable {
    private static final long serialVersionUID = 1L;

    /*
        The existence of SolveResults isn't sufficient to say that the work is completed, as these
          objects may represent errors, deferral of computation (pending decision), or ... something.
     */

    private boolean solveCompleted = false;
    private final HandData handData;
    private SolverOutput solverOutput;
    private HandSolverAnalysis handSolverAnalysis;

    public SolveTask(HandData h) {
        handData = h;
    }

    public boolean isSolveCompleted() { return solveCompleted; } // solveCompleted is an override state to results.success
    public boolean hasError() { return solverOutput.hasError(); }
    public HandData getHandData() { return handData; }
    public SolverOutput getSolveResults() {
        return solverOutput;
    }

    public void saveSolveResults(SolverOutput results) {
        solverOutput = results;

        if(results.success)
            solveCompleted = true;
    }
}
