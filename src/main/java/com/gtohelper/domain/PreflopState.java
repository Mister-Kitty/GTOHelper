package com.gtohelper.domain;

import java.io.Serializable;

public class PreflopState implements Serializable {
    private static final long serialVersionUID = 1L;
    public Situation situation;
    public Seat heroSeat;
    public Seat villainSeat;
    public LastAction lastHeroAction;

    public final static String delimiter = "-";

    public PreflopState(String string) {
        fromString(string);
    }

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
        switch(lastPreflopAction.action) {
            case BET:
            case RAISE:
                lastHeroAction = LastAction.RAISE;
                break;
            case CALL:
            case CHECK:
                lastHeroAction = LastAction.CALL;
                break;
            default:
                assert false;
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
                situation = Situation.CALL5BET;
        } else {
            assert(lastPreflopAction.betLevel == 5);
            situation = Situation.CALL5BET;
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

    private void fromString(String string) {
        String[] splitStrings = string.split(delimiter);

        // peel off the first segments until we find the leading 'situation'
        int index = 0;
        for(; index < splitStrings.length && situation == null; index++) {
            String split = splitStrings[index];
            // Check to see if it's a defined Situation
            for(Situation s : Situation.values()) {
                if(s.name.equals(split)) {
                    situation = s;
                    break;
                }
            }
        }

        assert index < splitStrings.length;

        // index is now pointing at the split _after_ we find the 'situation'.
        // This is because index++ is called before the break condition.
        if(situation == Situation.LIMP) {
            limpFromString(splitStrings[index]);
        } else if(situation == Situation.RFI) {
            rfiFromString(splitStrings[index]);
        } else if(situation == Situation.VRFI) {
            // Note that these FromStrings() have Villain, then hero as their parameter.
            vRfiFromString(splitStrings[index], splitStrings[index + 1], splitStrings[index + 2]);
        } else if(situation == Situation.V3BET) {
            v3BetFromString(splitStrings[index + 1], splitStrings[index], splitStrings[index + 2]);
        } else if(situation == Situation.V4BET) {
            v4BetFromString(splitStrings[index], splitStrings[index + 1], splitStrings[index + 2]);
        } else if(situation == Situation.CALL5BET) {
            call5BetFromString(splitStrings[index + 1], splitStrings[index], splitStrings[index + 2]);
        } else {
            // todo: log error.
            assert false;
        }

    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;

        if (!(obj instanceof PreflopState)) {
            return false;
        }

        PreflopState o = (PreflopState)obj;

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
        else if(situation == Situation.CALL5BET)
            return call5BetToString();
        else
            return "";
    }

    private void limpFromString(String heroPosition) {
        heroSeat = Seat.fromString(heroPosition);
        villainSeat = Seat.BB;
        lastHeroAction = LastAction.CALL;
    }

    private String limpToString() {
        return situation.name + delimiter + heroSeat.name;
    }

    private void rfiFromString(String heroPosition) {
        heroSeat = Seat.fromString(heroPosition);
        villainSeat = Seat.BB;
        lastHeroAction = LastAction.RAISE;
    }

    private String rfiToString() {
        return situation.name + delimiter + heroSeat.name;
    }

    private void vRfiFromString(String villainPosition, String heroPosition, String lastAction) {
        heroSeat = Seat.fromString(heroPosition);
        villainSeat = Seat.fromString(villainPosition.substring(1)); // remove the starting "v"
        lastHeroAction = LastAction.fromString(lastAction);
    }

    private String vRfiToString() {
        return situation.name + delimiter + "v" + villainSeat.name + delimiter + heroSeat.name + delimiter + lastHeroAction;
    }

    private void v3BetFromString(String villainPosition, String heroPosition, String lastAction) {
        heroSeat = Seat.fromString(heroPosition);
        villainSeat = Seat.fromString(villainPosition.substring(1)); // remove the starting "v"
        lastHeroAction = LastAction.fromString(lastAction);
    }

    private String v3BetToString() {
        return situation.name + delimiter + heroSeat.name + delimiter + "v" + villainSeat.name + delimiter + lastHeroAction;
    }

    private void v4BetFromString(String villainPosition, String heroPosition, String lastAction) {
        heroSeat = Seat.fromString(heroPosition);
        villainSeat = Seat.fromString(villainPosition.substring(1)); // remove the starting "v"
        lastHeroAction = LastAction.fromString(lastAction);
    }

    private String v4BetToString() {
        return situation.name + delimiter + "v" + villainSeat.name + delimiter + heroSeat.name + delimiter + lastHeroAction;
    }

    private void call5BetFromString(String villainPosition, String heroPosition, String lastAction) {
        heroSeat = Seat.fromString(heroPosition);
        villainSeat = Seat.fromString(villainPosition.substring(1)); // remove the starting "v"
        lastHeroAction = LastAction.fromString(lastAction);
    }

    private String call5BetToString() {
        return situation.name + delimiter + heroSeat.name + delimiter + "v" + villainSeat.name + delimiter + lastHeroAction;
    }
}
