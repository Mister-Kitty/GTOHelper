package com.gtohelper.datafetcher.models.solversettings;

import com.gtohelper.domain.RangeData;
import com.gtohelper.domain.Ranges;
import com.gtohelper.utility.SaveFileHelper;
import com.gtohelper.utility.Saveable;

import java.io.*;
import java.util.HashMap;

public class RangeFilesModel extends Saveable {
    public RangeFilesModel(SaveFileHelper saveHelper) {
        super(saveHelper, "RangeFiles");
    }

    @Override
    public HashMap<String, String> getDefaultValues() {
        HashMap<String, String> values = new HashMap<>();
        values.put("rangeFolderLocation", "");
        return values;
    }

    public Ranges loadRangeFiles(HashMap<String, File> actionToRangeFileMap) {
        Ranges ranges = new Ranges();

        actionToRangeFileMap.forEach((k, v) -> {
            Ranges.ActionAndSeat action = new Ranges.ActionAndSeat(k);
            RangeData data;

            try {
                data = new RangeData(loadFile(v));
            } catch (IOException e) {
               //Todo: log and flag this error
                return;
            }

            ranges.addRangeForAction(action, data);
        });

        ranges.fillEmptyRanges();
        return ranges;
    }

    private String loadFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();

        return new String(data, "UTF-8");
    }
}
