package com.gtohelper.utility;

import com.gtohelper.domain.HandData;

public class CardResolver {

 /* 1 is 2c
    13 is Ac

    14 is 2d
    26 is Ad

    27 is 2h
    39 is Ah

    40 is 2s
    52 is As */

    public static String resolveToString(int card) {
        char suit;
        if(card <= 13)
            suit = 'c';
        else if (card <= 26)
            suit = 'd';
        else if (card <= 39)
            suit = 'h';
        else
            suit = 's';

        int mod = (card % 13) + 1;
        String result;
        if(mod == 1)
            result = "A";
        else if(mod <= 8)
            result = String.valueOf(mod);
        else if (mod == 9)
            result = "T";
        else if (mod == 10)
            result = "J";
        else if (mod == 11)
            result = "Q";
        else
            result = "K";

        return (result += suit);
    }

    public static String getHandString(HandData data) {
        return resolveToString(data.holecard_1) + " " + resolveToString(data.holecard_2);
    }

    public static String getBoardString(HandData data) {
        String flop = resolveToString(data.card_1) + " " +
                resolveToString(data.card_2) + " " +
                resolveToString(data.card_3);

        if(data.card_4 == 0)
            return flop;

        String turn = resolveToString(data.card_4);
        if(data.card_5 == 0)
            return flop + " " + turn;

        String river = resolveToString(data.card_5);
        return flop + " " + turn + " " + river;
    }
}
