package com.gtohelper.domain;

public enum Actor {
    IP,
    OOP;

    public static Actor nextActor(Actor t) {
        if(t == null)
            return OOP;
        else if(t.equals(IP))
            return OOP;
        else
            return IP;
    }
}
