package com.gtohelper.domain;

public enum Street {
    PRE,
    FLOP,
    TURN,
    RIVER,
    SHOWDOWN;

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
