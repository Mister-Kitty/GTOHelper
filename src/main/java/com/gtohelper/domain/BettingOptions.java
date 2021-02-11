package com.gtohelper.domain;

import java.io.Serializable;
import java.util.ArrayList;

public class BettingOptions implements Serializable {
    public final String name;

    public Options options = new Options();

    public IPStreetAction IPFlop = new IPStreetAction(Street.FLOP),
            IPTurn = new IPStreetAction(Street.TURN),
            IPRiver = new IPStreetAction(Street.RIVER);
    public OOPStreetAction OOPFlop = new OOPStreetAction(Street.FLOP),
            OOPTurn = new OOPStreetAction(Street.TURN),
            OOPRiver = new OOPStreetAction(Street.RIVER);

    public BettingOptions(String name) {
        this.name = name;
    }

    public class Options implements Serializable{
        public int allInThresholdPercent;
        public int allInOnlyIfLessThanNPercent;
        public boolean forceOOPBet;
        public boolean forceOOPCheckIPBet;
    }

    public static abstract class StreetAction implements Serializable{
        Street street;
        boolean canAllIn;
        Bets bets = new Bets();
        Raises raises = new Raises();

        public Street getStreet() { return street; }
        public boolean getCanAllIn() { return canAllIn; }
        public void setCanAllIn(boolean can) { canAllIn = can; }
        public Bets getBets() { return bets; }
        public void setBets(String betsString) { bets = new Bets(betsString); }
        public Raises getRaises() { return raises; }
        public void setRaises(String raiseString) { raises = new Raises(raiseString); }
    }

    public static class IPStreetAction extends StreetAction {
        boolean can3Bet;

        public boolean getCan3Bet() { return can3Bet; }
        public void setCan3Bet(boolean canThreeBet) { can3Bet = canThreeBet; }

        public IPStreetAction(Street s) { street = s; }

        public void setActionData(boolean canAllIn, boolean can3Bet, String betsString, String raisesString) {
            this.canAllIn = canAllIn;
            this.can3Bet = can3Bet;
            this.bets = new Bets(betsString);
            this.raises = new Raises(raisesString);
        }
    }

    public static class OOPStreetAction extends StreetAction {
        public Bets donks = new Bets();

        public Bets getDonks() { return donks; }
        public void setDonks(String donkBetString) { donks = new Bets(donkBetString); }

        public OOPStreetAction(Street s) { street = s; }

        public void setActionData(boolean canAllIn, String betsString, String raisesString, String donksString) {
            this.canAllIn = canAllIn;
            this.bets = new Bets(betsString);
            this.raises = new Raises(raisesString);
            this.donks = new Bets(donksString);
        }
    }

    public static class Bets implements Serializable{
        boolean allIn = false;
        String initialString;
        ArrayList<Integer> percentOptions = new ArrayList<Integer>();

        private Bets() {}

        // I should probably have setBetString be a function rather than passed into the constructor.... whatever.
        public Bets(String betsString) {
            initialString = betsString;
            parseBetStrings(betsString);
        }

        protected void parseBetStrings(String betsString) {
            String[] sizes = betsString.split(",");
            for(String size : sizes) {
                parseBet(size.trim());
            }
        }

        protected void parseBet(String bet) {
            if(bet.toLowerCase().equals("allin")) {
                allIn = true;
            }
            else if (bet.matches("\\d+")) {
                percentOptions.add(Integer.parseInt(bet));
            }
        }

        public boolean getAllIn() {
            return allIn;
        }

        public String getInitialString() { return initialString; }

        public ArrayList<Integer> getBetPercentList() {
            return percentOptions;
        }

        public ArrayList<Integer> getSizeOfAllBets(int currentPot, int effectiveStack, int facingBet, boolean addAllIn) {
            ArrayList<Integer> results = new ArrayList<Integer>();
            for(Integer i : percentOptions) {
                Float percent = i.floatValue() / 100;
                Integer betPot = Math.round((currentPot + facingBet) * percent);

                // Suppose we face 100, with 175 left. We want to initially 50% it to 400.
                // But with 75 effective, we actually want to return 175 as that's
                // what we consider to be the bet size.
                if(betPot > effectiveStack + facingBet)
                    betPot = effectiveStack + facingBet;

                if(!results.contains(betPot))
                    results.add(betPot);
            }

            if((addAllIn || allIn) && !results.contains(effectiveStack))
                results.add(effectiveStack);

            return results;
        }
    }

    public static class Raises extends Bets {
        ArrayList<Float> multiplierOptions = new ArrayList<Float>();

        private Raises() {}

        public Raises(String raiseString) {
            initialString = raiseString;
            parseBetStrings(raiseString);
        }

        @Override
        protected void parseBet(String bet) {
            if(bet.endsWith("x")) {
                String decimalString = bet.substring(0, bet.lastIndexOf("x"));
                multiplierOptions.add(Float.parseFloat(decimalString));
                return;
            }
            super.parseBet(bet);
        }

        public ArrayList<Integer> getSizeOfRaisesOntop(int currentPot, int effectiveStack, int facingBet, boolean addAllIn) {
            ArrayList<Integer> results = getSizeOfAllBets(currentPot, effectiveStack, facingBet, addAllIn);

            for(Float i : multiplierOptions) {
                Integer betPot = Math.round(facingBet * i);

                if(betPot > effectiveStack + facingBet)
                    betPot = effectiveStack + facingBet;

                if(!results.contains(betPot))
                    results.add(betPot);
            }

            return results;
        }
    }
}
