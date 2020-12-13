package com.gtohelper.datafetcher.models.solversettings;

import com.gtohelper.utility.SaveFileHelper;
import com.gtohelper.utility.Saveable;

import java.util.HashMap;
public class SolverSettingsModel extends Saveable {

    public SolverSettingsModel(SaveFileHelper saveHelper) {
        super(saveHelper, "SolverSettings");
    }


    @Override
    public HashMap<String, String> getDefaultValues() {
        HashMap<String, String> values = new HashMap<>();


        return values;
    }
}
