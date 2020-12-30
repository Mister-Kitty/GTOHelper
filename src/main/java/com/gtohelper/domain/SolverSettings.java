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
/*
    // Will make a SolverSettings object if this grows to more than 2 fields
    public float percentOfPotAccuracy;
    public float getPotAccuracyInChips() {
        int potInChips = handData.getValueAsChips(handData.amt_pot_f);
        return (percentOfPotAccuracy / 100) * potInChips;
    }
*/


}
