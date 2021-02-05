package com.gtohelper.domain;

import java.io.Serializable;

public class Player implements Serializable {
    public int id_player;
    public String player_name;
    public transient boolean flg_note;
    public transient boolean flg_tag;

    @Override
    public String toString() {
        return player_name;
    }
}
