package com.gtohelper.domain;

public class GlobalSolverSettings {
    String solverLocation, viewerLocation, rakeLocation, solveResultsFolder, solveResultsBackupFolder;

    public String getSolverLocation() {
        return solverLocation;
    }

    public void setSolverLocation(String solverLocation) {
        this.solverLocation = solverLocation;
    }

    public String getViewerLocation() {
        return viewerLocation;
    }

    public void setViewerLocation(String viewerLocation) {
        this.viewerLocation = viewerLocation;
    }

    public String getRakeLocation() {
        return rakeLocation;
    }

    public void setRakeLocation(String rakeLocation) {
        this.rakeLocation = rakeLocation;
    }

    public String getSolveResultsFolder() {
        return solveResultsFolder;
    }

    public void setSolveResultsFolder(String solveResultsFolder) {
        this.solveResultsFolder = solveResultsFolder;
    }

    public String getSolveResultsBackupFolder() {
        return solveResultsBackupFolder;
    }

    public void setSolveResultsBackupFolder(String solveResultsBackupFolder) {
        this.solveResultsBackupFolder = solveResultsBackupFolder;
    }

    public String getWorkResultsFolder(Work work) {
        return solveResultsFolder + "\\" + work.getWorkSettings().getName()+ "\\";
    }
}
