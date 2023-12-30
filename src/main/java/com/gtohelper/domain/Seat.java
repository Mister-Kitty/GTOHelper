package com.gtohelper.domain;

import java.io.Serializable;

public enum Seat implements Serializable {
    // The 'position' of the seat is it's Enum.ordinal() value.
    BTN("BTN", 7, 9), CO("CO", 6, 8), HJ("HJ", 5, 7),
    LJ("LJ", 4, 6), UTG2("UTG2", 3, 5), UTG1("UTG1", 2, 4),
    UTG("UTG", 1, 3), TEN("TEN", 0, 2),
    BB("BB", 9, 1), SB("SB", 8, 0);

    private static final long serialVersionUID = 1L;
    public static final Seat valuesByTrackerPosition[] = values();
    public static final Seat preflopPositionsASC[] = {TEN, UTG, UTG1, UTG2, LJ, HJ, CO, BTN, SB, BB };
    public static final Seat preflopPositionsDESC[] = { BB, SB, BTN, CO, HJ, LJ, UTG2, UTG1, UTG, TEN};
    public static final Seat postflopPositionsASC[] = { SB, BB, UTG, TEN, UTG1, UTG2, LJ, HJ, CO, BTN };
    public static final Seat postflopPositionsDESC[] = { BTN, CO, HJ, LJ, UTG2, UTG1, UTG, TEN, SB, BB };
    public final String name;
    public final int preflopPosition, postflopPosition, trackerPosition;
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

    @Override
    public String toString() {
        return name;
    }
}
