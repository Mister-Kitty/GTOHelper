package com.gtohelper.PT4DataManager;

import com.gtohelper.datamanager.DataManagerBase;
import com.gtohelper.datamanager.IHandPlayerStatisticsDM;
import com.gtohelper.datamanager.IHandSummaryDM;
import com.gtohelper.domain.HandPlayerStatistics;
import com.gtohelper.domain.HandSummary;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class PT4HandPlayerStatisticsDM extends DataManagerBase implements IHandPlayerStatisticsDM {

    public PT4HandPlayerStatisticsDM(Connection con) {
        super(con);
    }

    public ArrayList<HandPlayerStatistics> getHandSummariesByTag(int tagId) throws SQLException {
        String sql = "";
        ArrayList<HandPlayerStatistics> hands = new ArrayList<HandPlayerStatistics>();

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                HandPlayerStatistics hand = mapHandSummary(rs);
                hands.add(hand);
            }
        }

        return hands;
    }



    private HandPlayerStatistics mapHandSummary(ResultSet rs) throws SQLException {
        HandPlayerStatistics hand = new HandPlayerStatistics();

        return hand;
    }


}
