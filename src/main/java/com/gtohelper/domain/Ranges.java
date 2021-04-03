package com.gtohelper.domain;

import java.io.Serializable;
import java.util.HashMap;
import com.gtohelper.domain.HandData.PlayerHandData;
import com.gtohelper.domain.HandData.PlayerHandData.LastActionForStreet;

public class Ranges implements Serializable {
    private static final long serialVersionUID = 1L;
    RangesMap rangesMap = new RangesMap();

    public RangeData getRangeForHand(PlayerHandData playerHand) {
        PreflopState action = new PreflopState(playerHand);
        return rangesMap.get(action);
    }

    public void addRangeForAction(PreflopState action, RangeData data) {
        rangesMap.put(action, data);
    }

    public void fillEmptyRanges() {
        rangesMap.fillEmptyRanges();
    }

    class RangesMap extends HashMap<PreflopState, RangeData> implements Serializable {
        private static final long serialVersionUID = 1L;
        // We could just use one HashMap<ActionAndSeat, RangeData>. Instead, we'll break down
        // this hashmap by position, as it'll make our use case way easier.
        HashMap<PreflopState, RangeData> LimpMap = new HashMap<>();
        HashMap<PreflopState, RangeData> RFIMap = new HashMap<>();
        HashMap<PreflopState, RangeData> vRFIMap = new HashMap<>();
        HashMap<PreflopState, RangeData> v3BetMap = new HashMap<>();
        HashMap<PreflopState, RangeData> v4BetMap = new HashMap<>();
        HashMap<PreflopState, RangeData> call5BetMap = new HashMap<>();

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

        private void fillNoVillainMap(HashMap<PreflopState, RangeData> map, Situation sit, LastAction lastAction) {
            PreflopState action = new PreflopState();
            action.lastHeroAction = lastAction;
            action.situation = sit;
            action.villainSeat = Seat.BB;

            RangeData lastRangeData = null;
            for(int heroIndex = 0; heroIndex < Seat.preflopPositionsDESC.length; heroIndex++) {
                action.heroSeat = Seat.preflopPositionsDESC[heroIndex];

                RangeData data = map.get(action);
                if (data == null || data.isTheEmptyRange)
                    map.put(new PreflopState(action), lastRangeData);
                else
                    lastRangeData = data;
            }
        }

        // by oopVillain, we mean preflop oop.
        private void fillOOPVillainMap(HashMap<PreflopState, RangeData> map, Situation sit, LastAction lastAction) {
            PreflopState action = new PreflopState();
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
                        map.put(new PreflopState(action), lastRangeData);
                    else
                        lastRangeData = data;
                }
            }
        }

        // by ipVillain, we mean preflop ip.
        private void fillIPVillainMap(HashMap<PreflopState, RangeData> map, Situation sit, LastAction lastAction) {
            PreflopState action = new PreflopState();
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
                        map.put(new PreflopState(action), lastRangeData);
                    } else if (data == null || data.isTheEmptyRange) {
                        // If lastRangeData is null, then no earlier data for our hero seat (eg, UTG+2), exists.
                        // As such we need to use values from earlier hero seats (eg, LJ for UTG+2) and use those values.
                        assert action.heroSeat.isFullRingOnlySeat();

                        // Because all prior heroSeats have been completely filled in, we are assured map enteries exist
                        // for all (heroSeat - 1, villainSeat)'s ~ except the very tail case where heroSeat-1 = villainSeat.
                        // Ie, hero is LJ. HJ will be filled for all villain from BB->CO, but obv not (HJ, HJ).
                        boolean villainAndHeroAreAdjacent = (heroIndex - 1 == villainIndex);

                        PreflopState newAction = new PreflopState(action);
                        if(villainAndHeroAreAdjacent)
                            newAction.villainSeat = Seat.preflopPositionsDESC[villainIndex - 1];
                        newAction.heroSeat = Seat.preflopPositionsDESC[heroIndex - 1];

                        data = map.get(newAction);
                        map.put(new PreflopState(action), data);

                        assert data != null;
                    } else {
                        lastRangeData = data;
                    }
                }
            }
        }

        @Override
        public RangeData put(PreflopState action, RangeData data) {
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
            if (!(obj instanceof PreflopState)) {
                return null;
            }

            PreflopState action = (PreflopState) obj;
            return getBestMatch(action);
        }

        private RangeData getBestMatch(PreflopState action) {
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
                    PreflopState newAction = new PreflopState(action);
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
}