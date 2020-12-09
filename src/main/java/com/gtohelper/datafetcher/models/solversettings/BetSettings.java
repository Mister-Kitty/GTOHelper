package com.gtohelper.datafetcher.models.solversettings;

import com.gtohelper.utility.SaveFileHelper;
import com.gtohelper.utility.Saveable;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.HashMap;

public class BetSettings extends Saveable {
    public BetSettings(SaveFileHelper saveHelper) {
        super(saveHelper, "BetSettings");
    }

    @Override
    public HashMap<String, String> getDefaultValues() {
        HashMap<String, String> values = new HashMap<>();
        values.put("betSettingsNames", "Default");


        return values;
    }
}
