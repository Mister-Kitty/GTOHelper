package com.gtohelper.solver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public interface ISolver {
    void connectAndInitToSolver(String solverLocation) throws IOException;

    void setRange(String position, String range) throws IOException;
    void setBoard(String board) throws IOException;
    void setEffectiveStack(int stack) throws IOException;
    void setIsomorphism(boolean flop, boolean turn) throws IOException;
    void setPotAndAccuracy(int oopInvestment, int ipInvestment, int pot, float chips) throws IOException;
    void setRake(float percent, float chipCap) throws IOException;

    void clearLines() throws IOException;
    String addLine(String line) throws IOException;
    String buildTree() throws IOException;

    String getEstimateSchematicTree() throws IOException;
    String getShowMemory() throws IOException;
    String getCalcResults() throws IOException;

    void go() throws IOException;
    void stop() throws IOException;
    void shutdown() throws IOException;

    void waitForReady() throws IOException, InterruptedException;
    String waitForSolve() throws IOException;

    void loadTree(String saveLocation) throws IOException;
    void dumpTree(String saveLocation, String options) throws IOException;
    void disconnect();

    // debug commands
    String runCustomCommand(String command) throws IOException;
    String runCustomCommandUntilEnd(String command) throws IOException;
}
