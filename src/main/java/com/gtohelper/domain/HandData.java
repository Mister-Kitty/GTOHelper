package com.gtohelper.domain;

import java.sql.Timestamp;
import java.util.ArrayList;

// HandData is a mix of cash_hand_summary & cash_hand_statistics, as we often need a both for GUI and Solve generation
public class HandData {
    public int id_hand;
    public Timestamp date_played;
    public float amt_pot;
    public short card_1;
    public short card_2;
    public short card_3;
    public short card_4;
    public short card_5;
    public String str_actors_p;
    public String str_aggressors_p;

    // Calculated fields
    public PlayerHandData oopPlayer;
    public PlayerHandData ipPlayer;
    public int highestPreflopBetLevel; // eg, 1bet, 2bet, 3bet

    // Default sorted by the data manager to be by position DESC
    public ArrayList<PlayerHandData> playerHandData = new ArrayList<>();
    public PlayerHandData getHandDataForPosition(int position) {
        for(PlayerHandData handData : playerHandData) {
            if(handData.position == position)
                return handData;
        }
        return null;
    }

    public static class PlayerHandData {
        public int id_hand;
        public int id_player;
        public short holecard_1;
        public short holecard_2;
        public float amt_before;
        public float amt_won;
        public short position;
        public Ranges.Seat seat; // even though this and 'position' are redundant, it's super convenient to have both.
        public String p_action;
        public String f_action;
        public String t_action;
        public String r_action;

        // Calculated fields to be set
        // These 3 fields are the info for the last action taken by the player.
        public short p_betLevel;
        public short p_vsPosition;
        public Ranges.LastAction last_p_action;

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
            if(data.amt_won >= largestWinnings) {
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
            if(data.amt_won <= largestLosings) {
                largestLosings = data.amt_won;
                largestLosingsIndex = currentIndex;
            }
        }

        return playerHandData.get(largestLosingsIndex);
    }

}
