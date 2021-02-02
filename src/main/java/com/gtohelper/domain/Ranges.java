package com.gtohelper.domain;

import java.util.HashMap;
import com.gtohelper.domain.HandData.PlayerHandData;
import com.gtohelper.domain.HandData.PlayerHandData.LastActionForStreet;

public class Ranges {

    RangesMap rangesMap = new RangesMap();

    public RangeData getRangeForHand(PlayerHandData playerHand) {
        ActionAndSeat action = new ActionAndSeat(playerHand);
        return rangesMap.get(action);
    }

    public void addRangeForAction(ActionAndSeat action, RangeData data) {
        rangesMap.put(action, data);
    }

    public void fillEmptyRanges() {
        rangesMap.fillEmptyRanges();
    }

    class RangesMap extends HashMap<ActionAndSeat, RangeData> {
        // We could just use one HashMap<ActionAndSeat, RangeData>. Instead, we'll break down
        // this hashmap by position, as it'll make our use case way easier.
        HashMap<ActionAndSeat, RangeData> LimpMap = new HashMap<>();
        HashMap<ActionAndSeat, RangeData> RFIMap = new HashMap<>();
        HashMap<ActionAndSeat, RangeData> vRFIMap = new HashMap<>();
        HashMap<ActionAndSeat, RangeData> v3BetMap = new HashMap<>();
        HashMap<ActionAndSeat, RangeData> v4BetMap = new HashMap<>();
        HashMap<ActionAndSeat, RangeData> call5BetMap = new HashMap<>();

        public void fillEmptyRanges() {
            if(!LimpMap.isEmpty())
                fillNoVillainMap(LimpMap, Situation.LIMP, LastAction.CALL);

            fillNoVillainMap(RFIMap, Situation.RFI, LastAction.RAISE);

            fillOOPVillainMap(vRFIMap, Situation.VRFI, LastAction.CALL);
            fillOOPVillainMap(vRFIMap, Situation.VRFI, LastAction.RAISE);

            fillIPVillainMap(v3BetMap, Situation.V3BET, LastAction.CALL);
            fillIPVillainMap(v3BetMap, Situation.V3BET, LastAction.RAISE);

            fillOOPVillainMap(v4BetMap, Situation.V4BET, LastAction.CALL);
            fillOOPVillainMap(v4BetMap, Situation.V4BET, LastAction.RAISE);

            if(!call5BetMap.isEmpty())
                fillIPVillainMap(call5BetMap, Situation.CALL5BET, LastAction.CALL);
        }

        private void fillNoVillainMap(HashMap<ActionAndSeat, RangeData> map, Situation sit, LastAction lastAction) {
            ActionAndSeat action = new ActionAndSeat();
            action.lastHeroAction = lastAction;
            action.situation = sit;
            action.villainSeat = Seat.BB;

            RangeData lastRangeData = null;
            for(int heroIndex = 0; heroIndex < Seat.preflopPositionsDESC.length; heroIndex++) {
                action.heroSeat = Seat.preflopPositionsDESC[heroIndex];

                RangeData data = map.get(action);
                if (data == null || data.isTheEmptyRange)
                    map.put(new ActionAndSeat(action), lastRangeData);
                else
                    lastRangeData = data;
            }
        }

        // by oopVillain, we mean preflop oop.
        private void fillOOPVillainMap(HashMap<ActionAndSeat, RangeData> map, Situation sit, LastAction lastAction) {
            ActionAndSeat action = new ActionAndSeat();
            action.situation = sit;
            action.lastHeroAction = lastAction;

            // This is outside of both loops on purpose. If we're only given 6max ranges, we want HJvLJ to propogate to FR seats.
            RangeData lastRangeData = null;
            for(int heroIndex = 0; heroIndex < Seat.preflopPositionsDESC.length - 1; heroIndex++) {
                action.heroSeat = Seat.preflopPositionsDESC[heroIndex];

                for(int villainIndex = heroIndex + 1; villainIndex < Seat.preflopPositionsDESC.length; villainIndex++) {
                    action.villainSeat = Seat.preflopPositionsDESC[villainIndex];

                    RangeData data = map.get(action);
                    if (data == null || data.isTheEmptyRange)
                        map.put(new ActionAndSeat(action), lastRangeData);
                    else
                        lastRangeData = data;
                }
            }
        }

        // by ipVillain, we mean preflop ip.
        private void fillIPVillainMap(HashMap<ActionAndSeat, RangeData> map, Situation sit, LastAction lastAction) {
            ActionAndSeat action = new ActionAndSeat();
            action.situation = sit;
            action.lastHeroAction = lastAction;

            for(int heroIndex = 1; heroIndex < Seat.preflopPositionsDESC.length; heroIndex++) {
                action.heroSeat = Seat.preflopPositionsDESC[heroIndex];

                // Placed here to refresh on hero seat change. When villain moves from HJ to BB after loop refresh when
                // hero moves, we don't want to use a villain HJ seat on the BB.
                RangeData lastRangeData = null;
                for(int villainIndex = 0; villainIndex < heroIndex; villainIndex++) {
                    action.villainSeat = Seat.preflopPositionsDESC[villainIndex];

                    RangeData data = map.get(action);
                    if ((data == null || data.isTheEmptyRange) && lastRangeData != null) {
                        // lastRangeData resets every time hero position moves towards UTG.
                        // If it's not null, then we have an entry for this hero seat. We use that.
                        map.put(new ActionAndSeat(action), lastRangeData);
                    } else if (data == null || data.isTheEmptyRange) {
                        // If lastRangeData is null, then no earlier data for our hero seat (eg, UTG+2), exists.
                        // As such we need to use values from earlier hero seats (eg, LJ for UTG+2) and use those values.
                        assert action.heroSeat.isFullRingOnlySeat();

                        // Because all prior heroSeats have been completely filled in, we are assured map enteries exist
                        // for all (heroSeat - 1, villainSeat)'s ~ except the very tail case where heroSeat-1 = villainSeat.
                        // Ie, hero is LJ. HJ will be filled for all villain from BB->CO, but obv not (HJ, HJ).
                        boolean villainAndHeroAreAdjacent = (heroIndex - 1 == villainIndex);

                        ActionAndSeat newAction = new ActionAndSeat(action);
                        if(villainAndHeroAreAdjacent)
                            newAction.villainSeat = Seat.preflopPositionsDESC[villainIndex - 1];
                        newAction.heroSeat = Seat.preflopPositionsDESC[heroIndex - 1];

                        data = map.get(newAction);
                        map.put(new ActionAndSeat(action), data);

                        assert data != null;
                    } else {
                        lastRangeData = data;
                    }
                }
            }
        }

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
                case CALL5BET:
                    call5BetMap.put(action, data);
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
                    return LimpMap.get(action);
                case RFI:
                    if(action.lastHeroAction == LastAction.RAISE)
                        return RFIMap.get(action);
                case VRFI:
                    boolean isHeroAfterVillain = action.heroSeat.preflopPosition > action.villainSeat.preflopPosition;
                    if(isHeroAfterVillain)
                        return vRFIMap.get(action);

                    // If we didn't raise and we are here, it's because we limp-called like an asshole.
                    // Just give him the BB call vs villain RFI seat.
                    ActionAndSeat newAction = new ActionAndSeat(action);
                    newAction.villainSeat = action.heroSeat;
                    newAction.heroSeat = Seat.BB;
                    return vRFIMap.get(newAction);
                case V3BET:
                    return v3BetMap.get(action);
                case V4BET:
                    return v4BetMap.get(action);
                case CALL5BET:
                    return call5BetMap.get(action);
                default:
                    assert false;
            }
            return null;
        }
    }

    public enum Situation {
        LIMP("Limp"), RFI("RFI"), VRFI("vRFI"), V3BET("v3Bet"), V4BET("v4Bet"), CALL5BET("call5Bet");

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

        public ActionAndSeat() {}

        public ActionAndSeat(ActionAndSeat action) {
            situation = action.situation;
            heroSeat = action.heroSeat;
            villainSeat = action.villainSeat;
            lastHeroAction = action.lastHeroAction;
        }

        public ActionAndSeat(PlayerHandData playerHand) {
            LastActionForStreet lastPreflopAction = playerHand.preflop;
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
}