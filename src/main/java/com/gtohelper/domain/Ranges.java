package com.gtohelper.domain;

import java.util.ArrayList;

public class Ranges {
    public enum Seat {
        UTG("UTG"), UTG1("UTG+1"), UTG2("UTG+2"),
        LJ("LJ"), HJ("HJ"), CO("CO"),
        BTN("BTN"), SB("SB"), BB("BB");

        public String name;
        Seat(String name) {
            this.name = name;
        }
    }

    public enum Action {
        RFI("RFI"), VRFI("vRFI"), V3BET("v3Bet"), V4BET("v4Bet");

        public String name;
        Action(String name) {
            this.name = name;
        }
    }

    public class RangesForSeat {
        public Seat seat;

        RangeData RFI, vRFI, v3Bet;

    }





}
