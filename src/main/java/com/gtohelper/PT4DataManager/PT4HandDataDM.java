package com.gtohelper.PT4DataManager;

import com.gtohelper.datamanager.DataManagerBase;
import com.gtohelper.datamanager.IHandDataDM;
import com.gtohelper.domain.HandData;
import com.gtohelper.domain.Ranges;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
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

        computeCalculatedFieldsForHandData(hands, playerId);

        return hands;
    }

    private void computeCalculatedFieldsForHandData(ArrayList<HandData> hands, int heroPlayerId) {
        for(HandData hand : hands) {
            hand.highestPreflopBetLevel = hand.str_aggressors_p.length();

            // ordering of calls is important
            resolvePreflopActionForPlayersInHand(hand);
            resolveHandResolvability(hand);
            resolveIPandOOPplayer(hand, heroPlayerId);
        }
    }

    private void resolvePreflopActionForPlayersInHand(HandData hand) {
        // Manually handle the all-limp case, to make edge cases in the following code chunk easier.
        if(hand.highestPreflopBetLevel == 1) {
            for(HandData.PlayerHandData handData : hand.playerHandData) {
                handData.p_betLevel = 1;
                handData.p_vsPosition = 8; // 8 is BB
                handData.last_p_action = Ranges.LastAction.CALL;
            }
            return;
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
    }

    private void resolveHandResolvability(HandData hand) {
        if(hand.playerHandData.size() == 2) {
            // The playerHandData arrayList contains all non instant folds. Thus if there's only 2, we're HU.
            hand.solveabilityLevel = HandData.SolvabilityLevel.HU_PRE;
        } else {
            if(hand.cnt_players_f == 2) {
                hand.solveabilityLevel = HandData.SolvabilityLevel.MULTI_PRE_HU_FLOP;
            } else {
                // If we're here, we're multiway to the flop. Let's see if there's exactly 2 players who VPIP flop
                long numFlopActors = hand.str_actors_f.chars().distinct().count();
                if(numFlopActors == 1 || numFlopActors == 2) {
                    hand.solveabilityLevel = HandData.SolvabilityLevel.MULTI_FLOP_HU_FLOP_VPIP;
                } else {
                    // So we either had 0 actors on the flop, or >= 3.
                    // Did we get to showdown?
                    hand.solveabilityLevel = HandData.SolvabilityLevel.MULTI_FLOP_MULTI_VPIP;
                /*        int numPlayersAtShowdown = hand.playersAtShowdown();
                        if(numPlayersAtShowdown == 0) {
                            // We didn't go to showdown. So we're multi_vpip without showdown.
                            hand.solveabilityLevel = HandData.SolvabilityLevel.MULTI_FLOP_MULTI_VPIP;
                        } else {
                            //
                        }
                */
                }
            }
        }
    }

    private void resolveIPandOOPplayer(HandData hand, int heroPlayerId) {
        // Then resolve the principal players
        HandData.PlayerHandData player1;
        HandData.PlayerHandData player2;
        HandData.PlayerHandData hero = hand.getHandDataForPlayer(heroPlayerId);

        boolean heroSawFlop = !hero.f_action.isEmpty();
        if(heroSawFlop) {
            // Find the last street that we partook in.
            // On that street, the villain is either:
            // - The player we folded to
            // - The last player we made fold
            // - Otherwise we're at showdown.
            player1 = hero;

        } else {
            player1 = hand.getBiggestWinner();
            player2 = hand.getBiggestLoser();
        }

   //     boolean winnerIsOOP = player1.position > player2.position;
   //     hand.oopPlayer = winnerIsOOP ? player1 : player2;
   //     hand.ipPlayer = winnerIsOOP ? player2 : player1;
        if(hand.oopPlayer.seat == hand.ipPlayer.seat)
            assert false;
    }

    private ArrayList<HandData> getHandSummaryData(String innerQuery) throws SQLException {
        final String handSummaryOuterQuerySql =
                "SELECT summary.id_hand, summary.date_played, summary.cnt_players, summary.amt_pot, summary.card_1,\n" +
                "       summary.card_2, summary.card_3, summary.card_4, summary.card_5,\n" +
                "       summary.str_actors_p, summary.str_aggressors_p,\n" +
                "       summary.cnt_players_f, summary.str_actors_f, summary.str_aggressors_f, summary.amt_pot_f,\n" +
                "       summary.cnt_players_t,\n" +
                "       summary.cnt_players_r,\n" +
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
                        "  stats.id_player, stats.holecard_1, stats.holecard_2, stats.amt_before, stats.amt_won, stats.position, stats.flg_showdown \n" +
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
        hand.date_played = rs.getObject("date_played", LocalDateTime.class);
        hand.cnt_players = rs.getShort("cnt_players");
        hand.cnt_players_f = rs.getShort("cnt_players_f");
        hand.cnt_players_t = rs.getShort("cnt_players_t");
        hand.cnt_players_r = rs.getShort("cnt_players_r");
        hand.amt_pot = rs.getFloat("amt_pot");
        hand.card_1 = rs.getShort("card_1");
        hand.card_2 = rs.getShort("card_2");
        hand.card_3 = rs.getShort("card_3");
        hand.card_4 = rs.getShort("card_4");
        hand.card_5 = rs.getShort("card_5");
        hand.str_actors_p = rs.getString("str_actors_p");
        hand.str_aggressors_p = rs.getString("str_aggressors_p");
        hand.str_actors_f = rs.getString("str_actors_f");
        hand.str_aggressors_f = rs.getString("str_aggressors_f");
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
        data.flg_showdown = rs.getBoolean("flg_showdown");

        return data;
    }
}
