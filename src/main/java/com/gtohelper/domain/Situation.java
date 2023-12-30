package com.gtohelper.domain;

import java.io.Serializable;

public enum Situation implements Serializable {
    LIMP("Limp"), RFI("RFI"), VRFI("vs RFI"), V3BET("vs 3Bet"), V4BET("vs 4Bet"), V5BET("vs 5Bet");

    private static final long serialVersionUID = 1L;
    public static final Situation values[] = values();
    public String name;
    Situation(String name) {
        this.name = name;
    }

    public static Situation fromString(String text) {
        for(Situation s : Situation.values()) {
            if(s.name.equals(text)) {
                return s;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
