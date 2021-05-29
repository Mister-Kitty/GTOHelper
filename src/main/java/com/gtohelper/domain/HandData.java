package com.gtohelper.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// HandData is a mix of cash_hand_summary & cash_hand_statistics, as we often need a both for GUI and Solve generation
public class HandData implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id_hand;
    public LocalDateTime date_played;
    public short cnt_players;
    public short cnt_players_f;
    public short cnt_players_t;
    public short cnt_players_r;
    public float amt_pot;
    public short card_1;
    public short card_2;
    public short card_3;
    public short card_4;
    public short card_5;
    public String str_actors_p;
    public String str_aggressors_p;
    public String str_actors_f;
    public String str_aggressors_f;
    public String str_actors_t;
    public String str_aggressors_t;
    public String str_actors_r;
    public String str_aggressors_r;
    public float amt_pot_f;

    // cash_limit fields
    public String limit_name;
    public float amt_sb;
    public float amt_bb;

    // Calculated fields
    public PlayerHandData oopPlayer;
    public PlayerHandData ipPlayer;
    public Actor lastPfAggressor;
    public int highestPreflopBetLevel; // eg, 1bet, 2bet, 3bet
    public SolvabilityLevel solveabilityLevel;

    public enum SolvabilityLevel implements Serializable {
        HU_PRE(0),
        MULTI_PRE_HU_FLOP(1),
        MULTI_FLOP_HU_FLOP_VPIP(2),
        MULTI_FLOP_MULTI_VPIP(3),
        HU_SHOWDOWN(4),
        MULTI_SHOWDOWN(5);

        private static final long serialVersionUID = 1L;

        int ambiguityLevel;
        SolvabilityLevel(int level) {
            ambiguityLevel = level;
        }

        public boolean lessThanOrEqualTo(SolvabilityLevel other) {
            return ambiguityLevel <= other.ambiguityLevel;
        }

        @Override
        public String toString() {
            switch(this) {
                case HU_PRE:
                    return "HU Preflop";
                case MULTI_PRE_HU_FLOP:
                    return "HU to Flop";
                case MULTI_FLOP_HU_FLOP_VPIP:
                    return "Multi to Flop, HU VPIP on Flop";
                case MULTI_FLOP_MULTI_VPIP:
                    return "Multi to Flop, Multi VPIP on Flop";
                case HU_SHOWDOWN:
                    return "HU at Showdown";
                case MULTI_SHOWDOWN:
                    return "Multiway Showdown";
                default:
                    return "Unknown";
            }
        }
    }

    // PlayerHandData and associated functions
    public ArrayList<PlayerHandData> playerHandData = new ArrayList<>();

    public PlayerHandData getHandDataForPosition(int position) {
        // Default sorted by the data manager to be by position DESC
        for(PlayerHandData handData : playerHandData) {
            if(handData.position == position)
                return handData;
        }
        return null;
    }

    public PlayerHandData getHandDataForSeat(Seat seat) {
        return getHandDataForPosition(seat.trackerPosition);
    }

    public PlayerHandData getHandDataForPlayer(int playerId) {
        for(PlayerHandData handData : playerHandData) {
            if(handData.id_player == playerId)
                return handData;
        }
        return null;
    }

    public List<PlayerHandData> getVillainHandsThatReachStreet(Street street, int heroPlayerId) {
        return getHandStreamForStreet(street).filter(t -> t.id_player != heroPlayerId).collect(Collectors.toList());
    }

    public List<PlayerHandData> getHandsThatReachStreet(Street street) {
        return getHandStreamForStreet(street).collect(Collectors.toList());
    }

    private Stream<PlayerHandData> getHandStreamForStreet(Street street) {
        switch(street) {
            case PRE:
                return playerHandData.stream();
            case FLOP:
                return playerHandData.stream().filter(t -> !t.f_action.isEmpty());
            case TURN:
                return playerHandData.stream().filter(t -> !t.t_action.isEmpty());
            case RIVER:
                return playerHandData.stream().filter(t -> !t.r_action.isEmpty());
            case SHOWDOWN:
                return playerHandData.stream().filter(t -> t.flg_showdown);
        }

        return null;
    }

    // utility functions
    public int getValueAsChips(float value, int chipsPerBB) {
        // We want 1BB = 100 chips.
        // Convert value into number of BB.
        // max effective stack is 65535 chips in pio. This gives us ~650BB max stack
        float numberOfBB = (value / amt_bb);
        return (int) (numberOfBB * 100);
    }

    public float getAsNumberOfBB(float value, int significantDigits) {
        float numBB = value / amt_bb;
        return (float) (Math.round(numBB * Math.pow(10, significantDigits)) / Math.pow(10, significantDigits));
    }

    public float getIPandOOPEffective() {
        float ipBefore = ipPlayer.amt_before;
        float oopBefore = oopPlayer.amt_before;
        return Math.max(ipBefore, oopBefore);
    }

    public Street getLastStreetOfHand() {
        if(playersAtShowdown() > 0)
            return Street.SHOWDOWN;
        else if(cnt_players_r > 0)
            return Street.RIVER;
        else if(cnt_players_t > 0)
            return Street.TURN;
        else if(cnt_players_f > 0)
            return Street.FLOP;
        else
            return Street.PRE;
    }

    public String getActorsForStreet(Street street) {
        switch(street) {
            case PRE:
                return str_actors_p;
            case FLOP:
                return str_actors_f;
            case TURN:
                return str_actors_t;
            case RIVER:
                return str_actors_r;
            default:
                return "";
        }
    }

    public String getAggressorsForStreet(Street street) {
        switch(street) {
            case PRE:
                return str_aggressors_p;
            case FLOP:
                return str_aggressors_f;
            case TURN:
                return str_aggressors_t;
            case RIVER:
                return str_aggressors_r;
            default:
                return "";
        }
    }

    public String getAllAggressorsUpToStreet(Street street) {
        String result = str_aggressors_p;

        if(street == Street.PRE)
            return result;

        result += str_aggressors_f;
        if(street == Street.FLOP)
            return result;

        result += str_aggressors_t;
        if(street == Street.TURN)
            return result;

        result += str_aggressors_r;
        return result;
    }

    public int playersAtShowdown() {
        // This has to be deduced by looking through PlayerHandData.
        int count = 0;
        for(PlayerHandData handData : playerHandData) {
            if(handData.flg_showdown)
                count++;
        }
        return count;
    }

    public static class PlayerHandData implements Serializable {
        private static final long serialVersionUID = 1L;
        public int id_hand;
        public int id_player;
        public String player_name;
        public short holecard_1;
        public short holecard_2;
        public float amt_before;
        public float amt_won;
        public short position; // deprecated. Should only use seat
        public Seat seat;
        public String p_action;
        public String f_action;
        public String t_action;
        public String r_action;
        public boolean flg_showdown;

        // Calculated fields to be set
        // These 3 fields are the info for the last action taken by the player.
        public class LastActionForStreet implements Serializable{
            private static final long serialVersionUID = 1L;
            public short betLevel;
            public Seat vsSeat;
            public Action action;
        }
        LastActionForStreet preflop = new LastActionForStreet();
        LastActionForStreet flop = new LastActionForStreet();
        LastActionForStreet turn = new LastActionForStreet();
        LastActionForStreet river = new LastActionForStreet();

        public LastActionForStreet getLastActionForStreet(Street street) {
            switch(street) {
                case PRE:
                    return preflop;
                case FLOP:
                    return flop;
                case TURN:
                    return turn;
                case RIVER:
                    return river;
                default:
                    return null;
            }
        }

        public Street getLastStreet() {
            if(flg_showdown)
                return Street.SHOWDOWN;
            else if(!r_action.isEmpty())
                return Street.RIVER;
            else if(!t_action.isEmpty())
                return Street.TURN;
            else if(!f_action.isEmpty())
                return Street.FLOP;
            else
                return Street.PRE;
        }

        public String getCompactedActionStrings(String separator) {
            String result = p_action;
            if(f_action.isEmpty())
                return result;

            result += separator + f_action;
            if(t_action.isEmpty())
                return result;

            result += separator + t_action;
            if(r_action.isEmpty())
                return result;

            result += separator + r_action;
            return result;
        }
    }

    // The following 2 functions are defined to never return the same player, even if they're tied.
    // We'll do this by solving front to back for the winner, and back to front for the loser. Thus ties will be diff.
    public PlayerHandData getBiggestWinner() {
        assert playerHandData.size() >= 2;

        int currentIndex = 0;
        float largestWinnings = 0;
        int largestWinningsIndex = 0;
        for(; currentIndex < playerHandData.size(); currentIndex++) {
            PlayerHandData data = playerHandData.get(currentIndex);
            if(currentIndex == 0 || data.amt_won >= largestWinnings) {
                largestWinnings = data.amt_won;
                largestWinningsIndex = currentIndex;
            }
        }

        return playerHandData.get(largestWinningsIndex);
    }

    public PlayerHandData getBiggestLoser() {
        assert playerHandData.size() >= 2;

        int currentIndex = playerHandData.size() - 1;
        float largestLosings = 0;
        int largestLosingsIndex = currentIndex;
        for(; currentIndex >= 0 ; currentIndex--) {
            PlayerHandData data = playerHandData.get(currentIndex);
            if(currentIndex == (playerHandData.size() - 1) || data.amt_won <= largestLosings) {
                largestLosings = data.amt_won;
                largestLosingsIndex = currentIndex;
            }
        }

        return playerHandData.get(largestLosingsIndex);
    }

    public static void sortHandDataListByPreflopPosition(List<PlayerHandData> list) {
        Collections.sort(list, preflopSorter);
    }

    public static void sortHandDataListByPostflopPosition(List<PlayerHandData> list) {
        Collections.sort(list, postflopSorter);
    }

    private static final Comparator<PlayerHandData> preflopSorter = Comparator.comparingInt(h -> h.seat.preflopPosition);
    private static final Comparator<PlayerHandData> postflopSorter = Comparator.comparingInt(h -> h.seat.postflopPosition);

}
