package com.gtohelper.domain;

import java.io.Serializable;

public class SolverOutput implements Serializable {
    private static final long serialVersionUID = 1L;

    private String setBuildTreeAsActive;
    private String estimateSchematicTree;
    private String showMemory;
    private String calcResults;

    private String error;
    public boolean success = false;
    public transient String solveFileName;

    public SolverOutput() {}

    /*
        Field accessors
     */

    public boolean hasError() { return error != null && !error.isEmpty(); }
    public void setError(String error) { this.error = error; }
    public void setSetBuildTreeAsActive(String output) { setBuildTreeAsActive = output; }
    public String getSetBuildTreeAsActive() { return setBuildTreeAsActive; }
    public void setEstimateSchematicTree(String output) { estimateSchematicTree = output; }
    public void setShowMemory(String output) { showMemory = output; }
    public void setCalcResults(String output) { calcResults = output; }

    /*
        Utility functions
     */
}
