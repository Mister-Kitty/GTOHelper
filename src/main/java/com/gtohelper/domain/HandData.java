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

    // cash_hand_player_statistics fields.
    // OOP player
    public PlayerHandData oopPlayer = new PlayerHandData();
    public PlayerHandData ipPlayer = new PlayerHandData();

    // Default sorted by the data manager to be by position DESC
    public ArrayList<PlayerHandData> playerHandData = new ArrayList<>();

    // IP Player
    public static class PlayerHandData {
        public int id_hand;
        public int id_player;
        public short holecard_1;
        public short holecard_2;
        public float amt_before;
        public short position;
        public String p_action;
        public String f_action;
        public String t_action;
        public String r_action;
    }

}
