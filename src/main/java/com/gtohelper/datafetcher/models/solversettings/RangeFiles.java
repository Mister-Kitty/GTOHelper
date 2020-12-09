package com.gtohelper.datafetcher.models.solversettings;

import com.gtohelper.utility.SaveFileHelper;
import com.gtohelper.utility.Saveable;

import java.util.HashMap;

public class RangeFiles extends Saveable {
    public RangeFiles(SaveFileHelper saveHelper) {
        super(saveHelper, "RangeFiles");
    }


    @Override
    public HashMap<String, String> getDefaultValues() {
        HashMap<String, String> values = new HashMap<>();
        values.put("rangeFolderLocation", "");
        return values;
    }

}
