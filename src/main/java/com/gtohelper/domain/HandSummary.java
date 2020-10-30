package com.gtohelper.domain;

import java.sql.Timestamp;

// I'm way too lazy to write all these damn getters//setters
public class HandSummary {
    public int hand;
    public short gametype;
    public short site;
    public short limit;
    public int table;
    public String hand_no;
    public Timestamp date_played;
    public Timestamp date_imported;
    public short cnt_players;
    public short cnt_players_f;
    public short cnt_players_t;
    public short cnt_players_r;
    public float amt_pot;
    public float amt_rake;
    public float amt_short_stack;
    public float amt_pot_p;
    public float amt_pot_f;
    public float amt_pot_t;
    public float amt_pot_r;
    public String str_actors_p;
    public String str_actors_f;
    public String str_actors_t;
    public String str_actors_r;
    public String str_aggressors_p;
    public String str_aggressors_f;
    public String str_aggressors_t;
    public String str_aggressors_r;
    public short id_win_hand;
    public int id_winner;
    public short button;
    public short card_1;
    public short card_2;
    public short card_3;
    public short card_4;
    public short card_5;
    public boolean flg_note;
    public boolean flg_tag;
    public boolean flg_autonote;
}
