package com.gtohelper.domain;

/*
    We dont' want to send HandData as a whole object to generate the game tree. But there are some of our
    application-specific data fields that we do need.
 */
public class HandSolveSettings {

    public int initialPot;
    public int initialEffectiveStack;
    public Actor lastPfAggressor;

    public HandSolveSettings() {}
    public HandSolveSettings(HandData data, int initialPot, int initialEffectiveStack) {
        this.initialPot = initialPot;
        this.initialEffectiveStack = initialEffectiveStack;
        this.lastPfAggressor = data.lastPfAggressor;

    }





}
