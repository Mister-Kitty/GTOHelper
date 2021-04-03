package com.gtohelper.domain;

import java.io.Serializable;

public enum LastAction implements Serializable {
    CALL("Call"), RAISE("Raise");

    private static final long serialVersionUID = 1L;
    public static final LastAction values[] = values();
    public String name;
    LastAction(String n) { this.name = n; }

    public static LastAction fromString(String text) {
        for(LastAction s : LastAction.values()) {
            if(s.name.equals(text)) {
                return s;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
