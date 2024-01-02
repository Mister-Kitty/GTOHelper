package com.gtohelper.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import com.gtohelper.domain.HandData.PlayerHandData;

public class Ranges implements Serializable {
    /*
    Looks like instead of holding each of the ranges as a separate member, we use a single
    subclass. I'm not sure why this was done... but it was.

    Also, consider the case of us trying to get a full ring UTG range but with only 6max ranges specified.
    In such a case, we've actually called fillEmptyRanges() after the RangeFilesModel initializes us.
    So we're good.
     */
    @Serial
    private static final long serialVersionUID = 1L;
    RangesMap rangesMap = new RangesMap();

    public RangeData getRangeForHand(PlayerHandData playerHand) {
        PreflopState action = new PreflopState(playerHand);
        return rangesMap.get(action);
    }

    public void addRangeForAction(PreflopState state, RangeData data) {
        rangesMap.put(state, data);
    }

    public void fillEmptyRanges() {
        rangesMap.fillEmptyRanges();
    }

    static class RangesMap extends HashMap<PreflopState, RangeData> implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        // We could just use one HashMap<ActionAndSeat, RangeData>. Instead, we'll break down
        // this hashmap by position, as it'll make our use case way easier.
        HashMap<PreflopState, RangeData> limpMap = new HashMap<>();
        HashMap<PreflopState, RangeData> RFIMap = new HashMap<>();
        HashMap<PreflopState, RangeData> vsRFIMap = new HashMap<>();
        HashMap<PreflopState, RangeData> vs3BetMap = new HashMap<>();
        HashMap<PreflopState, RangeData> vs4BetMap = new HashMap<>();
        HashMap<PreflopState, RangeData> vs5BetMap = new HashMap<>();

        public void fillEmptyRanges() {
            fillNoVillainMap(limpMap, Situation.LIMP, LastAction.CALL);
            fillNoVillainMap(RFIMap, Situation.RFI, LastAction.RAISE);

            fillIPMap(vsRFIMap, Situation.VRFI, LastAction.CALL);
            fillIPMap(vsRFIMap, Situation.VRFI, LastAction.RAISE);

            fillOPMap(vs3BetMap, Situation.V3BET, LastAction.CALL);
            fillOPMap(vs3BetMap, Situation.V3BET, LastAction.RAISE);

            fillIPMap(vs4BetMap, Situation.V4BET, LastAction.CALL);
            fillIPMap(vs4BetMap, Situation.V4BET, LastAction.RAISE);

            fillOPMap(vs5BetMap, Situation.V5BET, LastAction.CALL);
        }

        private void fillNoVillainMap(HashMap<PreflopState, RangeData> map, Situation sit, LastAction lastAction) {
            PreflopState action = new PreflopState();
            action.situation = sit;
            action.lastHeroAction = lastAction;

            RangeData lastRangeData = null;
            for(int heroIndex = 0; heroIndex < Seat.preflopPositionsDESC.length; heroIndex++) {
                action.heroSeat = Seat.preflopPositionsDESC[heroIndex];

                RangeData data = map.get(action);
                if ((data == null || data.isTheEmptyRange) && (lastRangeData != null && !lastRangeData.isTheEmptyRange))
                    map.put(new PreflopState(action), lastRangeData);
                else
                    lastRangeData = data;
            }
        }

        // by oopVillain, we mean preflop oop.
        private void fillIPMap(HashMap<PreflopState, RangeData> map, Situation sit, LastAction lastAction) {
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
                    if ((data == null || data.isTheEmptyRange) && (lastRangeData != null && !lastRangeData.isTheEmptyRange))
                        map.put(new PreflopState(action), lastRangeData);
                    else
                        lastRangeData = data;
                }
            }
        }

        // by ipVillain, we mean preflop ip.
        private void fillOPMap(HashMap<PreflopState, RangeData> map, Situation sit, LastAction lastAction) {
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
                    if ((data == null || data.isTheEmptyRange) && (lastRangeData != null && !lastRangeData.isTheEmptyRange)) {
                        // lastRangeData resets every time hero position moves towards UTG.
                        // If it's not null/empty, then we have an entry for this hero seat. We use that.
                        map.put(new PreflopState(action), lastRangeData);
                    } else if (data == null || data.isTheEmptyRange) {
                        // If lastRangeData is null, then no earlier data for our hero seat (eg, UTG+2), exists.
                        // As such we need to use values from earlier hero seat (eg, LJ for UTG+2) and use those values.
                        // Because all prior heroSeats have been completely filled in, we are assured map entries exist.

                        // Note the edge case where villain is to heroes left. Here, we need to push villain over too.
                        boolean villainAndHeroAreAdjacent = (heroIndex - 1 == villainIndex);

                        PreflopState newAction = new PreflopState(action);
                        if(villainAndHeroAreAdjacent) {
                            // Check base case where we're already at the earliest positions and can't go further.
                            if(villainIndex == 0)
                                continue;

                            newAction.villainSeat = Seat.preflopPositionsDESC[villainIndex - 1];
                        }
                        newAction.heroSeat = Seat.preflopPositionsDESC[heroIndex - 1];

                        data = map.get(newAction);

                        // data will be empty if BB range is missing.
                        if(data != null && !data.isTheEmptyRange)
                            map.put(new PreflopState(action), data);

                        assert data != null && !data.isTheEmptyRange;
                    } else {
                        lastRangeData = data;
                    }
                }
            }
        }

        @Override
        public RangeData put(PreflopState action, RangeData data) {
            switch (action.situation) {
                case LIMP -> {
                    PreflopState noVersusAction = new PreflopState(action);
                    noVersusAction.villainSeat = null;
                    limpMap.put(noVersusAction, data);
                }
                case RFI -> RFIMap.put(action, data);
                case VRFI -> vsRFIMap.put(action, data);
                case V3BET -> vs3BetMap.put(action, data);
                case V4BET -> vs4BetMap.put(action, data);
                case V5BET -> vs5BetMap.put(action, data);
                default -> {
                    assert false;
                }
            }
            return data;
        }

        @Override
        public RangeData get(Object obj) {
            if (!(obj instanceof PreflopState)) {
                return null;
            }

            PreflopState newState = new PreflopState((PreflopState) obj);
            if(newState.situation == Situation.LIMP) {
                newState.villainSeat = null;
            }

            return getBestMatch(newState);
        }

        private RangeData getBestMatch(PreflopState action) {
            // This really sucks, but the behavior of RFI and vRFI maps are different and need to be broken apart.
            // We'll also add some assertions to check for coding errors.
            switch (action.situation) {
                case LIMP:
                    assert action.lastHeroAction == LastAction.CALL;
                    return limpMap.get(action);
                case RFI:
                    assert action.lastHeroAction == LastAction.RAISE;
                    return RFIMap.get(action);
                case VRFI:
                    assert action.lastHeroAction == LastAction.CALL;
                    boolean isHeroAfterVillain = action.heroSeat.preflopPosition > action.villainSeat.preflopPosition;
                    if(isHeroAfterVillain)
                        return vsRFIMap.get(action);

                    // If we are here, it's because we limp-called like an asshole.
                    // Just give him the BB call vs villain RFI seat.
                    PreflopState newAction = new PreflopState(action);
                    newAction.villainSeat = action.heroSeat;
                    newAction.heroSeat = Seat.BB;
                    return vsRFIMap.get(newAction);
                case V3BET:
                    return vs3BetMap.get(action);
                case V4BET:
                    return vs4BetMap.get(action);
                case V5BET:
                    return vs5BetMap.get(action);
                default:
                    assert false;
            }
            return null;
        }
    }
}