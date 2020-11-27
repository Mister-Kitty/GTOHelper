package com.gtohelper.datafetcher.models.solversettings;

import com.gtohelper.utility.SaveFileHelper;
import com.gtohelper.utility.Saveable;

import java.util.HashMap;
public class SolverSettings extends Saveable {

    public SolverSettings(SaveFileHelper saveHelper) {
        super(saveHelper, "SolverSettings");
    }







    @Override
    public HashMap<String, String> getDefaultValues() {
        HashMap<String, String> values = new HashMap<>();
        values.put("rangeFolderLocation", "C:\\PioSolver Edge\\Ranges");
        return values;
    }


}
