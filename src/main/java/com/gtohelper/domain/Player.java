package com.gtohelper.domain;

import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id_player;
    public String player_name;
    public int total_hands;

    // If you choose to impliment this later, 	bool_or(p.flg_note) as flg_note,   into the SELECT statement when GROUP BY'd
    //public transient boolean flg_note;
    //public transient boolean flg_tag;

    @Override
    public String toString() {
        return player_name;
    }
}
