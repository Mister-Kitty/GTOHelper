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
    void setIsomorphism(boolean flop, boolean turn) throws IOException;
    void setPotAndAccuracy(int oopInvestment, int ipInvestment, int pot, float chips) throws IOException;
    void setIPFlop(boolean addAllIn, boolean dont3Bet, String betSizesString, String raiseSizesString);
    void setIPTurn(boolean addAllIn, boolean dont3Bet, String betSizesString, String raiseSizesString);
    void setIPRiver(boolean addAllIn, boolean dont3Bet, String betSizesString, String raiseSizesString);
    void setOOPFlop(boolean addAllIn, String cbetSizesString, String raiseSizesString, String donkSizesString);
    void setOOPTurn(boolean addAllIn, String betSizesString, String raiseSizesString, String donkSizesString);
    void setOOPRiver(boolean addAllIn, String betSizesString, String raiseSizesString, String donkSizesString);

    void clearLines() throws IOException;
    void buildTree();
    String setBuiltTreeAsActive() throws IOException; // aka add_lines function


    String getEstimateSchematicTree() throws IOException;
    String getShowMemory() throws IOException;
    String getCalcResults() throws IOException;

    void go() throws IOException;
    void stop() throws IOException;
    void shutdown() throws IOException;

    void waitForReady() throws IOException, InterruptedException;
    String waitForSolve() throws IOException;

    void dumpTree(String saveLocation, String options) throws IOException;
    void disconnect();

    // debug commands
    String runCustomCommand(String command) throws IOException;
    String runCustomCommandUntilEnd(String command) throws IOException;
    ArrayList<String> getAllInLeaves();

}
