package com.gtohelper.solver;

import com.gtohelper.domain.BettingOptions;

import java.io.*;
import java.util.ArrayList;

public class PioSolver implements ISolver {

    private Process process;
    private BufferedReader input;
    private BufferedWriter output;

    private GameTree tree;
    private BettingOptions currentGame;

    private int pot, effectiveStack;

    @Override
    public void connectAndInit(String pioLocation) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(pioLocation);
        pb.redirectErrorStream(true);
        String directory = pioLocation.substring(0, pioLocation.lastIndexOf("\\") + 1);
        pb.directory(new File(directory));
        process = pb.start();

        output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        input = new BufferedReader(new InputStreamReader(process.getInputStream()));

        // Eat and log the banner.
        readNLinesFromInput(4);

        // Default init commands that Pio wants
        writeToOutput("set_threads 0");
        readNLinesFromInput(1);

        writeToOutput("set_recalc_accuracy 0.0025 0.001 0.0005");
        readNLinesFromInput(1);

        currentGame = new BettingOptions("PioSolver");
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
        effectiveStack = stack;
        readNLinesFromInput(1);
    }

    @Override
    public void setGameTreeOptions(int allInThresholdPercent, int allInOnlyIfLessThanNPercent,
                                   boolean forceOOPBet, boolean forceOOPCheckIPBet) {
        currentGame.options.allInThresholdPercent = allInThresholdPercent;
        currentGame.options.allInOnlyIfLessThanNPercent = allInOnlyIfLessThanNPercent;

        // TODO: Add logging in this instance.
        if(forceOOPBet && forceOOPCheckIPBet)
            return;
        currentGame.options.forceOOPBet = forceOOPBet;
        currentGame.options.forceOOPCheckIPBet = forceOOPCheckIPBet;
    }

    @Override
    public void setIPFlop(boolean addAllIn, boolean dont3Bet, String betSizesString, String raiseSizesString) {
        currentGame.IPFlop.setActionData(addAllIn, dont3Bet, betSizesString, raiseSizesString);
    }

    @Override
    public void setIPTurn(boolean addAllIn, boolean dont3Bet, String betSizesString, String raiseSizesString) {
        currentGame.IPTurn.setActionData(addAllIn, dont3Bet, betSizesString, raiseSizesString);
    }

    @Override
    public void setIPRiver(boolean addAllIn, boolean dont3Bet, String betSizesString, String raiseSizesString) {
        currentGame.IPRiver.setActionData(addAllIn, dont3Bet, betSizesString, raiseSizesString);
    }

    @Override
    public void setOOPFlop(boolean addAllIn, String cbetSizesString, String raiseSizesString, String donkSizesString) {
        currentGame.OOPFlop.setActionData(addAllIn, cbetSizesString, raiseSizesString, donkSizesString);
    }

    @Override
    public void setOOPTurn(boolean addAllIn, String betSizesString, String raiseSizesString, String donkSizesString) {
        currentGame.OOPTurn.setActionData(addAllIn, betSizesString, raiseSizesString, donkSizesString);
    }

    @Override
    public void setOOPRiver(boolean addAllIn, String betSizesString, String raiseSizesString, String donkSizesString) {
        currentGame.OOPRiver.setActionData(addAllIn, betSizesString, raiseSizesString, donkSizesString);
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
        this.pot = pot;
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
    public void buildTree() {
        tree = new GameTree();
        tree.buildGameTree(currentGame, pot, effectiveStack);
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
    public void shutdown() throws IOException {
        writeToOutput("exit");
    }

    @Override
    public void waitForReady() throws IOException, InterruptedException {
        while(true) {
            writeToOutput("is_ready");
            String result = readNLinesFromInput(1);
            if(result.trim().equals("is_ready ok!"))
                return;
            else
                Thread.sleep(5000);
        }
    }

    @Override
    public String waitForSolve() throws IOException {
        return readFromInputUntil("SOLVER: stopped");
    }

    @Override
    public String setBuiltTreeAsActive() throws IOException {
        ArrayList<String> leaves = tree.getAllInLeaves();
        for(String leaf : leaves) {
            setAddLine(leaf);
        }

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

    private String setAddLine(String line) throws IOException {
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
    public ArrayList<String> getAllInLeaves() {
        return tree.getAllInLeaves();
    }

    @Override
    public void dumpTree(String saveLocation, String options) throws IOException {
        writeToOutput("dump_tree " + saveLocation + " " + options);
        readNLinesFromInput(1);
    }

    @Override
    public void disconnect() {
        try { input.close(); } catch (IOException e) {}
        try { output.close(); } catch (IOException e) {}
        process.destroy();
    }

    private void writeToOutput(String command) throws IOException {
        output.write(command);
        output.newLine();
        output.flush();
        System.out.println(command);
    }

    private String readNLinesFromInput(int numLines) throws IOException {
        String results = "";
        String currentLine;
        while ((numLines > 0) && (currentLine = input.readLine()) != null) {
            results += currentLine + "\n";
            numLines--;
        }
        System.out.println(results.trim());
        return results.trim();
    }

    private String readFromInputUntilEND() throws IOException {
        return readFromInputUntil("END");
    }

    private String readFromInputUntil(String terminalPrefix) throws IOException {
        String results = "";
        String currentLine;
        while ((currentLine = input.readLine()) != null) {
            System.out.println(currentLine.trim());
            if(currentLine.trim().startsWith(terminalPrefix))
                break;
            results += currentLine  + "\n";
        }
        return results.trim();
    }
}
