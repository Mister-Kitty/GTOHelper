package com.gtohelper.PT4DataManager;

import com.gtohelper.datamanager.DataManagerBase;
import com.gtohelper.datamanager.IHandDataDM;
import com.gtohelper.domain.*;
import com.gtohelper.domain.HandData.PlayerHandData;
import com.gtohelper.domain.HandData.PlayerHandData.LastActionForStreet;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PT4HandDataDM extends DataManagerBase implements IHandDataDM {
    public PT4HandDataDM(Connection connection) {
        super(connection);
    }

    public ArrayList<HandData> getHandDataByTag(int tagId, int playerId) throws SQLException {
        String handIdSelectSQL = String.format("select id_x from tags where tags.id_tag = %d", tagId);

        ArrayList<HandData> hands = getHandSummaryData(handIdSelectSQL);
        ArrayList<PlayerHandData> playerHands = getPlayerHandData(handIdSelectSQL);

        // Both are ordered by id_hand descending. Let's bundle them up
        int handsIndex = 0;
        int playerIndex = 0;
        while(playerIndex < playerHands.size()) {
            HandData currentHand = hands.get(handsIndex);
            PlayerHandData currentPlayerHand = playerHands.get(playerIndex);

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
            resolvePostflopActionForPlayersInHand(hand, Street.FLOP, hand.str_aggressors_f, hand.str_actors_f);
            if(hand.cnt_players_t > 0)
                resolvePostflopActionForPlayersInHand(hand, Street.TURN, hand.str_aggressors_t, hand.str_actors_t);
            if(hand.cnt_players_r > 0)
                resolvePostflopActionForPlayersInHand(hand, Street.RIVER, hand.str_aggressors_r, hand.str_actors_r);
            resolveHandResolvability(hand);
            resolveIPandOOPplayer(hand, heroPlayerId);
            resolvePreflopAggressor(hand);
        }
    }

    // resolvePostflop below this function was refactored/expanded later. This could mimic that refactor if desired.
    private void resolvePreflopActionForPlayersInHand(HandData hand) {
        // Manually handle the all-limp case, to make edge cases in the following code chunk easier.
        if(hand.highestPreflopBetLevel == 1) {
            for(PlayerHandData handData : hand.playerHandData) {
                LastActionForStreet lastPreflopAction = handData.getLastActionForStreet(Street.PRE);
                lastPreflopAction.betLevel = 1;
                lastPreflopAction.vsSeat = Seat.BB;
                lastPreflopAction.action = Action.CALL;
            }
            return;
        }

        // We can't directly resolve what action was taken against which seat via the stats table.
        // Instead, we have to actually 'replay' the action in order to figure out what 'vs range' we use.
        short currentBetLevel = 1; // eg. 1bet, 2bet, 3bet, etc.
        int lastAggressorsIndex = 0; // index within str_aggressors_p
        Seat lastAggressorSeat = Seat.BB;
        Seat nextAggressorSeat = getSeatFromChar(hand.str_aggressors_p.charAt(lastAggressorsIndex + 1)); // +1 to skip BB
        for(int currentPlayerIndex = 0; currentPlayerIndex < hand.str_actors_p.length(); currentPlayerIndex++) {
            Seat currentPlayerSeat = getSeatFromChar(hand.str_actors_p.charAt(currentPlayerIndex));
            PlayerHandData handDataForSeat = hand.getHandDataForSeat(currentPlayerSeat);
            LastActionForStreet lastPreflopAction = handDataForSeat.getLastActionForStreet(Street.PRE);

            if(currentPlayerSeat == nextAggressorSeat) {
                // If we're the next aggressor, update as such.
                currentBetLevel++;
                lastPreflopAction.action = Action.RAISE;
                lastPreflopAction.betLevel = currentBetLevel;
                lastPreflopAction.vsSeat = lastAggressorSeat;

                // _If_ another aggressor exists, update the field.
                lastAggressorsIndex++;
                nextAggressorSeat = (lastAggressorsIndex >= hand.str_aggressors_p.length() - 1) ? null :
                        getSeatFromChar(hand.str_aggressors_p.charAt(lastAggressorsIndex + 1));
                lastAggressorSeat = currentPlayerSeat;
            } else {
                // Otherwise we're just calling
                lastPreflopAction.action = Action.CALL;
                lastPreflopAction.betLevel = currentBetLevel;
                lastPreflopAction.vsSeat = lastAggressorSeat;
            }
        }
    }

    private void resolvePostflopActionForPlayersInHand(HandData hand, Street street, String aggressorsForStreet, String actorsForStreet) {
        List<PlayerHandData> hands = hand.getHandsThatReachStreet(street);
        HandData.sortHandDataListByPostflopPosition(hands);

        // Manually handle the all-limp case, to make edge cases in the following code chunk easier.
        if(aggressorsForStreet.isEmpty()) {
            for(PlayerHandData handData : hands) {
                LastActionForStreet lastAction = handData.getLastActionForStreet(street);
                lastAction.betLevel = 0;
                lastAction.vsSeat = null;
                lastAction.action = Action.CALL;
            }
            return;
        }

        short currentBetLevel = 0; // eg. 1bet, 2bet, 3bet, etc.
        Seat lastAggressorSeat = null; // used to track who we fold to.

        int currentAggressorsIndex = 0; // index within aggressorsForStreet
        int currentActorIndex = 0; // index within actorsForStreet
        Seat nextAggressorSeat = getSeatFromChar(aggressorsForStreet.charAt(currentAggressorsIndex));
        Seat nextActorSeat = getSeatFromChar(actorsForStreet.charAt(currentActorIndex));

        // This maybe could be simplified, but I feel that having two loops leaves it explicit to the reader
        int handsIndex = 0;
        do {
            // We cycle around and around the players in the hand, as if to replay it.
            PlayerHandData currentPlayerHand = hands.get(handsIndex);
            handsIndex = (handsIndex + 1) % hands.size();

            LastActionForStreet lastAction = currentPlayerHand.getLastActionForStreet(street);
            if(lastAction.action == Action.FOLD)
                continue;

            if(currentPlayerHand.seat == lastAggressorSeat)
                break;

            // When we're here, action is on a non-folded player.
            if (currentPlayerHand.seat == nextAggressorSeat) {
                // If we're the next aggressor, update as such. Note that we're also the next actor by definition.
                currentBetLevel++;
                if(currentBetLevel == 1)
                    lastAction.action = Action.BET;
                else
                    lastAction.action = Action.RAISE;
                lastAction.betLevel = currentBetLevel;
                lastAction.vsSeat = lastAggressorSeat;

                currentAggressorsIndex++;
                nextAggressorSeat = (currentAggressorsIndex >= aggressorsForStreet.length()) ? null :
                        getSeatFromChar(aggressorsForStreet.charAt(currentAggressorsIndex));
                lastAggressorSeat = nextActorSeat;

                currentActorIndex++;
                nextActorSeat = (currentActorIndex >= actorsForStreet.length()) ? null :
                        getSeatFromChar(actorsForStreet.charAt(currentActorIndex));
            } else if (currentPlayerHand.seat == nextActorSeat) {
                // If we're the next actor but not next aggressor, then there is a prior bet we've called.
                lastAction.action = Action.CALL;
                lastAction.betLevel = currentBetLevel;
                lastAction.vsSeat = lastAggressorSeat;

                currentActorIndex++;
                nextActorSeat = (currentActorIndex >= actorsForStreet.length()) ? null :
                        getSeatFromChar(actorsForStreet.charAt(currentActorIndex));
            } else if (lastAggressorSeat == null) {
                // If no prior bet and we arn't an actor, it's because we check with no bets before us
                lastAction.action = Action.CHECK;
                lastAction.betLevel = currentBetLevel;
                lastAction.vsSeat = null;
            } else {
                lastAction.action = Action.FOLD;
                lastAction.betLevel = currentBetLevel;
                lastAction.vsSeat = lastAggressorSeat;
            }
        } while (true);
    }

    private Seat getSeatFromChar(char seatChar) {
        return Seat.fromTrackerPosition(Character.getNumericValue(seatChar));
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
                    int numPlayersAtShowdown = hand.playersAtShowdown();
                    if(numPlayersAtShowdown == 0) {
                        // We didn't go to showdown. So we're multi_vpip without showdown.
                        hand.solveabilityLevel = HandData.SolvabilityLevel.MULTI_FLOP_MULTI_VPIP;
                    } else if (numPlayersAtShowdown == 2) {
                        hand.solveabilityLevel = HandData.SolvabilityLevel.HU_SHOWDOWN;
                    } else {
                        hand.solveabilityLevel = HandData.SolvabilityLevel.MULTI_SHOWDOWN;
                    }
                }
            }
        }
    }

    private Seat getLastVillainSeatFromActionString(String actionString, Seat heroSeat) {
        String nonHeroString = actionString.replace(Integer.toString(heroSeat.trackerPosition), "");
        if(nonHeroString.isEmpty())
            return null;

        return getSeatFromChar(nonHeroString.charAt(nonHeroString.length() - 1));
    }

    private void resolveIPandOOPplayer(HandData hand, int heroPlayerId) {
        // Then resolve the principal players
        PlayerHandData player1;
        PlayerHandData player2;
        PlayerHandData hero = hand.getHandDataForPlayer(heroPlayerId);

        boolean heroSawFlop = (hero != null) && !hero.f_action.isEmpty();
        if(heroSawFlop) {
            player1 = hero;
            player2 = getVillainForHand(hand, hero);
        } else {
            player1 = hand.getBiggestWinner();
            player2 = hand.getBiggestLoser();
        }

        boolean winnerIsOOP = player1.position > player2.position;
        hand.oopPlayer = winnerIsOOP ? player1 : player2;
        hand.ipPlayer = winnerIsOOP ? player2 : player1;
        if(hand.oopPlayer.seat == hand.ipPlayer.seat)
            assert false;
    }

    private void resolvePreflopAggressor(HandData hand) {
        // If no aggressors, skip.
        if(hand.str_aggressors_p.isEmpty())
            return;

        char lastAggressor = hand.str_aggressors_p.charAt(hand.str_aggressors_p.length() - 1);
        Seat lastAggressorSeat = getSeatFromChar(lastAggressor);
        if(hand.ipPlayer.seat == lastAggressorSeat)
            hand.lastPfAggressor = Actor.IP;
        else if(hand.oopPlayer.seat == lastAggressorSeat)
            hand.lastPfAggressor = Actor.OOP;
        else
            hand.lastPfAggressor = null; // not needed, but set for readability.

    }

    private PlayerHandData getVillainForHand(HandData hand, PlayerHandData hero) {
        Street lastStreetForHero = hero.getLastStreet();
        List<PlayerHandData> villainHands = hand.getVillainHandsThatReachStreet(lastStreetForHero, hero.id_player);
        String aggressorsUpToStreet = hand.getAllAggressorsUpToStreet(lastStreetForHero);

        // Remove leading non-VPIP Big Blind aggression.
        if(aggressorsUpToStreet.length() == 1) {
            // If hand is limp + checked to showdown, return earliest preflop seat.
            HandData.sortHandDataListByPreflopPosition(villainHands);
            return villainHands.get(0);
        } else {
            aggressorsUpToStreet = aggressorsUpToStreet.substring(1);
        }

        boolean heroWonTheHand = hero.amt_won > 0;
        if(heroWonTheHand || lastStreetForHero == Street.SHOWDOWN) {
            // If hero goes to showdown or we win the hand, we look at all villains who make it to showdown and pick whoever aggressed last.
            Seat villainSeat = getLastVillainSeatFromActionString(aggressorsUpToStreet, hero.seat);
            if(villainSeat == null) {
                // If no opponents bet/raised, then we pick the earliest Preflop position
                HandData.sortHandDataListByPreflopPosition(villainHands);
                return villainHands.get(0);
            }
            return hand.getHandDataForSeat(villainSeat);
        } else {
            // We lost the hand before showdown. Our villain is the aggressor we folded to. Be sure to account for call/folds multiway.
            LastActionForStreet lastAction = hero.getLastActionForStreet(lastStreetForHero);
            return hand.getHandDataForSeat(lastAction.vsSeat);
        }
    }

    private ArrayList<HandData> getHandSummaryData(String innerQuery) throws SQLException {
        final String handSummaryOuterQuerySql =
                "SELECT summary.id_hand, summary.date_played, summary.cnt_players, summary.amt_pot, summary.card_1,\n" +
                "       summary.card_2, summary.card_3, summary.card_4, summary.card_5,\n" +
                "       summary.str_actors_p, summary.str_aggressors_p,\n" +
                "       summary.cnt_players_f, summary.str_actors_f, summary.str_aggressors_f, summary.amt_pot_f,\n" +
                "       summary.cnt_players_t, summary.str_actors_t, summary.str_aggressors_t,\n" +
                "       summary.cnt_players_r, summary.str_actors_r, summary.str_aggressors_r,\n" +
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

    private ArrayList<PlayerHandData> getPlayerHandData(String innerQuery) throws SQLException {
        final String handPlayerStatsOuterQuerySql =
                "SELECT stats.id_hand, p_actions.action as p_action, f_actions.action as f_action, t_actions.action as t_action, r_actions.action as r_action,\n" +
                        "  stats.id_player, player.player_name, stats.holecard_1, stats.holecard_2, stats.amt_before, stats.amt_won, stats.position, stats.flg_showdown \n" +
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
                        "INNER JOIN player\n" +
                        "  on stats.id_player = player.id_player\n" +
                        "\n" +
                        "WHERE p_actions.action != 'F' AND \n" +
                        "stats.id_hand in\n" +
                        "(\n" +
                        "  %s\n" +
                        ")\n" +
                        "ORDER BY stats.id_hand DESC, stats.position DESC";
        String fullSql = String.format(handPlayerStatsOuterQuerySql, innerQuery);

        ArrayList<PlayerHandData> handData = new ArrayList<>();

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(fullSql)) {

            while (rs.next()) {
                PlayerHandData hand = mapPlayerHandData(rs);
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
        hand.str_actors_t = rs.getString("str_actors_t");
        hand.str_aggressors_t = rs.getString("str_aggressors_t");
        hand.str_actors_r = rs.getString("str_actors_r");
        hand.str_aggressors_r = rs.getString("str_aggressors_r");

        hand.limit_name = rs.getString("limit_name");
        hand.amt_sb = rs.getFloat("amt_sb");
        hand.amt_bb = rs.getFloat("amt_bb");

        return hand;
    }

    private PlayerHandData mapPlayerHandData(ResultSet rs) throws SQLException {
        PlayerHandData data = new PlayerHandData();

        data.id_hand = rs.getInt("id_hand");
        data.id_player = rs.getInt("id_player");
        data.player_name = rs.getString("player_name");
        data.holecard_1 = rs.getShort("holecard_1");
        data.holecard_2 = rs.getShort("holecard_2");
        data.amt_before = rs.getFloat("amt_before");
        data.amt_won = rs.getFloat("amt_won");
        data.position = rs.getShort("position");
        data.seat = Seat.valuesByTrackerPosition[data.position];
        data.p_action = rs.getString("p_action");
        data.f_action = rs.getString("f_action");
        data.t_action = rs.getString("t_action");
        data.r_action = rs.getString("r_action");
        data.flg_showdown = rs.getBoolean("flg_showdown");

        return data;
    }
}
