package com.gtohelper.domain;

import java.io.Serializable;

public class SolveData implements Serializable {
    private static final long serialVersionUID = 1L;
    HandData handData;
    SolveResults solveResults;

    public SolveData(HandData h) {
        handData = h;
    }

    public HandData getHandData() { return handData; }
    public SolveResults getSolveResults() {
        return solveResults;
    }

    public void saveSolveResults(SolveResults results) {
        solveResults = results;
    }
}
