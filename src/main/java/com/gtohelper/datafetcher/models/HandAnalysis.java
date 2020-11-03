package com.gtohelper.datafetcher.models;

import com.gtohelper.PT4DataManager.PT4HandSummaryDM;
import com.gtohelper.PT4DataManager.PT4LookupDM;
import com.gtohelper.database.Database;
import com.gtohelper.datamanager.IHandSummaryDM;
import com.gtohelper.datamanager.ILookupDM;
import com.gtohelper.domain.HandSummary;
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

    public ArrayList<HandSummary> getHandSummariesByTag(int tagId) throws SQLException {
        try (Connection con = Database.getConnection();) {

            IHandSummaryDM handSummaryDM = new PT4HandSummaryDM(con);
            return handSummaryDM.getHandSummariesByTag(tagId);
        }
    }

}
