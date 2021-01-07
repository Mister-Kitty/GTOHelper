package com.gtohelper.PT4DataManager;

import com.gtohelper.datamanager.DataManagerBase;
import com.gtohelper.datamanager.IHandDataDM;
import com.gtohelper.domain.HandData;
import com.gtohelper.domain.Ranges;

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

        // And finally, there are computed fields in the objects that need their numbers crunched.
        // I'm not sure exectly where this code should be placed... I'll put it here and move it later if needed.
        computeCalculatedFieldsForHandData(hands);

        return hands;
    }

    private void computeCalculatedFieldsForHandData(ArrayList<HandData> hands) {

        for(HandData hand : hands) {
            hand.highestPreflopBetLevel = hand.str_aggressors_p.length();

            // Manually handle the all-limp case, to make edge cases in the next code chunk easier.
            if(hand.highestPreflopBetLevel == 1) {
                for(HandData.PlayerHandData handData : hand.playerHandData) {
                    handData.p_betLevel = 1;
                    handData.p_vsPosition = 8; // 8 is BB
                    handData.last_p_action = Ranges.LastAction.CALL;
                }
                continue;
            }

            // We can't directly resolve what action was taken against which seat via the stats table.
            // Instead, we have to actually 'replay' the action in order to figure out what 'vs range' we use.
            int lastAggressorsIndex = 0; // index within str_aggressors_p
            short currentBetLevel = 1; // eg. 1bet, 2bet, 3bet, etc.
            int lastAggressorSeat = 8; // == Character.getNumericValue(hand.str_actors_p.charAt(aggressorsIndex)
            int nextAggressorSeat = Character.getNumericValue(hand.str_aggressors_p.charAt(lastAggressorsIndex + 1));
            for(int currentActorIndex = 0; currentActorIndex < hand.str_actors_p.length(); currentActorIndex++) {
                int currentPlayerPosition = Character.getNumericValue(hand.str_actors_p.charAt(currentActorIndex));
                HandData.PlayerHandData handDataForSeat = hand.getHandDataForPosition(currentPlayerPosition);

                if(currentPlayerPosition == nextAggressorSeat) {
                    // If we're the next aggressor, update as such.
                    currentBetLevel++; // Technically = to lastAggressorIndex - 1, but separated out for clarity.
                    handDataForSeat.last_p_action = Ranges.LastAction.RAISE;
                    handDataForSeat.p_betLevel = currentBetLevel;
                    handDataForSeat.p_vsPosition = (short)lastAggressorSeat;

                    //_if_ another aggressor exists, update the field, else set it to -1 if we're the last aggressor.
                    lastAggressorsIndex++;
                    nextAggressorSeat = (lastAggressorsIndex >= hand.str_aggressors_p.length() - 1) ? -1 :
                            Character.getNumericValue(hand.str_aggressors_p.charAt(lastAggressorsIndex + 1));
                    lastAggressorSeat = currentPlayerPosition;
                } else {
                    // Otherwise we're just calling
                    handDataForSeat.last_p_action = Ranges.LastAction.CALL;
                    handDataForSeat.p_betLevel = currentBetLevel;
                    handDataForSeat.p_vsPosition = (short)lastAggressorSeat;
                }
            }


            // Then resolve the principal players based upon whomever won and lost the most money.
            HandData.PlayerHandData winner = hand.getBiggestWinner();
            HandData.PlayerHandData loser = hand.getBiggestLoser();

            boolean winnerIsOOP = winner.position > loser.position;
            hand.oopPlayer = winnerIsOOP ? winner : loser;
            hand.ipPlayer = winnerIsOOP ? loser : winner;
            if(hand.oopPlayer.seat == hand.ipPlayer.seat)
                assert false;
        }


    }

    private ArrayList<HandData> getHandSummaryData(String innerQuery) throws SQLException {
        final String handSummaryOuterQuerySql =
                "SELECT summary.id_hand, summary.date_played, summary.cnt_players, summary.amt_pot, summary.card_1,\n" +
                "       summary.card_2, summary.card_3, summary.card_4, summary.card_5,\n" +
                "       summary.str_actors_p, summary.str_aggressors_p, summary.amt_pot_f,\n" +
                "       table_limit.limit_name, table_limit.amt_sb, table_limit.amt_bb\n" +
                "FROM cash_hand_summary as summary\n" +
                "INNER JOIN cash_limit as table_limit\n" +
                "  on summary.id_limit = table_limit.id_limit\n" +
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
                        "  stats.id_player, stats.holecard_1, stats.holecard_2, stats.amt_before, stats.amt_won, stats.position \n" +
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
        hand.cnt_players = rs.getShort("cnt_players");
        hand.amt_pot = rs.getFloat("amt_pot");
        hand.card_1 = rs.getShort("card_1");
        hand.card_2 = rs.getShort("card_2");
        hand.card_3 = rs.getShort("card_3");
        hand.card_4 = rs.getShort("card_4");
        hand.card_5 = rs.getShort("card_5");
        hand.str_actors_p = rs.getString("str_actors_p");
        hand.str_aggressors_p = rs.getString("str_aggressors_p");
        hand.amt_pot_f = rs.getFloat("amt_pot_f");

        hand.limit_name = rs.getString("limit_name");
        hand.amt_sb = rs.getFloat("amt_sb");
        hand.amt_bb = rs.getFloat("amt_bb");

        return hand;
    }

    private HandData.PlayerHandData mapPlayerHandData(ResultSet rs) throws SQLException {
        HandData.PlayerHandData data = new HandData.PlayerHandData();

        data.id_hand = rs.getInt("id_hand");
        data.id_player = rs.getInt("id_player");
        data.holecard_1 = rs.getShort("holecard_1");
        data.holecard_2 = rs.getShort("holecard_2");
        data.amt_before = rs.getFloat("amt_before");
        data.amt_won = rs.getFloat("amt_won");
        data.position = rs.getShort("position");
        data.seat = Ranges.Seat.valuesByTrackerPosition[data.position];
        data.p_action = rs.getString("p_action");
        data.f_action = rs.getString("f_action");
        data.t_action = rs.getString("t_action");
        data.r_action = rs.getString("r_action");

        return data;
    }
}
