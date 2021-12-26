package com.gtohelper.solver;

import java.io.IOException;

/*
      This inheritance tree with v1 and v2 really could be better...
      Maybe move most of this into a SolverBase.java?
      That way they could easily have their own matching test files
 */
public class PioSolverV2 extends PioSolverBase implements ISolver {
    @Override
    public void connectAndInitToSolver(String solverLocation) throws IOException {
        connect(solverLocation);

        // Eat and log the banner.
        readNLinesFromInput(2);

        writeToOutput("set_end_string END");
        readFromInputUntilEND();

        writeToOutput("set_threads 0");
        readFromInputUntilEND();

        writeToOutput("set_recalc_accuracy 0.0025 0.001 0.0005");
        readFromInputUntilEND();

        writeToOutput("set_info_freq 2");
        readFromInputUntilEND();

        writeToOutput("set_info_freq 2");
        readFromInputUntilEND();

        writeToOutput("set_info_freq 2");
        readFromInputUntilEND();
    }

    @Override
    public void setRange(String position, String range) throws IOException {
        writeToOutput("set_range " + position + " " + range);
        readFromInputUntilEND();
    }

    @Override
    public void setBoard(String board) throws IOException {
        String noSpacesBoard = board.replaceAll(" ", "");

        writeToOutput("set_board " + noSpacesBoard);
        readFromInputUntilEND();
    }

    @Override
    public void setEffectiveStack(int stack) throws IOException {
        writeToOutput("set_eff_stack " + stack);
        readFromInputUntilEND();
    }

    @Override
    public void setRake(float percent, float chipCap) throws IOException {
        writeToOutput("set_rake " + percent + " " + chipCap);
        readFromInputUntilEND();
    }

    @Override
    public void setIsomorphism(boolean flop, boolean turn) throws IOException {
        int flopIso, turnIso;
        if(flop) flopIso = 1; else flopIso = 0;
        if(turn) turnIso = 1; else turnIso = 0;

        writeToOutput("set_isomorphism " + flopIso + " " + turnIso);
        readFromInputUntilEND();
    }

    @Override
    public void setPotAndAccuracy(int oopInvestment, int ipInvestment, int pot, float chips) throws IOException {
        writeToOutput("set_pot " + oopInvestment + " " + ipInvestment + " " + pot);
        readFromInputUntilEND();

        writeToOutput("set_accuracy " + chips);
        readFromInputUntilEND();
    }

    @Override
    public void clearLines() throws IOException {
        writeToOutput("clear_lines");
        readFromInputUntilEND();
    }

    @Override
    public void go() throws IOException {
        writeToOutput("go");
        readFromInputUntilEND();
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
        return readFromInputUntilEND();
    }

    public String runCustomCommand(String command) throws IOException {
        writeToOutput(command);
        return readFromInputUntilEND();
    }

    public String runCustomCommandUntilEnd(String command) throws IOException {
        writeToOutput(command);
        return readFromInputUntilEND();
    }

    @Override
    public String addLine(String line) throws IOException {
        writeToOutput("add_line " + line);
        return readFromInputUntilEND();
    }

    @Override
    public String getEstimateSchematicTree() throws IOException {
        writeToOutput("estimate_schematic_tree");
        return readFromInputUntilEND();
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
        readFromInputUntilEND();
    }

    @Override
    public void loadTree(String saveLocation) throws IOException {
        writeToOutput("load_tree " + saveLocation);
        readFromInputUntilEND();
    }

}
