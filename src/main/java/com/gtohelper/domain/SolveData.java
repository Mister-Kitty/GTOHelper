package com.gtohelper.domain;

public class SolveData {
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
