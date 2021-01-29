package com.gtohelper.domain;

public enum Seat {
    // The 'position' of the seat is it's Enum.ordinal() value.
    BTN("BTN", 7, 9), CO("CO", 6, 8), HJ("HJ", 5, 7),
    LJ("LJ", 4, 6), UTG2("UTG+2", 3, 5), UTG1("UTG+1", 2, 4),
    UTG("UTG", 1, 3), TthSeat("Tenth Seat", 0, 2),
    BB("BB", 9, 1), SB("SB", 8, 0);

    public static final Seat valuesByTrackerPosition[] = values();
    public static final Seat preflopPositionsASC[] = { TthSeat, UTG, UTG1, UTG2, LJ, HJ, CO, BTN, SB, BB };
    public static final Seat preflopPositionsDESC[] = { BB, SB, BTN, CO, HJ, LJ, UTG2, UTG1, UTG, TthSeat };
    public static final Seat postflopPositionsASC[] = { SB, BB, UTG, TthSeat, UTG1, UTG2, LJ, HJ, CO, BTN };
    public static final Seat postflopPositionsDESC[] = { BTN, CO, HJ, LJ, UTG2, UTG1, UTG, TthSeat, SB, BB };
    public String name;
    public int preflopPosition, postflopPosition, trackerPosition;
    Seat(String name, int preflopPosition, int postflopPosition) {
        this.name = name;
        this.preflopPosition = preflopPosition;
        this.postflopPosition = postflopPosition;
        this.trackerPosition = ordinal();
    }

    public boolean isFullRingOnlySeat() {
        return postflopPosition > 1 && postflopPosition <= 6;
    }
    public boolean isBlindSeat() { return name.equals("SB") || name.equals("BB"); }


    public Seat getNextSeat() {
        if(name.equals(BTN))
            return Seat.SB;
        else
            return valuesByTrackerPosition[ordinal() - 1];
    }

    public static Seat fromString(String text) {
        for(Seat s : Seat.values()) {
            if(s.name.equals(text)) {
                return s;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }

    public static Seat fromTrackerPosition(int position) {
        return Seat.values()[position];
    }
}
