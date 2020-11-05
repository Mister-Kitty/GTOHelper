package com.gtohelper.domain;

import java.sql.Timestamp;

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

    public int id_player;
    public short holecard_1;
    public short holecard_2;
}
