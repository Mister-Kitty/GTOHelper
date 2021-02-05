package com.gtohelper.domain;

import java.io.Serializable;
import java.util.ArrayList;

public class RakeData implements Serializable {
    private ArrayList<Float> bbLimits = new ArrayList<>();
    private ArrayList<Float> rakePercents = new ArrayList<>();
    private ArrayList<Float> twoPlayerCaps = new ArrayList<>();
    private ArrayList<Float> fourPlayerCaps = new ArrayList<>();
    private ArrayList<Float> fullPlayerCaps = new ArrayList<>();

    public void addRakeRow(Float bbLimit, Float rakePercent, Float twoPlayerCap, Float fourPlayerCap, Float fullPlayerCap) {
        bbLimits.add(bbLimit);
        rakePercents.add(rakePercent);
        twoPlayerCaps.add(twoPlayerCap);
        fourPlayerCaps.add(fourPlayerCap);
        fullPlayerCaps.add(fullPlayerCap);
    }

    public float getRakeForBB(float bb) {
        for(int index = 0; index < bbLimits.size() - 1; index++) {
            boolean isNextLimitOver = bbLimits.get(index + 1) > bb;
            if(isNextLimitOver) {
                return rakePercents.get(index);
            }
        }

        // If we're here, then we're using the biggest limit.
        return rakePercents.get(bbLimits.size() - 1);
    }

    public float getCapForBB(float bb, int players) {
        for(int index = 0; index < bbLimits.size() - 1; index++) {
            boolean isNextLimitOver = bbLimits.get(index + 1) > bb;
            if(isNextLimitOver) {
                return getCapFromIndex(index, players);
            }
        }

        // If we're here, then we're using the biggest limit.
        return getCapFromIndex(bbLimits.size() - 1, players);
    }

    private float getCapFromIndex(int index, int players) {
        if(players <= 2)
            return twoPlayerCaps.get(index);
        else if(players <= 4)
            return fourPlayerCaps.get(index);
        else
            return fullPlayerCaps.get(index);
    }
}
