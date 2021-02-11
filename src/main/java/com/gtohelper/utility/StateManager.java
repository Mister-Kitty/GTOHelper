package com.gtohelper.utility;

import com.gtohelper.domain.GlobalSolverSettings;
import com.gtohelper.domain.Work;

import java.io.*;
import java.nio.file.*;

public class StateManager {

    public static File createWorkFolder(Work work, GlobalSolverSettings solverSettings) {
        File rootResultsDirectory = new File(solverSettings.getSolveResultsFolder());
        String finalFolderAddress = rootResultsDirectory.getAbsolutePath() + "\\" + work.getWorkSettings().getName();

        try {
            Path result = Files.createDirectory(Paths.get(finalFolderAddress));
            return result.toFile();
        } catch (FileAlreadyExistsException e) {
            Popups.showError(String.format("Work folder %s already exists", finalFolderAddress));
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

    public static void deleteEmptyWorkFolder(File folder) {

    }

    public static boolean saveWorkObject(Work work, File saveFolder) {
        String outputLocation = saveFolder.getAbsolutePath() + "\\" + work.getWorkSettings().getName() + ".gto";
        FileOutputStream fileOutputStream;
        ObjectOutputStream out;

        try {
            fileOutputStream = new FileOutputStream(outputLocation);
        } catch (FileNotFoundException e) {
            Popups.showError("File Not Found exception while trying to write work \"" + work + "\" data to disk.");
            Logger.log(e.getMessage());
            return false;
        }

        try {
            out = new ObjectOutputStream(fileOutputStream);
            out.writeObject(work);
            out.flush();
            out.close();
        } catch (IOException e) {
            Popups.showError("Write error writing work \"" + work + "\" data to disk. \n" +
                    "Maybe out process doesn't have write permission? Check error log.");
            Logger.log(e.getMessage());
            return false;
        }

        Logger.log("Successfully wrote work \"" + work + "\" to disk");
        return true;
    }

    public Work readWorkObject(File file) {
        FileInputStream fileInputStream;
        ObjectInputStream in;

        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Popups.showError("File Not Found exception while trying to read work data from\"" + file.getPath() + "\" .");
            Logger.log(e.getMessage());
            return null;
        }

        try {
            in = new ObjectInputStream(fileInputStream);
            Work work = (Work) in.readObject();
            in.close();
            fileInputStream.close();
            return work;
        } catch (IOException e) {
            Popups.showError("File read error trying to get Work data from \"" + file.getPath() + "\" .");
            Logger.log(e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            Logger.log(e.getMessage());
            return null;
        }
    }





}
