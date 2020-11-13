package com.gtohelper.solver;

import java.io.IOException;
import java.util.ArrayList;

public interface ISolver {
    void connectAndInit(String pioLocation) throws IOException;
    void setRange(String position, String range) throws IOException;
    void setBoard(String board) throws IOException;
    void setEffectiveStack(int stack) throws IOException;
    void setGameTreeOptions(int allInThresholdPercent, int allInOnlyIfLessThanNPercent,
                            boolean forceOOPBet, boolean forceOOPCheckIPBet);
    void setIsomorphism(int flop, int turn) throws IOException;
    void setPot(int oopInvestment, int ipInvestment, int pot) throws IOException;
    void clearLines() throws IOException;

    void setIPFlop(boolean addAllIn, boolean dont3Bet, String betSizesString, String raiseSizesString);
    void setIPTurn(boolean addAllIn, boolean dont3Bet, String betSizesString, String raiseSizesString);
    void setIPRiver(boolean addAllIn, boolean dont3Bet, String betSizesString, String raiseSizesString);

    void setOOPFlop(boolean addAllIn, String donkSizesString, String raiseSizesString);
    void setOOPTurn(boolean addAllIn, String betSizesString, String raiseSizesString, String donkSizesString);
    void setOOPRiver(boolean addAllIn, String betSizesString, String raiseSizesString, String donkSizesString);

    void buildTree();
    ArrayList<String> getAllInLeaves();
    void disconnect();
}
