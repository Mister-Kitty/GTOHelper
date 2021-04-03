package com.gtohelper.datamanager;

import com.gtohelper.domain.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// HandData is a mix of cash_hand_summary & cash_hand_statistics, as we often need a both for GUI and Solve generation
public interface IHandDataDM {

    ArrayList<HandData> getHandDataByTag(int tagId, int playerId) throws SQLException;
    ArrayList<HandData> getHandDataBySessionBundle(SessionBundle session, int tagId, int playerId) throws SQLException;
    ArrayList<HandData> getHandDataByTaggedHandsInSessions(List<SessionBundle> sessions, int tagId, int playerId) throws SQLException;
    ArrayList<HandData> getHandDataByPositionVsPosition(SeatGroup heroSeatGroup, SeatGroup villainSeatGroup,
                                 Situation sit, LastAction lastAction, HandData.SolvabilityLevel solvability, int playerId) throws SQLException;
}
