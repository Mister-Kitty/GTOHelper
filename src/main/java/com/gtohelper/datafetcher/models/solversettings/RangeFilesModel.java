package com.gtohelper.datafetcher.models.solversettings;

import com.gtohelper.datafetcher.controllers.solversettings.RangeFilesController.ActionPosition;
import com.gtohelper.domain.Ranges;
import com.gtohelper.utility.SaveFileHelper;
import com.gtohelper.utility.Saveable;

import java.io.File;
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

    public Ranges loadRangeFiles(HashMap<ActionPosition, File> actionToRangeFileMap) {
        Ranges ranges = new Ranges();

        actionToRangeFileMap.forEach((k, v) -> {



        });


        return ranges;
    }





}
