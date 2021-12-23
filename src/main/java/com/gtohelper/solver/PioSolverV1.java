package com.gtohelper.solver;

import com.gtohelper.utility.Logger;
import com.gtohelper.utility.Popups;

import java.io.*;

/*
      This inheritance tree with v1 and v2 really could be better...
      Maybe move most of this into a SolverBase.java?
      That way they could easily have their own matching test files
 */
public class PioSolverV1 extends PioSolverBase implements ISolver {

    @Override
    public void connectAndInitToSolver(String solverLocation) throws IOException {
        connect(solverLocation);

        // Eat and log the banner.
        readNLinesFromInput(4);

        // Default init commands that Pio wants for V1
        writeToOutput("set_threads 0");
        readNLinesFromInput(1);

        writeToOutput("set_recalc_accuracy 0.0025 0.001 0.0005");
        readNLinesFromInput(1);

        writeToOutput("set_info_freq 2");
        readNLinesFromInput(1);
    }

    @Override
    public void setRange(String position, String range) throws IOException {
        writeToOutput("set_range " + position + " " + range);
        readNLinesFromInput(1);
    }

    @Override
    public void setBoard(String board) throws IOException {
        writeToOutput("set_board " + board);
        readNLinesFromInput(1);
    }

    @Override
    public void setEffectiveStack(int stack) throws IOException {
        writeToOutput("set_eff_stack " + stack);
        readNLinesFromInput(1);
    }

    @Override
    public void setRake(float percent, float chipCap) throws IOException {
        writeToOutput("set_rake " + percent + " " + chipCap);
        readNLinesFromInput(1);
    }

    @Override
    public void setIsomorphism(boolean flop, boolean turn) throws IOException {
        int flopIso, turnIso;
        if(flop) flopIso = 1; else flopIso = 0;
        if(turn) turnIso = 1; else turnIso = 0;

        writeToOutput("set_isomorphism " + flopIso + " " + turnIso);
        readNLinesFromInput(1);
    }

    @Override
    public void setPotAndAccuracy(int oopInvestment, int ipInvestment, int pot, float chips) throws IOException {
        writeToOutput("set_pot " + oopInvestment + " " + ipInvestment + " " + pot);
        readNLinesFromInput(1);

        writeToOutput("set_accuracy " + chips);
        readNLinesFromInput(1);
    }

    @Override
    public void clearLines() throws IOException {
        writeToOutput("clear_lines");
        readNLinesFromInput(1);
    }

    @Override
    public void go() throws IOException {
        writeToOutput("go");
        readNLinesFromInput(1);
    }

    @Override
    public void stop() throws IOException {
        writeToOutput("stop");
        readFromInputUntil("stop ok!");
    }


    @Override
    public String waitForSolve() throws IOException {
        return readFromInputUntil("SOLVER: stopped");
    }

    @Override
    public String buildTree() throws IOException {
        writeToOutput("build_tree");
        return readNLinesFromInput(1);
    }

    public String runCustomCommand(String command) throws IOException {
        writeToOutput(command);
        return readNLinesFromInput(1);
    }

    public String runCustomCommandUntilEnd(String command) throws IOException {
        writeToOutput(command);
        return readFromInputUntilEND();
    }

    @Override
    public String addLine(String line) throws IOException {
        writeToOutput("add_line " + line);
        return readNLinesFromInput(1);
    }

    @Override
    public String getEstimateSchematicTree() throws IOException {
        writeToOutput("estimate_schematic_tree");
        return readNLinesFromInput(1);
    }

    @Override
    public String getShowMemory() throws IOException {
        writeToOutput("show_memory");
        return readFromInputUntilEND();
    }

    @Override
    public String getCalcResults() throws IOException {
        writeToOutput("calc_results");
        return readFromInputUntilEND();
    }

    @Override
    public void dumpTree(String saveLocation, String options) throws IOException {
        writeToOutput("dump_tree " + saveLocation + " " + options);
        readNLinesFromInput(1);
    }

    @Override
    public void loadTree(String saveLocation) throws IOException {
        writeToOutput("load_tree " + saveLocation);
        readNLinesFromInput(1);
    }

}
