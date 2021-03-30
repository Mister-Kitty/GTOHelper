package com.gtohelper.pt4datamanager;

import com.gtohelper.datamanager.DataManagerBase;
import com.gtohelper.datamanager.ISessionDM;
import com.gtohelper.domain.Session;
import com.gtohelper.domain.SessionBundle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class PT4SessionDM extends DataManagerBase implements ISessionDM {
    public static final Calendar tzUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    public PT4SessionDM(Connection connection) {
        super(connection);
    }

    @Override
    public ArrayList<SessionBundle> getAllSessionBundles(int siteId, int playerId) throws SQLException {
        String allSessionsSql = String.format(sessionSqlQuery, playerId);

        ArrayList<SessionBundle> allBundles = new ArrayList<>();
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(allSessionsSql)) {

            SessionBundle currentBundle = new SessionBundle();
            while (rs.next()) {
                Session currentSession = mapSession(rs);

                // The queried Sessions are ordered by session_date_start.
                // Let's see if our next session belongs in our previous session bundle.
                if(currentBundle.isEmpty()) {
                    currentBundle.addSession(currentSession);
                } else {
                    Duration betweenMaxEndAndCurrentStart = Duration.between(currentBundle.getMaxSessionEndTime().plusMinutes(30), currentSession.session_date_start);

                    // This next session belongs into the previous bundle if there has been _less than_ a 30m break;
                    // so if our duration above is exactly Zero, or positive, then >= 30m has passed and we move to the next bundle.
                    if(betweenMaxEndAndCurrentStart.isNegative()) {
                        currentBundle.addSession(currentSession);
                    } else {
                        // Add the
                        allBundles.add(currentBundle);
                        currentBundle = new SessionBundle();
                        currentBundle.addSession(currentSession);
                    }
                }
            }

            if(!currentBundle.isEmpty())
                allBundles.add(currentBundle);
        }

        return allBundles;
    }

    private Session mapSession(ResultSet rs) throws SQLException {
        Session session = new Session();



        session.id_session = rs.getInt("id_session");
        session.session_date_start = rs.getTimestamp("session_date_start", tzUTC).toLocalDateTime();
        session.cnt_hands = rs.getInt("cnt_hands");
        session.cnt_hands_flopped = rs.getInt("cnt_hands_flopped");
        session.amt_won_curr_conv = rs.getFloat("amt_won_curr_conv");
        session.amt_won = rs.getFloat("amt_won");
        session.session_date_end = rs.getTimestamp("session_date_end", tzUTC).toLocalDateTime();

        session.duration = Duration.between(session.session_date_start, session.session_date_end);

        return session;
    }

    private final String sessionSqlQuery = "SELECT (cash_table_session_summary.id_session) as \"id_sessions\", \n" +
            "(timezone('UTC',  cash_table_session_summary.date_start  + INTERVAL '0 HOURS')) as \"session_date_start\", \n" +
            "(cash_table_session_summary.id_session) as \"id_session\", " +
            "(sum((case when(cash_hand_player_statistics.id_hand > 0) then  1 else  0 end))) as \"cnt_hands\", \n" +
            "(sum((case when(cash_hand_player_statistics.flg_f_saw) then  1 else  0 end))) as \"cnt_hands_flopped\", \n" +
            "(sum(CAST( (cash_hand_player_statistics.val_curr_conv * cash_hand_player_statistics.amt_won) AS numeric ))) as \"amt_won_curr_conv\", \n" +
            "(sum(CAST( (cash_hand_player_statistics.val_curr_conv * ((case when(cash_hand_player_statistics.amt_payout > 0) then  cash_hand_player_statistics.amt_payout - cash_hand_player_statistics.amt_bet_ttl else  cash_hand_player_statistics.amt_won end))) AS numeric ))) as \"amt_won_inc_payout_curr_conv\",\n" +
            "(sum(cash_hand_player_statistics.amt_payout)) as \"amt_payout\", " +
            "(sum((case when(cash_hand_player_statistics.flg_vpip) then  1 else  0 end))) as \"cnt_vpip\",\n" +
            "(sum((case when(lookup_actions_p.action = '') then  1 else  0 end))) as \"cnt_walks\", " +
            "(sum((case when(cash_hand_player_statistics.cnt_p_raise > 0) then  1 else  0 end))) as \"cnt_pfr\",\n" +
            "(sum((case when( lookup_actions_p.action LIKE '__%%' OR (lookup_actions_p.action LIKE '_' AND (cash_hand_player_statistics.amt_before > (cash_limit.amt_bb + cash_hand_player_statistics.amt_ante)) AND (cash_hand_player_statistics.amt_p_raise_facing < (cash_hand_player_statistics.amt_before - (cash_hand_player_statistics.amt_blind + cash_hand_player_statistics.amt_ante))) AND (cash_hand_player_statistics.flg_p_open_opp OR cash_hand_player_statistics.cnt_p_face_limpers > 0 OR cash_hand_player_statistics.flg_p_3bet_opp OR cash_hand_player_statistics.flg_p_4bet_opp) )) then  1 else  0 end))) as \"cnt_pfr_opp\", \n" +
            "(sum((case when(cash_hand_player_statistics.flg_f_bet) then  1 else  0 end))) as \"cnt_f_bet\", " +
            "(sum(cash_hand_player_statistics.cnt_f_raise)) as \"cnt_f_raise\", \n" +
            "(sum((case when(cash_hand_player_statistics.flg_t_bet) then  1 else  0 end))) as \"cnt_t_bet\", " +
            "(sum(cash_hand_player_statistics.cnt_t_raise)) as \"cnt_t_raise\", \n" +
            "(sum((case when(cash_hand_player_statistics.flg_r_bet) then  1 else  0 end))) as \"cnt_r_bet\", " +
            "(sum(cash_hand_player_statistics.cnt_r_raise)) as \"cnt_r_raise\", \n" +
            "(sum(cash_hand_player_statistics.cnt_f_call )) as \"cnt_f_call\", " +
            "(sum(cash_hand_player_statistics.cnt_t_call )) as \"cnt_t_call\", \n" +
            "(sum(cash_hand_player_statistics.cnt_r_call )) as \"cnt_r_call\", " +
            "(sum((case when(cash_hand_player_statistics.flg_f_fold) then  1 else  0 end))) as \"cnt_f_fold\", \n" +
            "(sum((case when(cash_hand_player_statistics.flg_t_fold) then  1 else  0 end))) as \"cnt_t_fold\", \n" +
            "(sum((case when(cash_hand_player_statistics.flg_r_fold) then  1 else  0 end))) as \"cnt_r_fold\", \n" +
            "(sum((case when(cash_hand_player_statistics.flg_showdown) then  1 else  0 end))) as \"cnt_wtsd\", \n" +
            "(sum((case when(cash_hand_player_statistics.flg_f_saw) then  1 else  0 end))) as \"cnt_f_saw\", \n" +
            "(sum((case when(cash_hand_player_statistics.flg_showdown AND cash_hand_player_statistics.flg_won_hand) then  1 else  0 end))) as \"cnt_wtsd_won\", \n" +
            "(timezone('UTC',  cash_table_session_summary.date_end  + INTERVAL '0 HOURS')) as \"session_date_end\", " +
            "(sum(cash_hand_player_statistics.amt_won)) as \"amt_won\" \n" +
            "\n" +
            "FROM       cash_hand_player_statistics , lookup_actions lookup_actions_p, cash_limit, cash_table_session_summary \n" +
            "\n" +
            "WHERE  (lookup_actions_p.id_action=cash_hand_player_statistics.id_action_p)  AND (cash_limit.id_limit = cash_hand_player_statistics.id_limit)  AND \n" +
            "(cash_hand_player_statistics.id_session = cash_table_session_summary.id_session)  AND (cash_limit.id_limit = cash_table_session_summary.id_limit)   AND \n" +
            "(cash_hand_player_statistics.id_player = %d)\n" +
            "AND ((cash_hand_player_statistics.id_gametype = 1)AND (cash_hand_player_statistics.id_gametype<>1 \n" +
            "\n" +
            "OR (cash_hand_player_statistics.id_gametype=1 AND \n" +
            "\t(cash_hand_player_statistics.id_limit in \n" +
            "\t (SELECT hlrl.id_limit FROM cash_limit hlrl WHERE \n" +
            "\t  (hlrl.flg_nlpl=false and (CASE WHEN hlrl.limit_currency='SEK' THEN (hlrl.amt_bb*0.15) ELSE (CASE WHEN hlrl.limit_currency='INR' THEN (hlrl.amt_bb*0.020) ELSE (CASE WHEN hlrl.limit_currency='XSC' THEN 0.0 ELSE (CASE WHEN hlrl.limit_currency='PLY' THEN 0.0 ELSE hlrl.amt_bb END) END) END) END)<=1.01) \n" +
            "\t  or \n" +
            "\t  (hlrl.flg_nlpl=true and (CASE WHEN hlrl.limit_currency='SEK' THEN (hlrl.amt_bb*0.15) ELSE (CASE WHEN hlrl.limit_currency='INR' THEN (hlrl.amt_bb*0.020) ELSE (CASE WHEN hlrl.limit_currency='XSC' THEN 0.0 ELSE (CASE WHEN hlrl.limit_currency='PLY' THEN 0.0 ELSE hlrl.amt_bb END) END) END) END)\n" +
            "\t   <=0.51))))))\n" +
            "\t  \n" +
            "\t  GROUP BY (cash_table_session_summary.id_session), \n" +
            "\t  (timezone('UTC',  cash_table_session_summary.date_start  + INTERVAL '0 HOURS')), \n" +
            "\t  (cash_table_session_summary.id_session), \n" +
            "\t  (timezone('UTC',  cash_table_session_summary.date_end  + INTERVAL '0 HOURS')) \n" +
            "\t  \n" +
            "\t  ORDER BY (timezone('UTC',  cash_table_session_summary.date_start  + INTERVAL '0 HOURS')) asc";
}
