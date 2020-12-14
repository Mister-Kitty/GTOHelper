package com.gtohelper.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Ranges {
    // We could just use one HashMap<ActionAndSeat, RangeData>. Instead, we'll break down
    // this hashmap by position, as it'll make our use case way easier.
    public HashMap<ActionAndSeat, RangeData> RFIMap = new HashMap<>();
    public HashMap<ActionAndSeat, RangeData> vRFIMap = new HashMap<>();
    public HashMap<ActionAndSeat, RangeData> v3BetMap = new HashMap<>();
    public HashMap<ActionAndSeat, RangeData> v4BetMap = new HashMap<>();

    public void addRangeForAction(ActionAndSeat action, RangeData data) {
        switch(action.situation) {
            case RFI:
                RFIMap.put(action, data);
                break;
            case VRFI:
                vRFIMap.put(action, data);
                break;
            case V3BET:
                v3BetMap.put(action, data);
                break;
            case V4BET:
                v4BetMap.put(action, data);
                break;
            default:
                assert false;
        }
    }

    public enum Seat {
        // The 'position' of the seat is it's Enum.ordinal() value.
        BTN("BTN"), CO("CO"), HJ("HJ"),
        LJ("LJ"), UTG2("UTG+2"), UTG1("UTG+1"),
        UTG("UTG"), BB("BB"), SB("SB");

        public String name;
        Seat(String name) {
            this.name = name;
        }

        public static Seat fromString(String text) {
            for(Seat s : Seat.values()) {
                if(s.name.equals(text)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("No constant with text " + text + " found");
        }
    }

    public enum Situation {
        RFI("RFI"), VRFI("vRFI"), V3BET("v3Bet"), V4BET("v4Bet");

        public String name;
        Situation(String name) {
            this.name = name;
        }

        public static Situation fromString(String text) {
            for(Situation s : Situation.values()) {
                if(s.name.equals(text)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("No constant with text " + text + " found");
        }
    }

    public enum LastAction {
        CALL("Call"), RAISE("Raise");

        public String name;
        LastAction(String n) { this.name = n; }

        public static LastAction fromString(String text) {
            for(LastAction s : LastAction.values()) {
                if(s.name.equals(text)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("No constant with text " + text + " found");
        }
    }

    public static class ActionAndSeat {
        public Situation situation;
        public Seat heroSeat;
        public Seat villainSeat;
        public LastAction lastHeroAction;

        public final static String delimiter = "-";

        public ActionAndSeat(String string) {
            fromString(string);
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
            if(situation == Situation.RFI) {
                rfiFromString(splitStrings[index]);
            } else if(situation == Situation.VRFI) {
                // Note that these FromStrings() have Villain, then hero as their parameter.
                vRfiFromString(splitStrings[index], splitStrings[index + 1], splitStrings[index + 2]);
            } else if(situation == Situation.V3BET) {
                v3BetFromString(splitStrings[index + 1], splitStrings[index], splitStrings[index + 2]);
            } else
                assert false;

        }

        @Override
        public String toString() {
            if(situation == Situation.RFI)
                return rfiToString();
            else if(situation == Situation.VRFI)
                return vRfiToString();
            else if(situation == Situation.V3BET)
                return v3BetToString();
            else
                return "";
        }

        private void rfiFromString(String heroPosition) {
            heroSeat = Seat.fromString(heroPosition);
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
    }
}
