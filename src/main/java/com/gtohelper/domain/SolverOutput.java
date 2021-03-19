package com.gtohelper.domain;

import java.io.Serializable;
import java.nio.file.Path;

public class SolverOutput implements Serializable {
    private static final long serialVersionUID = 1L;

    private String buildTree;
    private String estimateSchematicTree;
    private String showMemory;
    private String calcResults;

    private String error;
    public boolean success = false;
    public transient Path solveFile;

    public SolverOutput() {}

    /*
        Field accessors
     */

    public boolean hasError() { return error != null && !error.isEmpty(); }
    public void setError(String error) { this.error = error; }
    public void setBuildTree(String output) { buildTree = output; }
    public String getBuildTree() { return buildTree; }
    public void setEstimateSchematicTree(String output) { estimateSchematicTree = output; }
    public void setShowMemory(String output) { showMemory = output; }
    public void setCalcResults(String output) { calcResults = output; }

    /*
        Utility functions
     */
}
