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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class HandAnalysis {

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

}
