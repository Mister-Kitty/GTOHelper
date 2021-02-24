package com.gtohelper.utility;

import com.gtohelper.domain.GlobalSolverSettings;
import com.gtohelper.domain.SolveTask;
import com.gtohelper.domain.SolverOutput;
import com.gtohelper.domain.Work;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Optional;

/*
    Note!: As per my arbitrary convention, all public functions should take GlobalSolverSetting objects instead of direct folders.

    P.S. work.location needs to be saved by us.
 */
public class StateManager {

    public static File createWorkFolder(Work work, GlobalSolverSettings solverSettings) {
        File rootResultsDirectory = new File(solverSettings.getSolveResultsFolder());
        String finalFolderAddress = rootResultsDirectory.getAbsolutePath() + "\\" + work.getWorkSettings().getName();

        try {
            Path result = Files.createDirectory(Paths.get(finalFolderAddress));
            return result.toFile();
        } catch (FileAlreadyExistsException e) {
            // If the folder exists but it's empty, then we're okay.
            File folder = new File(finalFolderAddress);
            if(folder.listFiles().length == 0)
                return folder;

            Popups.showError(String.format("Work folder %s already exists, and is non-empty.", finalFolderAddress));
            Logger.log(e);
        } catch (NoSuchFileException e) {
            Popups.showError(String.format("Solver results directory %s does not exist.", rootResultsDirectory.getAbsolutePath()));
            Logger.log(e);
        } catch (IOException e) {
            Popups.showError(String.format("IOException while trying to create %s.", rootResultsDirectory.getAbsolutePath()));
            Logger.log(e);
        }

        return null;
    }

    public static boolean saveNewWorkObject(Work work, GlobalSolverSettings solverSettings) {
        File rootResultsDirectory = new File(solverSettings.getSolveResultsFolder());

  //      Path directoryPath = Paths.get()

        boolean success = saveWorkObject(work, rootResultsDirectory, work.getWorkSettings().getName() + ".gto");
 //       if(success)
  //          work.setLocation();
        return success;
    }

    public static boolean saveExistingWorkObject(Work work, GlobalSolverSettings solverSettings) {
        File saveFolder = new File(solverSettings.getWorkResultsFolder(work));

        String oldFileName = work.getWorkSettings().getName() + ".gto";
        String newFileName = work.getWorkSettings().getName() + ".gto.new";

        boolean newFileSuccess = saveWorkObject(work, saveFolder, newFileName);
        if(newFileSuccess) {
            Path newWorkItem = Paths.get(saveFolder.getAbsolutePath() + "\\" + newFileName);
            Path oldWorkItem = Paths.get(saveFolder.getAbsolutePath() + "\\" + oldFileName);

            try {
                Files.move(newWorkItem, oldWorkItem, StandardCopyOption.ATOMIC_MOVE);
                Logger.log("Successfully wrote work " + work.toString() + "'s data file to disk");
                return true;
            } catch (AtomicMoveNotSupportedException e) {
                try {
                    Files.delete(oldWorkItem);
                    Files.move(newWorkItem, oldWorkItem);
                    Logger.log("Successfully wrote work " + work.toString() + "'s data file to disk");
                    return true;
                } catch (IOException a) {
                    Logger.log(a);
                }
            } catch (IOException e) {
                Logger.log(e);
            }
        }

        return false;
    }

    private static boolean saveWorkObject(Work work, File saveFolder, String fileName) {
        String outputLocation = saveFolder.getAbsolutePath() + "\\" + fileName;
        FileOutputStream fileOutputStream;
        ObjectOutputStream out;

        try {
            fileOutputStream = new FileOutputStream(outputLocation);
        } catch (FileNotFoundException e) {
            Logger.log(e);
            return false;
        }

        try {
            out = new ObjectOutputStream(fileOutputStream);
            out.writeObject(work);
            out.flush();
            out.close();
        } catch (IOException e) {
            Logger.log(e);
            return false;
        }

        Logger.log("Successfully wrote work \"" + work + "\" to disk");
        return true;
    }

    public static ArrayList<Work> readAllWorkObjectFiles(GlobalSolverSettings solverSettings) {
        ArrayList<Work> results = new ArrayList<>();
        File rootDirectory = new File(solverSettings.getSolveResultsFolder());

        if(!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            Popups.showError(String.format("Specified solver results directory %s either does not exist " +
                    "or is not a folder.", solverSettings.getSolveResultsFolder()));
            return null;
        }

        for(File solveFolder : rootDirectory.listFiles(pathname -> pathname.isDirectory() == true)) {
            String matchingGtoFileName = solveFolder.getAbsolutePath() + "\\" + solveFolder.getName() + ".gto";
            File matchingGtoFile = new File(matchingGtoFileName);

            if(matchingGtoFile.exists())
                results.add(readWorkObjectFile(matchingGtoFile));
        }

        return results;
    }

    private static Work readWorkObjectFile(File file) {
        FileInputStream fileInputStream;
        ObjectInputStream in;

        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Logger.log("File Not Found exception while trying to read work data from\"" + file.getPath() + "\" .");
            Logger.log(e.getMessage());
            return null;
        }

        try {
            in = new ObjectInputStream(fileInputStream);
            Work work = (Work) in.readObject();
            in.close();
            fileInputStream.close();

            fillFoundSolveFilesForWork(work, file.getParentFile());

            return work;
        } catch (IOException e) {
            Logger.log("File read error trying to get Work data from \"" + file.getPath() + "\" .");
            Logger.log(e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            Logger.log(e.getMessage());
            return null;
        }
    }

    public static void deleteWorkFileFromDisk(Work work) {
  //      String fileName = work.getWorkSettings().getName() + ".gto";
  //      String saveFolderName = solverSettings.getWorkResultsFolder(work);
  //      Path workItemPath = Paths.get(saveFolder.getAbsolutePath() + "\\" + oldFileName);
    }

    private static void fillFoundSolveFilesForWork(Work work, File workFolder) {
        for(SolveTask solve : work.getTasks()) {
            int handId = solve.getHandData().id_hand;

            // note that we only look for leading handId. We allow the rest of the file name open for future change.
            try {
                Optional<Path> result = Files.find(workFolder.toPath(), 1, (path, basicFileAttributes) ->
                        path.toFile().getName().matches(String.format("%d -.*.cfr", handId))).findFirst();

                if(result.isPresent() && solve.getSolveResults() != null) {
                    solve.getSolveResults().solveFileName = result.get().toAbsolutePath().toString();
                } else if (result.isPresent()) {
                    // Solve Results is null.
                    // Maybe we've copied in existing results for the work?
                    // Or maybe an error writing back the .gto file for this work after the dump was successful?
                    SolverOutput results = new SolverOutput();
                    results.solveFileName = result.get().toAbsolutePath().toString();

                    solve.saveSolveResults(results);
                    solve.setSolveState(SolveTask.SolveTaskState.CFG_FOUND);

                    Logger.log(String.format("Found solve file %s which matches a hand to be solved by work %s.\n" +
                            "However, solve metadata/statistics were not found in work. Assuming file is valid and solving stats for hand.",
                            solve.getSolveResults().solveFileName, work.toString()));

                }

            } catch (IOException e) {
                Logger.log(String.format("IO error while trying to look for finished CFR files for work %s in folder %s",
                        work.toString(), workFolder.getAbsolutePath()));
                Logger.log(e);
            }
        }
    }
}
