package com.gtohelper.domain;

public class Player {
    public int id_player;
    public String player_name;
    public boolean flg_note;
    public boolean flg_tag;

    @Override
    public String toString() {
        return player_name;
    }
}
