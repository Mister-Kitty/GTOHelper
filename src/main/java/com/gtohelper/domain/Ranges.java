package com.gtohelper.domain;

import java.util.HashMap;

public class Ranges {

    RangesMap rangesMap = new RangesMap();

    public RangeData getRangeForHand(HandData.PlayerHandData playerHand) {
        ActionAndSeat action = new ActionAndSeat(playerHand);
        return rangesMap.get(action);
    }

    public void addRangeForAction(ActionAndSeat action, RangeData data) {
        rangesMap.put(action, data);
    }

    class RangesMap extends HashMap<ActionAndSeat, RangeData> {
        // We could just use one HashMap<ActionAndSeat, RangeData>. Instead, we'll break down
        // this hashmap by position, as it'll make our use case way easier.
        HashMap<ActionAndSeat, RangeData> LimpMap = new HashMap<>();
        HashMap<ActionAndSeat, RangeData> RFIMap = new HashMap<>();
        HashMap<ActionAndSeat, RangeData> vRFIMap = new HashMap<>();
        HashMap<ActionAndSeat, RangeData> v3BetMap = new HashMap<>();
        HashMap<ActionAndSeat, RangeData> v4BetMap = new HashMap<>();

        @Override
        public RangeData put(ActionAndSeat action, RangeData data) {
            switch (action.situation) {
                case LIMP:
                    LimpMap.put(action, data);
                    break;
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
            return data;
        }

        // Only 6 max ranges are required, and even some of them aren't.
        // On top of that, the user may define a no-flat calling range, but may face
        // an opponent who does. So we get the best fit for all fetches.
        @Override
        public RangeData get(Object obj) {
            if (!(obj instanceof ActionAndSeat)) {
                return null;
            }

            ActionAndSeat action = (ActionAndSeat) obj;
            return getBestMatch(action);
        }

        private RangeData getBestMatch(ActionAndSeat action) {
            // This really sucks, but the behavior of the maps are different and need to be broken apart.
            switch (action.situation) {
                case LIMP:
                    return getBestLimpMatch(action);
                case RFI:
                    return getBestRFIMatch(action);
                case VRFI:
                    return getBestvRFIMatch(action);
                case V3BET:
                    return getBestv3BetMatch(action);
                case V4BET:
                    return getBestv4BetMatch(action);
                default:
                    assert false;
            }
            return null;
        }

        private RangeData getBestLimpMatch(ActionAndSeat a) {
            ActionAndSeat action = new ActionAndSeat(a);

            if(!LimpMap.isEmpty()) { // one of the few valid empty maps.
                RangeData getAttempt = LimpMap.get(action);
                if (getAttempt != null)
                    return getAttempt;

                // We keep going clockwise until we get a non-empty limp. We never look at earlier positions.
                while(action.heroSeat != Seat.UTG) {
                    action.heroSeat = action.heroSeat.getNextSeat();
                    getAttempt = LimpMap.get(action);
                    if (getAttempt != null)
                        return getAttempt;
                }
            }

            // If we're here, there are no valid limps. So instead we'll use the BB flat calling vRFI range.
            action.villainSeat = a.heroSeat;
            action.heroSeat = Seat.BB;
            action.situation = Situation.VRFI;
            return vRFIMap.get(action);
        }

        private RangeData getBestRFIMatch(ActionAndSeat a) {
            RangeData getAttempt = RFIMap.get(a);
            if (getAttempt != null)
                return getAttempt;

            // All 6max RFI are required, so just set it to LJ.
            ActionAndSeat action = new ActionAndSeat(a);
            action.heroSeat = Seat.LJ;
            return RFIMap.get(action);
        }

        private RangeData getBestvRFIMatch(ActionAndSeat a) {
            ActionAndSeat action = new ActionAndSeat(a);

            // Here's the first not obvious match.
            // Since people can limp/call OOP, or flat call IP.
            boolean isHeroAfterVillain = action.heroSeat.preflopPosition > action.villainSeat.preflopPosition;
            if(isHeroAfterVillain) {

                // Then we have a flat call after villain preflop.
                RangeData getAttempt = vRFIMap.get(action);
                if (getAttempt != null && !getAttempt.isTheEmptyRange) {
                    return getAttempt;
                } else if (getAttempt != null) {
                    // If we have the empty range, a flat is defined as not an option.
                    // So instead, we'll work clockwise until we find a flat. BB must have flat range, so stop there.
                    return getFirstNonEmptyCallRange(action, vRFIMap);
                }

                // Since all vRFI for 6max are required, we must be here for FR seats
                assert (a.heroSeat.isFullRingOnlySeat() || a.villainSeat.isFullRingOnlySeat());

                normalizeFullRingSeats(action);
                getAttempt = vRFIMap.get(action);
                if (getAttempt != null && !getAttempt.isTheEmptyRange) {
                    return getAttempt;
                }

                // Especially after normalizing FR to 6max, we'll have LJ v HJ/CO etc with no flat range.
                assert getAttempt != null;
                return getFirstNonEmptyCallRange(action, vRFIMap);
            } else {
                // If we're here then hero is preflop OOP vRFI and must have limp/called like an asshole.
                // Just give him the BB call vs villain RFI seat.
                action.villainSeat = action.heroSeat;
                action.heroSeat = Seat.BB;

                RangeData getAttempt = vRFIMap.get(action);
                if (getAttempt != null)
                    return getAttempt;

                // if our BB flat didn't return something, it must be because the FR seats aren't defined
                assert (a.heroSeat.isFullRingOnlySeat() || a.villainSeat.isFullRingOnlySeat());

                action.villainSeat = Seat.LJ;
                return vRFIMap.get(action);
            }
        }

        private RangeData getBestv3BetMatch(ActionAndSeat a) {
            // This gets a bit easier than vRFI, since we no longer have to care about no-flat ranges.
            ActionAndSeat action = new ActionAndSeat(a);

            boolean isHeroAfterVillain = action.heroSeat.preflopPosition > action.villainSeat.preflopPosition;
            if(isHeroAfterVillain) {
                // If we're IP, it's because we did the raising on the vRFI chart
                assert action.lastHeroAction == LastAction.RAISE;
                RangeData getAttempt = vRFIMap.get(action);
                if (getAttempt != null)
                    return getAttempt;

                assert (action.heroSeat.isFullRingOnlySeat() || action.villainSeat.isFullRingOnlySeat());

                normalizeFullRingSeats(action);

                return vRFIMap.get(action);
            } else {
                // If we're OOP, then it's because we called after RFIing
                assert action.lastHeroAction == LastAction.CALL;
                RangeData getAttempt = v3BetMap.get(action);
                if (getAttempt != null)
                    return getAttempt;

                assert (action.heroSeat.isFullRingOnlySeat() || action.villainSeat.isFullRingOnlySeat());

                normalizeFullRingSeats(action);

                return v3BetMap.get(action);
            }
        }

        private RangeData getBestv4BetMatch(ActionAndSeat a) {
            // This gets a bit easier than vRFI, since we no longer have to care about no-flat ranges.
            ActionAndSeat action = new ActionAndSeat(a);

            boolean isHeroAfterVillain = action.heroSeat.preflopPosition > action.villainSeat.preflopPosition;
            if(isHeroAfterVillain) {
                // If we're IP, it's because we called the 4bet
                assert action.lastHeroAction == LastAction.CALL;
                RangeData getAttempt = v3BetMap.get(action);
                if (getAttempt != null)
                    return getAttempt;

                assert (action.heroSeat.isFullRingOnlySeat() || action.villainSeat.isFullRingOnlySeat());

                normalizeFullRingSeats(action);

                return v3BetMap.get(action);
            } else {
                // If we're OOP, then it's because we raised the 4bet
                assert action.lastHeroAction == LastAction.RAISE;
                RangeData getAttempt = v4BetMap.get(action);
                if (getAttempt != null)
                    return getAttempt;

                assert (action.heroSeat.isFullRingOnlySeat() || action.villainSeat.isFullRingOnlySeat());

                normalizeFullRingSeats(action);

                return v4BetMap.get(action);
            }
        }

        private RangeData getFirstNonEmptyCallRange(ActionAndSeat a, HashMap<ActionAndSeat, RangeData> map) {
            ActionAndSeat action = new ActionAndSeat(a);
            while(action.heroSeat != Seat.BB) {
                RangeData getAttempt = map.get(a);
                if (getAttempt != null && !getAttempt.isTheEmptyRange)
                    return getAttempt;
                action.heroSeat = action.heroSeat.getNextSeat();
            }

            // If we reach here, we're on the BB.
            return map.get(a);
        }

        private void normalizeFullRingSeats(ActionAndSeat action) {
            boolean isHeroAfterVillain = action.heroSeat.preflopPosition < action.villainSeat.preflopPosition;

            if(action.heroSeat.isFullRingOnlySeat() && action.villainSeat.isFullRingOnlySeat()) {
                if(isHeroAfterVillain) {
                    action.heroSeat = Seat.HJ;
                    action.villainSeat = Seat.LJ;
                } else {
                    action.heroSeat = Seat.LJ;
                    action.villainSeat = Seat.HJ;
                }
            } else if(action.heroSeat.isFullRingOnlySeat()) {
                action.heroSeat = Seat.LJ;
                if(action.villainSeat == Seat.LJ)
                    action.villainSeat = Seat.HJ;
            } else if(action.villainSeat.isFullRingOnlySeat()) {
                action.villainSeat = Seat.LJ;
                if(action.heroSeat == Seat.LJ)
                    action.heroSeat = Seat.HJ;
            }
        }


    }

    public enum Seat {
        // The 'position' of the seat is it's Enum.ordinal() value.
        BTN("BTN", 7, 9), CO("CO", 6, 8), HJ("HJ", 5, 7),
        LJ("LJ", 4, 6), UTG2("UTG+2", 3, 5), UTG1("UTG+1", 2, 4),
        UTG("UTG", 1, 3), TthSeat("Tenth Seat", 0, 2),
        BB("BB", 9, 1), SB("SB", 8, 0);

        public static final Seat values[] = values();
        public String name;
        public int preflopPosition, postflopPosition; // acting order, starting at 0 and increasing.
        Seat(String name, int preflopPosition, int postflopPosition) {
            this.name = name;
            this.preflopPosition = preflopPosition;
            this.postflopPosition = postflopPosition;
        }

        public boolean isFullRingOnlySeat() {
            return ordinal() >= 4 && ordinal() <= 7;
        }
        public boolean isBlindSeat() { return name.equals("SB") || name.equals("BB"); }


        public Seat getNextSeat() {
            if(name.equals(BTN))
                return Seat.SB;
            else
                return values[ordinal() - 1];
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
        LIMP("Limp"), RFI("RFI"), VRFI("vRFI"), V3BET("v3Bet"), V4BET("v4Bet");

        public static final Situation values[] = values();
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

        public static final LastAction values[] = values();
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

        public ActionAndSeat(ActionAndSeat action) {
            situation = action.situation;
            heroSeat = action.heroSeat;
            villainSeat = action.villainSeat;
            lastHeroAction = action.lastHeroAction;
        }

        public ActionAndSeat(HandData.PlayerHandData playerHand) {
            // not sure if this should even be here, really... whatever
            heroSeat = playerHand.seat;
            villainSeat = Seat.values[playerHand.p_vsPosition];
            lastHeroAction = playerHand.last_p_action;

            if(playerHand.p_betLevel == 1) {
                situation = Situation.LIMP;
            }
            else if(playerHand.p_betLevel == 2) {
                if(lastHeroAction == LastAction.RAISE)
                    situation = Situation.RFI;
                else
                    situation = Situation.VRFI;
            } else if(playerHand.p_betLevel == 3) {
                if(lastHeroAction == LastAction.RAISE)
                    situation = Situation.VRFI;
                else
                    situation = Situation.V3BET;
            } else if(playerHand.p_betLevel == 4) {
                if(lastHeroAction == LastAction.RAISE)
                    situation = Situation.V3BET;
                else
                    situation = Situation.V4BET;
            } else if(playerHand.p_betLevel >= 5) {
                // Not sure if we support calling 5bets pre...
                situation = Situation.V4BET;
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
            } else
                assert false;

        }

        @Override
        public boolean equals(Object obj) {
            if(obj == this)
                return true;

            if (!(obj instanceof ActionAndSeat)) {
                return false;
            }

            ActionAndSeat o = (ActionAndSeat)obj;

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
    }
}




        /*
            I'm not convinced that this code should yet work by rotating clockwise, rather than just defaulting to
            Lojack if FR ranges are missing. Keep it here for now, as this may be an alternative setting.

        RangeData getMatchClockwiseFromSeat(HashMap<ActionAndSeat, RangeData> map, ActionAndSeat a) {
            // Copy the input, as we might fuck with it.
            ActionAndSeat action = new ActionAndSeat(a);

            // LIMP and RFI have villain set to BB
            while(action.villainSeat.isFullRingOnlySeat()) {
                RangeData getAttempt = map.get(action);
                if(getAttempt != null)
                    return getAttempt;

                // Looks like our fetch DNE. So shift villain (and hero, if he's adjacent) toward the button and try again.
                action.villainSeat = action.villainSeat.getNextSeat();

                if(action.heroSeat == action.villainSeat)
                    action.heroSeat = action.heroSeat.getNextSeat();
            }

            // If we're here, villain is at earliest case the LJ. Hero can still be Full ring though,
            // if hero was always FR and also earlier than villain.
            while(action.heroSeat.isFullRingOnlySeat()) {
                RangeData getAttempt = map.get(action);
                if (getAttempt != null)
                    return getAttempt;

                action.heroSeat = action.heroSeat.getNextSeat();

                if(action.villainSeat == action.heroSeat)
                    action.villainSeat = action.villainSeat.getNextSeat();
            }

            // hero and villain are now in 6max ranges
            // do one last fetch (we did not try a fetch if we triggered the WHILE loop's exit condition.
            RangeData getAttempt = map.get(action);
            if (getAttempt != null)
                return getAttempt;

            assert action.lastHeroAction != LastAction.RAISE;

            // Okay, so if we're here then it's because we didn't find a non-empty call



        }*/
