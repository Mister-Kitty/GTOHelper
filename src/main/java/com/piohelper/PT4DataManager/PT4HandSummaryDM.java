package com.piohelper.PT4DataManager;

import com.piohelper.datamanager.DataManagerBase;
import com.piohelper.datamanager.IHandSummaryDM;
import com.piohelper.domain.HandSummary;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class PT4HandSummaryDM extends DataManagerBase implements IHandSummaryDM {

    public PT4HandSummaryDM(Connection con) {
        super(con);
    }

    public int getRowCount() throws SQLException {
        String sql = "select count(*) from cash_hand_summary";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("Fetch row count from the cash_hand_summary table failed. Maybe the table DNE?");
            }
        }
    }

    public ArrayList<HandSummary> getNHandSummaries(int N) throws SQLException {
        String sql = "select * from cash_hand_summary limit " + N;
        ArrayList<HandSummary> hands = new ArrayList<>();

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                HandSummary hand = mapHandSummary(rs);
                hands.add(hand);
            }
        }

        return hands;
    }


    private HandSummary mapHandSummary(ResultSet rs) throws SQLException {
        HandSummary hand = new HandSummary();

        hand.hand = rs.getInt("id_hand");
        hand.gametype = rs.getShort("id_gametype");
        hand.site = rs.getShort("id_site");
        hand.limit = rs.getShort("id_limit");
        hand.table = rs.getInt("id_table");
        hand.hand_no = rs.getString("hand_no");
        hand.date_played = rs.getTimestamp("date_played");
        hand.date_imported = rs.getTimestamp("date_imported");
        hand.cnt_players = rs.getShort("cnt_players");
        hand.cnt_players_f = rs.getShort("cnt_players_f");
        hand.cnt_players_t = rs.getShort("cnt_players_t");
        hand.cnt_players_r = rs.getShort("cnt_players_r");
        hand.amt_pot = rs.getFloat("amt_pot");
        hand.amt_rake = rs.getFloat("amt_rake");
        hand.amt_short_stack = rs.getFloat("amt_short_stack");
        hand.amt_pot_p = rs.getFloat("amt_pot_p");
        hand.amt_pot_f = rs.getFloat("amt_pot_f");
        hand.amt_pot_t = rs.getFloat("amt_pot_t");
        hand.amt_pot_r = rs.getFloat("amt_pot_r");
        hand.str_actors_p = rs.getString("str_actors_p");
        hand.str_actors_f = rs.getString("str_actors_f");
        hand.str_actors_t = rs.getString("str_actors_t");
        hand.str_actors_r = rs.getString("str_actors_r");
        hand.str_aggressors_p = rs.getString("str_aggressors_p");
        hand.str_aggressors_f = rs.getString("str_aggressors_f");
        hand.str_aggressors_t = rs.getString("str_aggressors_t");
        hand.str_aggressors_r = rs.getString("str_aggressors_r");
        hand.id_win_hand = rs.getShort("id_win_hand");
        hand.id_winner = rs.getInt("id_winner");
        hand.button = rs.getShort("button");
        hand.card_1 = rs.getShort("card_1");
        hand.card_2 = rs.getShort("card_2");
        hand.card_3 = rs.getShort("card_3");
        hand.card_4 = rs.getShort("card_4");
        hand.card_5 = rs.getShort("card_5");
        hand.flg_note = rs.getBoolean("flg_note");
        hand.flg_tag = rs.getBoolean("flg_tag");
        hand.flg_autonote = rs.getBoolean("flg_autonote");

        return hand;
    }


}
