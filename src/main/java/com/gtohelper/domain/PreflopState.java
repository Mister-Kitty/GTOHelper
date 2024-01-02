package com.gtohelper.domain;

import java.io.Serial;
import java.io.Serializable;

public class PreflopState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public Situation situation;
    public Seat heroSeat;
    public Seat villainSeat;
    public LastAction lastHeroAction;

    public final static String delimiter = "-";

    public PreflopState() {}

    public PreflopState(Situation s, Seat hSeat, Seat vSeat, LastAction lAction) {
        situation = s;
        heroSeat = hSeat;
        villainSeat = vSeat;
        lastHeroAction = lAction;
    }

    public PreflopState(PreflopState action) {
        situation = action.situation;
        heroSeat = action.heroSeat;
        villainSeat = action.villainSeat;
        lastHeroAction = action.lastHeroAction;
    }

    public PreflopState(HandData.PlayerHandData playerHand) {
        HandData.PlayerHandData.LastActionForStreet lastPreflopAction = playerHand.preflop;
        heroSeat = playerHand.seat;
        villainSeat = lastPreflopAction.vsSeat;
        switch (lastPreflopAction.action) {
            case BET, RAISE -> lastHeroAction = LastAction.RAISE;
            case CALL, CHECK -> lastHeroAction = LastAction.CALL;
        }

        if(lastPreflopAction.betLevel == 1) {
            situation = Situation.LIMP;
        } else if(lastPreflopAction.betLevel == 2) {
            if(lastHeroAction == LastAction.RAISE)
                situation = Situation.RFI;
            else
                situation = Situation.VRFI;
        } else if(lastPreflopAction.betLevel == 3) {
            if(lastHeroAction == LastAction.RAISE)
                situation = Situation.VRFI;
            else
                situation = Situation.V3BET;
        } else if(lastPreflopAction.betLevel == 4) {
            if(lastHeroAction == LastAction.RAISE)
                situation = Situation.V3BET;
            else
                situation = Situation.V4BET;
        } else if(lastPreflopAction.betLevel == 5) {
            if(lastHeroAction == LastAction.RAISE)
                situation = Situation.V4BET;
            else
                situation = Situation.V5BET;
        } else {
            situation = Situation.V5BET;
        }
    }

    public static int getBetLevelFromSituationAndLastAction(Situation situation, LastAction lastAction) {
        switch (situation) {
            case LIMP -> { return 1; }
            case RFI -> { return 2; }
            case VRFI -> {
                if(lastAction == LastAction.RAISE)
                    return 3;
                return 2;
            }
            case V3BET -> {
                if(lastAction == LastAction.RAISE)
                    return 4;
                return 3;
            }
            case V4BET -> {
                if(lastAction == LastAction.RAISE)
                    return 5;
                return 4;
            }
            default -> { return 5; }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;

        if (!(obj instanceof PreflopState o)) {
            return false;
        }

        return heroSeat == o.heroSeat && villainSeat == o.villainSeat &&
                situation == o.situation && lastHeroAction == o.lastHeroAction;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public String toString() {
        if(situation == Situation.LIMP)
            return limpToString();
        else if(situation == Situation.RFI)
            return rfiToString();
        else if(situation == Situation.VRFI)
            return vRfiToString();
        else if(situation == Situation.V3BET)
            return v3BetToString();
        else if(situation == Situation.V4BET)
            return v4BetToString();
        else if(situation == Situation.V5BET)
            return call5BetToString();
        else
            return "Invalid preflop state";
    }

    private String limpToString() {
        return situation.name + delimiter + heroSeat.name;
    }

    private String rfiToString() {
        return situation.name + delimiter + heroSeat.name;
    }

    private String vRfiToString() {
        return situation.name + delimiter + "v" + villainSeat.name + delimiter + heroSeat.name + delimiter + lastHeroAction;
    }

    private String v3BetToString() {
        return situation.name + delimiter + heroSeat.name + delimiter + "v" + villainSeat.name + delimiter + lastHeroAction;
    }

    private String v4BetToString() {
        return situation.name + delimiter + "v" + villainSeat.name + delimiter + heroSeat.name + delimiter + lastHeroAction;
    }

    private String call5BetToString() {
        return situation.name + delimiter + heroSeat.name + delimiter + "v" + villainSeat.name + delimiter + lastHeroAction;
    }
}
