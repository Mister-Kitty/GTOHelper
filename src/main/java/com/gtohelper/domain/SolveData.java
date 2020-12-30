package com.gtohelper.domain;

public class SolveData {
    HandData handData;
    BettingOptions bettingOptions;
    SolverSettings solverSettings;
    SolveResults solveResults;

    public SolveData(HandData h, BettingOptions b, SolverSettings s) {
        handData = h;
        bettingOptions = b;
        solverSettings = s;
    }

    public HandData getHandData() { return handData; }
    public BettingOptions getBettingOptions() { return bettingOptions; }
    public SolverSettings getSolverSettings() { return solverSettings; }
    public SolveResults getSolveResults() {
        return solveResults;
    }

    public void saveSolveResults(SolveResults results) {
        solveResults = results;
    }
}
