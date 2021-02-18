package com.gtohelper.utility;

import com.gtohelper.domain.HandData;
import com.gtohelper.domain.Player;

import java.util.Optional;

public class CardResolver {

 /* 1 is 2c
    9 is Tc
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
        else if(mod <= 9)
            result = String.valueOf(mod);
        else if (mod == 10)
            result = "T";
        else if (mod == 11)
            result = "J";
        else if (mod == 12)
            result = "Q";
        else
            result = "K";

        return (result += suit);
    }

    /*
    public static int resolveToCard(String hand) {
        int strength = hand.charAt(0);
        char suit = hand.charAt(1);

        int result = 0

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
*/

    public static String getHandStringForPlayer(Player p, HandData data) {
        // if IP is hero, get IP hand. Else get OOP hand ('cause it's hero or there is no hero).
        Optional<HandData.PlayerHandData> playerHand = data.playerHandData.stream().filter(t -> t.id_player == p.id_player).findFirst();
        if(playerHand.isPresent())
            return getHandString(playerHand.get());
        else
            return "";
    }

    public static String getHandString(HandData.PlayerHandData playerHand) {
        if(playerHand != null)
            return resolveToString(playerHand.holecard_1) + " " + resolveToString(playerHand.holecard_2);
        else
            return "";
    }

    public static String getFlopString(HandData data) {
        return resolveToString(data.card_1) + " " +
                resolveToString(data.card_2) + " " +
                resolveToString(data.card_3);
    }

    public static String getBoardString(HandData data) {
        String flop = getFlopString(data);

        if(data.card_4 == 0)
            return flop;

        String turn = resolveToString(data.card_4);
        if(data.card_5 == 0)
            return flop + " " + turn;

        String river = resolveToString(data.card_5);
        return flop + " " + turn + " " + river;
    }


    public static int getCardValueFromCardChar(char cardValueChar) {
        switch(cardValueChar) {
            case 'T':
                return 10;
            case 'J':
                return 11;
            case 'Q':
                return 12;
            case 'K':
                return 13;
            case 'A':
                return 14;
            default:
                // cleaver way to compute the actual INT of this ascii char quickly.
                return cardValueChar - '0';
        }
    }
}
