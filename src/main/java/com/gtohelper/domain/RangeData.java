package com.gtohelper.domain;

import com.gtohelper.utility.CardResolver;

import java.util.Arrays;

public class RangeData {
    float[] floats;

    public RangeData(float[] otherFloats) {
        assert otherFloats.length == 1326;
        floats = Arrays.copyOf(otherFloats, otherFloats.length);
    }

    public RangeData(String rangeString) {
        floats = new float[1326];
        // let's try some cheesy ways to detect file format.

        // Pioformat has 1326 floats, so let's split and see if there are that many
        String[] spaceSplit = rangeString.split(" ");
        if(spaceSplit.length == 1326) {
            for (int i = 0; i < spaceSplit.length; i++) {
                floats[i] = Float.parseFloat(spaceSplit[i]);
            }
            return;
        }

        // The enumeration format is the last one we support. From here we split on commas.
        // Iterate through every split and go until we're done. Note that "" is valid.
        String[] commaSplit = rangeString.split(",");

        // The above split function will return an empty 1 index array on blank file input.
        // Ensure we skip over that, but not other valid 1 index arrays, by checking the value.
        if(commaSplit[0].isEmpty())
            return;

        for(String nugget : commaSplit) {
            String[] splitNugget = nugget.split("[:]");

            String hand = splitNugget[0];
            // If a value is absent, then it's an implied '1'.
            float fraction;
            if(splitNugget.length == 1)
                fraction = 1;
            else
                fraction = Float.parseFloat(splitNugget[1]);

            // Hand can represent sets of hands or individual hands, eg - JhTs  AA   AJs
            // So let's expand these into an array of specific hands, which we'll then loop over.
            String[] expandedHands = expandHands(hand);
            for(String specificHand : expandedHands) {
                int specificIndex = getIndexForSpecificHand(specificHand);
                floats[specificIndex] = fraction;
            }
        }
    }

    public String getRangeAsString() {
        String result = "";
        for(int i = 0; i < floats.length - 1; i++) {
            result += floats[i] + " ";
        }
        // make sure we don't append " ".
        result += floats[floats.length - 1];

        return result;
    }


    public static String[] expandHands(String hands) {
        // let's cheat and deduce what we're dealing with by length
        if(hands.length() == 4) {
            // Then we have something like Jh8d and we can just return it. Easy.
            return new String[] { hands };
        }

        else if(hands.length() == 3) {
            // Then we have AJs or KTo or something.
            boolean isSuited = hands.charAt(2) == 's';
            if(isSuited) {
                return getSuitedCombos(hands.charAt(0), hands.charAt(1));
            } else {
                return getUnsuitedCombos(hands.charAt(0), hands.charAt(1));
            }
        }

        else if (hands.length() == 2) {
            // either pairs..
            if(hands.charAt(0) == hands.charAt(1)) {
                return getPairs(hands.charAt(0));
            }

            // or all 16 card combos like AK
            return getCombos(hands.charAt(0), hands.charAt(1));
        }

        return null;
    }

    // We use reverse suit because we want the card on the left to be higher. So we want AsAh, not AhAs. Only effects pairs.
    private static final char[] reverseSuitArray = new char[] {'s', 'h', 'd', 'c'};

    private static String[] getSuitedCombos(char card1, char card2) {
        String[] results = new String[4];
        for(int i = 0; i < 4; i++)
            results[i] = "" + card1 + reverseSuitArray[i] + card2 + reverseSuitArray[i];
        return results;
    }

    private static String[] getUnsuitedCombos(char card1, char card2) {
        String[] results = new String[12];
        int index = 0;
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                if(i != j) {
                    results[index] = "" + card1 + reverseSuitArray[i] + card2 + reverseSuitArray[j];
                    index++;
                }
            }
        }
        return results;
    }

    private static String[] getCombos(char card1, char card2) {
        String[] results = new String[16];
        int index = 0;
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                results[index] = "" + card1 + reverseSuitArray[i] + card2 + reverseSuitArray[j];
                index++;
            }
        }
        return results;
    }

    private static String[] getPairs(char card) {
        String[] results = new String[6];

        int index = 0;
        for(int i = 0; i < 4; i++) {
            for(int j = i + 1; j < 4; j++) {
                results[index] = "" + card + reverseSuitArray[i] + card + reverseSuitArray[j];
                index++;
            }
        }
        return results;
    }

    // convenience wrapper method
    private int getIndexForSpecificHand(String specificHand) {
        return getIndexForHand(CardResolver.getCardValueFromCardChar(specificHand.charAt(0)), specificHand.charAt(1),
                CardResolver.getCardValueFromCardChar(specificHand.charAt(2)), specificHand.charAt(3));
    }

    public static int getIndexForHand(int card1Value, char card1Suit, int card2Value, char card2Suit) {
        // Find deck index for both cards, ie {2c .. As} = {0 .. 52}.
        int card1DeckIndex = ((card1Value - 2) * 4) + getSuitValue(card1Suit);
        int card2DeckIndex = ((card2Value - 2) * 4) + getSuitValue(card2Suit);

        // Caller assures the first card is the higher one.
        assert card1DeckIndex > card2DeckIndex;

        // All card1 hands are in contiguous blocks, starting at their (n-1)th to nth sum aka triangle number.
        // Ie, card1 = 3d. Deck index = 5. Triangle number = 15. PioFormat[10,11...,15] = {3d2c 3d2d 3d2h 3d2s 3d3c}
        int card1NthSum = nthSum(card1DeckIndex - 1);

        // And card2Index is the index within card1's contiguous block.
        return card1NthSum + card2DeckIndex;
    }

    private static int nthSum(int n) {
        return (n * (n+1)) / 2;
    }

    private static int getSuitValue(char suit) {
        switch(suit) {
            case 'c':
                return 0;
            case 'd':
                return 1;
            case 'h':
                return 2;
            case 's':
                return 3;
            default:
                assert false;
                return -1;
        }
    }
}
