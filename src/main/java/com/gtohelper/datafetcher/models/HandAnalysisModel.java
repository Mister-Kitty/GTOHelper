package com.gtohelper.datafetcher.models;

import com.gtohelper.PT4DataManager.PT4HandDataDM;
import com.gtohelper.PT4DataManager.PT4HandSummaryDM;
import com.gtohelper.PT4DataManager.PT4LookupDM;
import com.gtohelper.database.Database;
import com.gtohelper.datamanager.IHandDataDM;
import com.gtohelper.datamanager.IHandSummaryDM;
import com.gtohelper.datamanager.ILookupDM;
import com.gtohelper.domain.HandData;
import com.gtohelper.domain.Tag;
import com.gtohelper.utility.SaveFileHelper;
import com.gtohelper.utility.Saveable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class HandAnalysisModel extends Saveable {
    public HandAnalysisModel(SaveFileHelper saveHelper) {
        super(saveHelper, "HandAnalysis");
    }

    public ArrayList<Tag> getHandTags() throws SQLException {
        try (Connection con = Database.getConnection();) {

            ILookupDM lookupDM = new PT4LookupDM(con);
            return lookupDM.getsTagsByType('H');
        }
    }

    public ArrayList<HandData> getHandSummariesByTag(int tagId, int playerId) throws SQLException {
        try (Connection con = Database.getConnection();) {

            IHandDataDM handSummaryDM = new PT4HandDataDM(con);
            return handSummaryDM.getHandDataByTag(tagId, playerId);
        }
    }

    @Override
    public HashMap<String, String> getDefaultValues() {
        HashMap<String, String> values = new HashMap<>();

        return values;
    }
}
