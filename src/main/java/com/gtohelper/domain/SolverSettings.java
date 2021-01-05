package com.gtohelper.domain;

public class SolverSettings {

    float percentOfPotAccuracy;
    String solveSaveLocation = "C:\\Users\\jordan\\IdeaProjects\\piohelper\\solver results";

    public SolverSettings(float percentPotAccuracy) {
        percentOfPotAccuracy = percentPotAccuracy;
    }

    public float getAccuracyInChips(int potInChips) {
        return (percentOfPotAccuracy / 100) * potInChips;
    }

    public String getSolveSaveLocation() {
        return solveSaveLocation;
    }


}
