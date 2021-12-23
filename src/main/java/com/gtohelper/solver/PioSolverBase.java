package com.gtohelper.solver;

import com.gtohelper.utility.Logger;
import com.gtohelper.utility.Popups;

import java.io.*;

public abstract class PioSolverBase implements ISolver {

    protected Process process;
    protected BufferedReader input;
    protected BufferedWriter output;

    protected void connect(String solverLocation) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(solverLocation);
        pb.redirectErrorStream(true);
        String directory = solverLocation.substring(0, solverLocation.lastIndexOf("\\") + 1);
        pb.directory(new File(directory));
        process = pb.start();

        output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        input = new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    @Override
    public void shutdown() throws IOException {
        writeToOutput("exit");
    }

    @Override
    public void waitForReady() throws IOException, InterruptedException {
        int count = 0;
        while(true) {
            writeToOutput("is_ready");
            String result = readNLinesFromInput(1);
            if(result.trim().equals("is_ready ok!"))
                return;
            else {
                if(count == 12)
                    Popups.showWarning("Warning: Piosolver has refused new work for over a minute. The process may not be responding.");

                Logger.log(Logger.Channel.SOLVER, "is_ready returned false. Will try again after 5 seconds.");
                Thread.sleep(5000);
                count++;
            }
        }
    }

    protected String readNLinesFromInput(int numLines) throws IOException {
        // Yes, this is terrible and should be rewritten

        String results = "";
        String currentLine;
        while ((numLines > 0) && (currentLine = input.readLine()) != null) {
            results += currentLine + "\n";
            numLines--;
        }
        //       Logger.log(Logger.Channel.PIO, results.trim());
        System.out.println(results.trim());
        return results.trim();
    }

    protected String readFromInputUntilEND() throws IOException {
        return readFromInputUntil("END");
    }

    protected String readFromInputUntil(String terminalPrefix) throws IOException {
        String results = "";
        String currentLine;
        while ((currentLine = input.readLine()) != null) {
            System.out.println(currentLine.trim());
            //           Logger.log(Logger.Channel.PIO, currentLine.trim());
            if(currentLine.trim().startsWith(terminalPrefix))
                break;
            results += currentLine  + "\n";
        }
        return results.trim();
    }

    @Override
    public void disconnect() {
        try { input.close(); } catch (IOException e) {}
        try { output.close(); } catch (IOException e) {}
        process.destroy();
    }

    protected void writeToOutput(String command) throws IOException {
        output.write(command);
        output.newLine();
        output.flush();
        //      Logger.log(Logger.Channel.PIO, command);
        System.out.println(command);
    }
}
