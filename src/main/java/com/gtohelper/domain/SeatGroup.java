package com.gtohelper.domain;

import javafx.util.StringConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;


public enum SeatGroup implements Serializable {
    EP("EP - (All FR seats before LJ)", Seat.TEN, Seat.UTG, Seat.UTG1, Seat.UTG2), Tth_Seat(Seat.TEN), UTG(Seat.UTG), UTG1(Seat.UTG1), UTG2(Seat.UTG2),
    MP("MP - (LJ & HJ)", Seat.LJ, Seat.HJ), LJ(Seat.LJ), HJ(Seat.HJ), CO(Seat.CO), BTN(Seat.BTN), SB(Seat.SB), BB(Seat.BB);

    public static final SeatGroup allByPostflopPosition[] = { SB, BB, EP, UTG, UTG1, UTG2, MP, LJ, HJ, CO, BTN };
    public static final SeatGroup allByPreflopPosition[] = { EP, UTG, UTG1, UTG2, MP, LJ, HJ, CO, BTN, SB, BB };

    private static final long serialVersionUID = 1L;
    String seatGroupName;
    Seat[] seats;

    SeatGroup(Seat seat) {
        seats = new Seat[] { seat };
    }

    SeatGroup(String groupName, Seat ...seats) {
        this.seatGroupName = groupName;
        this.seats = Arrays.copyOf(seats, seats.length);
    }

    public boolean isIndividualSeat() { return seatGroupName == null; }
    public String getSeatGroupName() {
        if(isIndividualSeat())
            return seats[0].name;
        else
            return seatGroupName;
    }

    @Override
    public String toString() {
        return getSeatGroupName();
    }

    public String getCommaSeparatedSeats() {
        ArrayList<String> seatsAsStrings = new ArrayList<>();
        for(Seat s : seats) {
            seatsAsStrings.add(String.valueOf(s.trackerPosition));
        }

        return String.join(",", seatsAsStrings);
    }

    public static final StringConverter<SeatGroup> indentFormattedSeatGroupConverter = new StringConverter<>() {
        @Override
        public String toString(SeatGroup seatGroup) {
            if(seatGroup == null)
                return "";
            else if(seatGroup == SeatGroup.HJ || seatGroup == SeatGroup.LJ)
                return "   " + seatGroup.toString();
            else if(seatGroup == SeatGroup.Tth_Seat || seatGroup == SeatGroup.UTG ||
                    seatGroup == SeatGroup.UTG1 || seatGroup == SeatGroup.UTG2)
                return "   " + seatGroup.toString();
            else
                return seatGroup.toString();
        }

        @Override
        public SeatGroup fromString(String s) { return SeatGroup.valueOf(s); }
    };

    public boolean areWeIPPreflop(SeatGroup other) {
        assert (this != other);
        // Could also assert that one isn't an actual group of the other

        // Okay. We're IP if
        int otherPreflopIndex = Arrays.asList(allByPreflopPosition).indexOf(other);
        int ourPreflopIndex = Arrays.asList(allByPreflopPosition).indexOf(this);

        return ourPreflopIndex > otherPreflopIndex;
    }

}
