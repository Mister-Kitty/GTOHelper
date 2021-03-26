package com.gtohelper.domain;

import java.io.Serializable;
import java.util.UUID;

public class SolveTask implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id = UUID.randomUUID();

    /*
        The existence of SolveResults isn't sufficient to say that the work is completed, as these
          objects may represent errors, deferral of computation (pending decision), or ... something.
     */

    public enum SolveTaskState implements Serializable {
        NEW,
        ERRORED,
        CFG_FOUND,
        COMPLETED,
        SKIPPED;
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
    public String getError() { return solverOutput.getError(); }
    public HandData getHandData() { return handData; }
    public SolverOutput getSolverOutput() {
        return solverOutput;
    }

    public void saveSolverOutput(SolverOutput results) {
        assert results.success != results.hasError();
        solverOutput = results;

        if(results.success)
            solveState = SolveTaskState.COMPLETED;
        else if (results.hasError())
            solveState = SolveTaskState.ERRORED;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof SolveTask)) {
            return false;
        }

        return ((SolveTask) obj).id.equals(this.id);
    }
}
