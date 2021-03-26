package com.gtohelper.solver;

import java.io.*;
import java.nio.file.Path;

public class PioViewer {

    public static void launchViewerForCFG(Path viewerPath, Path cfgPath) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(viewerPath.toString(), "\"" + cfgPath.toAbsolutePath().toString() + "\"");
        pb.redirectErrorStream(true);
        pb.directory(viewerPath.getParent().toFile());
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        Process process = pb.start();
    }
}
