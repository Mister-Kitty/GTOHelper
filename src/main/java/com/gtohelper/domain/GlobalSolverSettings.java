package com.gtohelper.domain;

import java.nio.file.Path;

public class GlobalSolverSettings {
    Boolean isV2 = false;
    Path solverLocation, viewerLocation, rakeLocation, solverResultsFolder, solverResultsArchiveFolder;

    public Boolean getIsV2() {
        return isV2;
    }

    public void setIsV2(Boolean v2) {
        isV2 = v2;
    }

    /*
        File locations below
     */

    public Path getSolverLocation() {
        return solverLocation;
    }

    public void setSolverLocation(Path solverLocation) {
        this.solverLocation = solverLocation;
    }

    public Path getViewerLocation() {
        return viewerLocation;
    }

    public void setViewerLocation(Path viewerLocation) {
        this.viewerLocation = viewerLocation;
    }

    public Path getRakeLocation() {
        return rakeLocation;
    }

    public void setRakeLocation(Path rakeLocation) {
        this.rakeLocation = rakeLocation;
    }

    /*
        Folder locations below
     */

    public Path getSolverResultsFolder() {
        return solverResultsFolder;
    }

    public void setSolverResultsFolder(Path solverResultsFolder) {
        this.solverResultsFolder = solverResultsFolder;
    }

    public Path getSolverResultsArchiveFolder() {
        return solverResultsArchiveFolder;
    }

    public void setSolverResultsArchiveFolder(Path solverResultsArchiveFolder) {
        this.solverResultsArchiveFolder = solverResultsArchiveFolder;
    }

    public Path getWorkResultsFolder(Work work) {
        return solverResultsFolder.resolve(work.getWorkSettings().getName());
    }
}
