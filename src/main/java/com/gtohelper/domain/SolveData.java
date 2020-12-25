package com.gtohelper.domain;

public class SolveData {
    public HandData handData;
    public BettingOptions gameTree;
    SolveResults solveResults;

    public SolveData(HandData h, BettingOptions t) {
        handData = h;
        gameTree = t;
    }

    public HandData getHandData() { return handData; }

    public SolveResults getSolveResults() {
        return solveResults;
    }
    public void saveSolveResults(SolveResults results) {
        solveResults = results;
    }
}
