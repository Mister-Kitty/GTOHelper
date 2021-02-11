package com.gtohelper.domain;

import java.io.Serializable;

public enum Street implements Serializable {
    PRE,
    FLOP,
    TURN,
    RIVER,
    SHOWDOWN;

    private static final long serialVersionUID = 1L;
    public static Street nextStreet(Street s) {
        if(s == PRE)
            return FLOP;
        else if(s == FLOP)
            return TURN;
        else if (s == TURN)
            return RIVER;
        return SHOWDOWN;
    }
}
