package com.gtohelper.utility;

import com.gtohelper.domain.GlobalSolverSettings;
import com.gtohelper.domain.SolveTask;
import com.gtohelper.domain.SolverOutput;
import com.gtohelper.domain.Work;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/*
    Note!: As per my arbitrary convention, all public functions should take GlobalSolverSetting objects instead of direct folders.
    We should catch and return true/false instead of throwing IO exceptions in most cases, with readAllWorkObjectFiles being the exception (no pun intended)

    P.S. work.location needs to be saved by us. This is because we allow the SolveResultsFolder to change, and so we must track ourselves.
 */
public class StateManager {

    public static Path createWorkFolder(Work work, GlobalSolverSettings solverSettings) {
        Path rootResultsDirectory = solverSettings.getSolverResultsFolder();
        Path finalFolderAddress = rootResultsDirectory.resolve(work.getWorkSettings().getName());

        if(Files.isDirectory(finalFolderAddress)) {
            long childCount;
            try {
                childCount = Files.list(finalFolderAddress).count();
            } catch (IOException e) {
                String error = String.format("Input/output error while trying to access folder %s.", finalFolderAddress.toString());
                Popups.showError(error);
                return null;
            }

            if(childCount > 0) {
                Popups.showError(String.format("Work folder %s already exists, and is non-empty. Please empty or delete the folder.", finalFolderAddress));
                return null;
            } else {
                return finalFolderAddress;
            }
        }

        try {
            Path result = Files.createDirectory(finalFolderAddress);
            return result;
        } catch (IOException e) {
            Popups.showError(String.format("IOException while trying to create work folder %s.", rootResultsDirectory.toString()));
            Logger.log(e);
            return null;
        }
    }

    public static boolean saveNewWorkObject(Work work, GlobalSolverSettings solverSettings) {
        Path GtoFilePath = solverSettings.getWorkResultsFolder(work).resolve(work.getWorkSettings().getName() + ".gto");

        boolean success = saveWorkObject(work, GtoFilePath);
        if(success)
            work.setSaveFileLocation(GtoFilePath);

        return success;
    }

    // Because the work is pre existing, we don't use the current save location, as it can be modified.
    public static synchronized boolean saveExistingWorkObject(Work work) {
        Path saveFolder = work.getSaveFileLocation().getParent();

        Path oldFile = saveFolder.resolve(work.getWorkSettings().getName() + ".gto");
        Path newFile = saveFolder.resolve(work.getWorkSettings().getName() + ".gto.new");

        boolean newFileSuccess = saveWorkObject(work, newFile);
        if(newFileSuccess) {

            try {
                Files.move(newFile, oldFile, StandardCopyOption.ATOMIC_MOVE);
                Logger.log("Successfully wrote work " + work.toString() + "'s data file to disk");
                return true;
            } catch (AtomicMoveNotSupportedException e) {
                try {
                    Files.delete(oldFile);
                    Files.move(newFile, oldFile);
                    Logger.log("Successfully wrote work " + work.toString() + "'s data file to disk non-atomically");
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

    private static boolean saveWorkObject(Work work, Path saveFilePath) {
        OutputStream fileOutputStream;
        ObjectOutputStream out;

        try {
            fileOutputStream = Files.newOutputStream(saveFilePath);
        } catch (IOException e) {
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

    public static ArrayList<Work> readAllWorkObjectFiles(GlobalSolverSettings solverSettings) throws IOException {
        ArrayList<Work> results = new ArrayList<>();
        AtomicInteger errorOccured = new AtomicInteger(0);
        Path rootDirectory = solverSettings.getSolverResultsFolder();

        if(!Files.exists(rootDirectory) || !Files.isDirectory(rootDirectory)) {
            throw new IOException(String.format("Specified solver results directory %s either does not exist " +
                    "or is not a folder.", solverSettings.getSolverResultsFolder()));
        }

        Files.walk(rootDirectory, 1).filter(Files::isDirectory).forEach(subfolderPath -> {
            Path possibleGtoFilePath = subfolderPath.resolve(subfolderPath.getFileName()+ ".gto");

            if(Files.exists(possibleGtoFilePath)) {
                Work readWorkResults = readWorkObjectFile(possibleGtoFilePath);

                if(readWorkResults != null) {
                    readWorkResults.setSaveFileLocation(possibleGtoFilePath);
                    results.add(readWorkResults);
                } else {
                    errorOccured.incrementAndGet();
                }
            }
        });

        if(errorOccured.get() > 0)
            Popups.showWarning(String.format("Warning! %d work objects failed to load. See logging tab for messages.", errorOccured.get()));

        return results;
    }

    private static Work readWorkObjectFile(Path file) {
        InputStream fileInputStream;
        ObjectInputStream in;

        try {
            fileInputStream = Files.newInputStream(file);
        } catch (IOException e) {
            Logger.log(String.format("Input/output error while trying to read work data from supposed work object %s", file.toString()));
            Logger.log(e.getMessage());
            return null;
        }

        try {
            in = new ObjectInputStream(fileInputStream);
            Work work = (Work) in.readObject();
            in.close();
            fileInputStream.close();

            boolean success = fillFoundSolveFilesForWork(work, file);
            if(!success)
                return null;

            work.resetTaskIndex(); // task index is defined as transient, which aparently will ignore the = -1 default value.
            return work;
        } catch (IOException e) {
            Logger.log("File read error trying to get Work data from \"" + file.toString() + "\" .");
            Logger.log(e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            Logger.log(e.getMessage());
            return null;
        }
    }

    public static boolean recycleElseDeleteWorkFolder(Work work) {
        Path parentFolder = work.getSaveFileLocation().getParent();
        boolean success = recycleFile(parentFolder);
        if(success)
            return true;

        String error = String.format("Failed to move %s to recycle. Delete from disk?", parentFolder.toString());
        boolean choice = Popups.showConfirmation(error);
        if(choice)
            return deleteFile(parentFolder);

        return false;
    }

    public static boolean recycleElseDeleteWorkFile(Work work) {
        boolean success = recycleFile(work.getSaveFileLocation());
        if(success)
            return true;

        String error = String.format("Failed to move %s to recycle. Delete from disk?", work.getSaveFileLocation().toString());
        boolean choice = Popups.showConfirmation(error);
        if(choice)
            return deleteFile(work.getSaveFileLocation());

        return false;
    }

    private static boolean recycleFile(Path file) {
        boolean desktopSupported = Desktop.isDesktopSupported();

        if(desktopSupported) {
            try {
                return Desktop.getDesktop().moveToTrash(file.toFile());
            } catch (UnsupportedOperationException e) {
                // Ignore and allow to fall through
            } catch (Exception e) {
                String error = String.format("Error while trying to move %s to the recycle.", file.toString());
                Logger.log(error);
                Logger.log(e);
                return false;
            }
        }

        return false;
    }

    private static boolean deleteFile(Path file) {
        try {
            Files.delete(file);
            return true;
        }  catch (IOException e) {
            String error = String.format("Error while trying to move %s to the recycle.", file.toString());
            Logger.log(error);
            Logger.log(e);
            return false;
        }
    }

    private static boolean fillFoundSolveFilesForWork(Work work, Path workFile) {
        for(SolveTask solve : work.getTasks()) {
            int handId = solve.getHandData().id_hand;

            // note that we only look for leading handId. We allow the rest of the file name open for future change.
            try {
                Optional<Path> result = Files.find(workFile.getParent(), 1, (path, basicFileAttributes) ->
                        path.toFile().getName().matches(String.format("%d -.*.cfr", handId))).findFirst();

                if(result.isPresent() && solve.getSolveResults() != null) {
                    solve.getSolveResults().solveFile = result.get();
                } else if (result.isPresent()) {
                    // Solve Results is null.
                    // Maybe we've copied in existing results for the work?
                    // Or maybe an error writing back the .gto file for this work after the dump was successful?
                    SolverOutput results = new SolverOutput();
                    results.solveFile = result.get();

                    solve.saveSolveResults(results);
                    solve.setSolveState(SolveTask.SolveTaskState.CFG_FOUND);

                    Logger.log(String.format("Found solve file %s which matches a hand to be solved by work %s.\n" +
                            "However, solve metadata/statistics were not found in work. Process work through queue to load and resolve hand stats.",
                            solve.getSolveResults().solveFile, work.toString()));

                }

            } catch (IOException e) {
                Logger.log(String.format("IO error while trying to look for finished cfr files for work %s.",
                        work.toString()));
                Logger.log(e);
                return false;
            }
        }

        return true;
    }
}
