package com.gtohelper.PT4DataManager;

import com.gtohelper.datamanager.DataManagerBase;
import com.gtohelper.datamanager.IHandDataDM;
import com.gtohelper.domain.HandData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class PT4HandDataDM extends DataManagerBase implements IHandDataDM {
    public PT4HandDataDM(Connection connection) {
        super(connection);
    }

    public ArrayList<HandData> getHandDataByTag(int tagId, int playerId) throws SQLException {
        String sql = String.format( "select summary.id_hand, summary.date_played, summary.amt_pot, summary.card_1, \n" +
                "summary.card_2, summary.card_3, summary.card_4, summary.card_5, stats.id_player, stats.holecard_1, stats.holecard_2\n" +
                "from cash_hand_summary as summary\n" +
                "inner join tags\n" +
                "on tags.id_x = summary.id_hand\n" +
                "and tags.id_tag = %d\n" +
                "inner join cash_hand_player_statistics as stats\n" +
                "on summary.id_hand = stats.id_hand\n" +
                "and stats.id_player = %d\n" +
                "order by summary.date_played DESC", tagId, playerId);

        ArrayList<HandData> hands = new ArrayList<HandData>();

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                HandData hand = mapHandData(rs);
                hands.add(hand);
            }
        }

        return hands;
    }

    private HandData mapHandData(ResultSet rs) throws SQLException {
        HandData hand = new HandData();

        hand.id_hand = rs.getInt("id_hand");
        hand.date_played = rs.getTimestamp("date_played");
        hand.amt_pot = rs.getFloat("amt_pot");
        hand.card_1 = rs.getShort("card_1");
        hand.card_2 = rs.getShort("card_2");
        hand.card_3 = rs.getShort("card_3");
        hand.card_4 = rs.getShort("card_4");
        hand.card_5 = rs.getShort("card_5");

        hand.id_player = rs.getInt("id_player");
        hand.holecard_1 = rs.getShort("holecard_1");
        hand.holecard_2 = rs.getShort("holecard_2");

        return hand;
    }
}
