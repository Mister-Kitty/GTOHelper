package com.gtohelper.domain;

public class SolveData {
    public HandData handData;
    public GameTreeData gameTree;
    SolveResults solveResults;

    public SolveData(HandData h, GameTreeData t) {
        handData = h;
        gameTree = t;
    }

    public SolveResults getSolveResults() {
        return solveResults;
    }

    public void saveSolveResults(SolveResults results) {
        solveResults = results;
    }
}
