package com.gtohelper.solver;

import java.io.*;

public class PioSolver implements ISolver {

    private Process process;
    private BufferedReader input;
    private BufferedWriter output;

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
    }

    @Override
    public void setRange(String position, String range) throws IOException {
        writeToOutput("set_range " + position + " " + range);
        readNLinesFromInput(1);
    }

    public void setBoard(String board) throws IOException {
        writeToOutput("set_board " + board);
        readNLinesFromInput(1);
    }

    public void setEffectiveStack(int stack) throws IOException {
        writeToOutput("set_eff_stack " + stack);
        readNLinesFromInput(1);
    }

    public void setIsomorphism(int flop, int turn) throws IOException {
        writeToOutput("set_isomorphism " + flop + " " + turn);
        readNLinesFromInput(1);
    }

    public void setPot(int oopInvestment, int ipInvestment, int pot) throws IOException {
        writeToOutput("set_pot " + oopInvestment + " " + ipInvestment + " " + pot);
        readNLinesFromInput(1);
    }

    public void clearLines() throws IOException {
        writeToOutput("clear_lines");
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
            results += currentLine;
            numLines--;
        }
        System.out.println(results);
        return results;
    }


}
