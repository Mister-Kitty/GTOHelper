package com.gtohelper.PT4DataManager;

import com.gtohelper.datamanager.DataManagerBase;
import com.gtohelper.datamanager.IHandDataDM;
import com.gtohelper.domain.HandData;
import com.gtohelper.domain.Player;

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
        String handIdSelectSQL = String.format("select id_x from tags where tags.id_tag = %d", tagId);

        ArrayList<HandData> hands = getHandSummaryData(handIdSelectSQL);
        ArrayList<HandData.PlayerHandData> playerHands = getPlayerHandData(handIdSelectSQL);

        // Both are ordered by id_hand descending. Let's bundle them up
        int handsIndex = 0;
        int playerIndex = 0;
        while(playerIndex < playerHands.size()) {
            HandData currentHand = hands.get(handsIndex);
            HandData.PlayerHandData currentPlayerHand = playerHands.get(playerIndex);

            // Peek at the playerHandData. If it belongs to this hand, we match it up and increase the index.
            if(currentHand.id_hand == currentPlayerHand.id_hand) {
                currentHand.playerHandData.add(currentPlayerHand);
                playerIndex++;
            } else {
                handsIndex++;
            }
        }

        assert handsIndex == hands.size() - 1;
        return hands;
    }

    private ArrayList<HandData> getHandSummaryData(String innerQuery) throws SQLException {
        final String handSummaryOuterQuerySql =
                "SELECT summary.id_hand, summary.date_played, summary.amt_pot, summary.card_1,\n" +
                "       summary.card_2, summary.card_3, summary.card_4, summary.card_5,\n" +
                "       summary.str_actors_p, summary.str_aggressors_p\n" +
                "FROM cash_hand_summary as summary\n" +
                "WHERE summary.cnt_players_f > 1 AND\n" +
                "summary.id_hand in\n" +
                "(\n" +
                "       %s\n" +
                ")\n" +
                "ORDER BY summary.id_hand DESC";
        String fullSql = String.format(handSummaryOuterQuerySql, innerQuery);

        ArrayList<HandData> hands = new ArrayList<>();
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(fullSql)) {

            while (rs.next()) {
                HandData hand = mapHandData(rs);
                hands.add(hand);
            }
        }

        return hands;
    }

    private ArrayList<HandData.PlayerHandData> getPlayerHandData(String innerQuery) throws SQLException {
        final String handPlayerStatsOuterQuerySql =
                "SELECT stats.id_hand, p_actions.action as p_action, f_actions.action as f_action, t_actions.action as t_action, r_actions.action as r_action,\n" +
                        "  stats.id_player, stats.holecard_1, stats.holecard_2, stats.position, stats.amt_before\n" +
                        "FROM cash_hand_player_statistics as stats\n" +
                        "\n" +
                        "INNER JOIN lookup_actions as p_actions\n" +
                        "  on stats.id_action_p = p_actions.id_action\n" +
                        "INNER JOIN lookup_actions as f_actions\n" +
                        "  on stats.id_action_f = f_actions.id_action\n" +
                        "INNER JOIN lookup_actions as t_actions\n" +
                        "  on stats.id_action_t = t_actions.id_action\n" +
                        "INNER JOIN lookup_actions as r_actions\n" +
                        "  on stats.id_action_r = r_actions.id_action\n" +
                        "\n" +
                        "WHERE p_actions.action != 'F' AND \n" +
                        "stats.id_hand in\n" +
                        "(\n" +
                        "  %s\n" +
                        ")\n" +
                        "ORDER BY stats.id_hand DESC, stats.position DESC";
        String fullSql = String.format(handPlayerStatsOuterQuerySql, innerQuery);

        ArrayList<HandData.PlayerHandData> handData = new ArrayList<>();

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(fullSql)) {

            while (rs.next()) {
                HandData.PlayerHandData hand = mapPlayerHandData(rs);
                handData.add(hand);
            }
        }

        return handData;
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
        hand.str_actors_p = rs.getString("str_actors_p");
        hand.str_aggressors_p = rs.getString("str_aggressors_p");

        return hand;
    }

    private HandData.PlayerHandData mapPlayerHandData(ResultSet rs) throws SQLException {
        HandData.PlayerHandData data = new HandData.PlayerHandData();

        data.id_hand = rs.getInt("id_hand");
        data.id_player = rs.getInt("id_player");
        data.holecard_1 = rs.getShort("holecard_1");
        data.holecard_2 = rs.getShort("holecard_2");
        data.amt_before = rs.getFloat("amt_before");
        data.position = rs.getShort("position");
        data.p_action = rs.getString("p_action");
        data.f_action = rs.getString("f_action");
        data.t_action = rs.getString("t_action");
        data.r_action = rs.getString("r_action");

        return data;
    }
}
