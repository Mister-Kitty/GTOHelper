package com.gtohelper.datafetcher.controllers.solversettings;

import com.gtohelper.datafetcher.models.solversettings.BetSettings;
import com.gtohelper.utility.SaveFileHelper;
import javafx.fxml.FXML;

public class BetSettingsController {


    BetSettings betSettings;

    public void loadModel(SaveFileHelper saveHelper) {
        betSettings = new BetSettings(saveHelper);
        loadFieldsFromModel();
    }

    @FXML
    private void initialize() {


    }



    void loadFieldsFromModel() {



    }



}
