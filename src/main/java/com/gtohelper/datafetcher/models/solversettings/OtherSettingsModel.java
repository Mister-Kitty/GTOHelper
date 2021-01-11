package com.gtohelper.datafetcher.models.solversettings;


import com.gtohelper.utility.SaveFileHelper;
import com.gtohelper.utility.Saveable;

import java.util.HashMap;

public class OtherSettingsModel extends Saveable {
    public OtherSettingsModel(SaveFileHelper saveHelper) {
        super(saveHelper, "OtherSettings");
    }

    @Override
    public HashMap<String, String> getDefaultValues() {
        HashMap<String, String> values = new HashMap<>();
        values.put("solverLocation", "");
        values.put("viewerLocation", "");
        values.put("rakeLocation", "DefaultRakeFile.csv");
        return values;
    }

}
