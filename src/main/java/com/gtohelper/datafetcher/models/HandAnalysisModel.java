package com.gtohelper.datafetcher.models;

import com.gtohelper.datamanager.ISessionDM;
import com.gtohelper.domain.*;
import com.gtohelper.pt4datamanager.PT4HandDataDM;
import com.gtohelper.pt4datamanager.PT4LookupDM;
import com.gtohelper.database.Database;
import com.gtohelper.datamanager.IHandDataDM;
import com.gtohelper.datamanager.ILookupDM;
import com.gtohelper.pt4datamanager.PT4SessionDM;
import com.gtohelper.utility.SaveFileHelper;
import com.gtohelper.utility.Saveable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HandAnalysisModel extends Saveable {
    public HandAnalysisModel(SaveFileHelper saveHelper) {
        super(saveHelper, "HandAnalysis");
    }

    /*
        Get our Tag and Session objects
     */
    public ArrayList<Tag> getHandTags() throws SQLException {
        try (Connection con = Database.getConnection()) {

            ILookupDM lookupDM = new PT4LookupDM(con);
            return lookupDM.getsTagsByType('H');
        }
    }

    public ArrayList<SessionBundle> getSessionBundles(int siteId, int playerId) throws SQLException {
        try (Connection con = Database.getConnection()) {

            ISessionDM sessionDM = new PT4SessionDM(con);
            return sessionDM.getAllSessionBundles(siteId, playerId);
        }
    }

    /*
        When we have a tag or session query, we use these 3 functions.
        These could be rolled up into one function.... I'm not sure what's a better choice...
        Maybe you're supposed to just pick either and stick with it? Whatever...
     */

    public ArrayList<HandData> getHandDataByTag(int tagId, int playerId) throws SQLException {
        try (Connection con = Database.getConnection()) {

            IHandDataDM handSummaryDM = new PT4HandDataDM(con);
            return handSummaryDM.getHandDataByTag(tagId, playerId);
        }
    }

    public ArrayList<HandData> getHandDataBySessionBundle(SessionBundle session, int tagId, int playerId) throws SQLException {
        try (Connection con = Database.getConnection()) {

            IHandDataDM handSummaryDM = new PT4HandDataDM(con);
            return handSummaryDM.getHandDataBySessionBundle(session, tagId, playerId);
        }
    }

    public ArrayList<HandData> getHandDataByTaggedHandsInSessions(List<SessionBundle> sessions, int tagId, int playerId) throws SQLException {
        try (Connection con = Database.getConnection()) {

            IHandDataDM handSummaryDM = new PT4HandDataDM(con);
            return handSummaryDM.getHandDataByTaggedHandsInSessions(sessions, tagId, playerId);
        }
    }

    public ArrayList<HandData> getHandDataByPositionVsPosition(SeatGroup heroSeatGroup, SeatGroup villainSeatGroup,
                                        Situation sit, LastAction lastAction, HandData.SolvabilityLevel solvability, int playerId) throws SQLException {
        try (Connection con = Database.getConnection()) {

            IHandDataDM handSummaryDM = new PT4HandDataDM(con);
            return handSummaryDM.getHandDataByPositionVsPosition(heroSeatGroup, villainSeatGroup, sit, lastAction, solvability, playerId);
        }
    }

    @Override
    public HashMap<String, String> getDefaultValues() {
        HashMap<String, String> values = new HashMap<>();

        return values;
    }
}
