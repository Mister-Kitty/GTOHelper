package com.gtohelper.pt4datamanager;

import com.gtohelper.datamanager.DataManagerBase;
import com.gtohelper.datamanager.ISessionDM;
import com.gtohelper.datamanager.ITournamentDM;
import com.gtohelper.domain.Session;
import com.gtohelper.domain.SessionBundle;
import com.gtohelper.domain.Tournament;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class PT4TournamentDM extends DataManagerBase implements ITournamentDM {
    public static final Calendar tzUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    public PT4TournamentDM(Connection connection) {
        super(connection);
    }

    @Override
    public ArrayList<Tournament> getAllTournaments(int siteId, int playerId) throws SQLException {
        // We need an inner query to select all handIDs for the flattened session
        String tourneyIdSelectSQL = String.format(
                "select tourney.id_tourney,\n" +
                        "tourney.date_start,\n" +
                        "tourney.cnt_hands,\n" +
                        "tourney.cnt_players,\n" +
                        "tourney.amt_buyin\n" +
                "from tourney_summary as tourney\n" +
                        "where\n" +
                        "\t   (tourney.id_gametype = 1 and \n" +
                        "\t   tourney.id_site = %d)"
                , siteId);

        ArrayList<Tournament> allTournaments = new ArrayList<>();
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(tourneyIdSelectSQL)) {

            while (rs.next()) {
                Tournament currentTournament = mapTournament(rs);
                allTournaments.add(currentTournament);
            }

        }

        return allTournaments;
    }

    private Tournament mapTournament(ResultSet rs) throws SQLException {
        Tournament tournament = new Tournament();

        tournament.id_tourney = rs.getInt("id_tourney");
        tournament.date_start = rs.getTimestamp("date_start", tzUTC).toLocalDateTime();
        tournament.cnt_hands = rs.getInt("cnt_hands");
        tournament.cnt_players = rs.getInt("cnt_players");
        tournament.amt_buyin = rs.getFloat("amt_buyin");

        return tournament;
    }
}
