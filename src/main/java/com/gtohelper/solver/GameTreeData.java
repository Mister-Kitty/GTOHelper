package com.gtohelper.solver;

import java.util.ArrayList;

public class GameTreeData {
    int effectiveStack;
    int pot;
    Options options = new Options();

    IPStreetAction IPFlop, IPTurn, IPRiver;
    OOPStreetAction OOPFlop, OOPTurn, OOPRiver;

    public enum Street {
        PRE,
        FLOP,
        TURN,
        RIVER,
        SHOWDOWN
    }

    public class Options {
        public int allInThresholdPercent;
        public int allInOnlyIfLessThanNPercent;
        public boolean forceOOPBet;
        public boolean forceOOPCheckIPBet;
    }

    public static abstract class StreetAction {
        Street street;
        boolean canAllIn;
        Bets bets;
        Raises raises;

        public Street getStreet() { return street; }
        public boolean getCanAllIn() { return canAllIn; }
        public Bets getBets() { return bets; }
        public Raises getRaises() { return raises; }
    }

    public static class IPStreetAction extends StreetAction {
        boolean can3Bet;

        public IPStreetAction(Street street, boolean canAllIn, boolean can3Bet, String betsString, String raisesString) {
            this.street = street;
            this.canAllIn = canAllIn;
            this.can3Bet = can3Bet;
            this.bets = new Bets(betsString);
            this.raises = new Raises(raisesString);
        }
    }

    public static class OOPStreetAction extends StreetAction {
        public Bets donks;

        public OOPStreetAction(Street street, boolean canAllIn, String betsString, String raisesString, String donksString) {
            this.street = street;
            this.canAllIn = canAllIn;
            this.bets = new Bets(betsString);
            this.raises = new Raises(raisesString);
            this.donks = new Bets(donksString);
        }

        public Bets getDonks() { return donks; }
    }

    public static class Bets {
        boolean allIn = false;
        ArrayList<Integer> percentOptions = new ArrayList<Integer>();

        private Bets() {}

        public Bets(String betsString) {
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

        public ArrayList<Integer> getBetPercentList() {
            return percentOptions;
        }

        public ArrayList<Integer> getSizeOfAllBets(int currentPot, int effectiveStack, boolean addAllIn) {
            ArrayList<Integer> results = new ArrayList<Integer>();
            for(Integer i : percentOptions) {
                Float percent = i.floatValue() / 100;
                Integer betPot = Math.round(currentPot * percent);

                if(betPot > effectiveStack)
                    betPot = effectiveStack;

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

        public Raises(String raiseString) {
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
    }
}
