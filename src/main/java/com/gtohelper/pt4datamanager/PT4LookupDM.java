package com.gtohelper.pt4datamanager;

import com.gtohelper.datamanager.DataManagerBase;
import com.gtohelper.datamanager.ILookupDM;
import com.gtohelper.domain.Player;
import com.gtohelper.domain.Site;
import com.gtohelper.domain.Tag;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class PT4LookupDM  extends DataManagerBase implements ILookupDM {

    public PT4LookupDM(Connection connection) {
        super(connection);
    }

    @Override
    public ArrayList<Tag> getsTagsByType(char type) throws SQLException {
        String sql = "select * from lookup_tags where lookup_tags.enum_type = '" + type + "'";

        ArrayList<Tag> hands = new ArrayList<Tag>();

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Tag tag = mapTag(rs);
                hands.add(tag);
            }
        }

        return hands;
    }

    @Override
    public String getsHandHistory(int handId) throws SQLException {
        String sql = String.format("select history from cash_hand_histories where id_hand = %d", handId);

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                return rs.getString("history");
            }
        }

        throw new SQLException(String.format("No history for handId %d found", handId));
    }

    @Override
    public ArrayList<Site> getSites() throws SQLException {
        String sql = "select * from lookup_sites";

        ArrayList<Site> hands = new ArrayList<Site>();

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Site site = mapSite(rs);
                hands.add(site);
            }
        }

        return hands;
    }


    @Override
    public ArrayList<Player> getSortedPlayersBySite(int siteId, int maxRows)throws SQLException {
        String sql = String.format(
                "SELECT	playerId, playerName,\n" +
                "    SUM(handCount) as totalHands\n" +
                "FROM\n" +
                "(\n" +
                "        SELECT player.id_player as playerId,\n" +
                "           player.player_name as playerName,\n" +
                "           count(*) as handCount\n" +
                "       FROM player\n" +
                "       INNER JOIN cash_hand_player_statistics as cash_stats\n" +
                "           on cash_stats.id_player = player.id_player\n" +
                "\n" +
                "       WHERE player.id_site = %d\n" +
                "       GROUP BY player.id_player, player.player_name\n" +
                "\n" +
                "   UNION ALL\n" +
                "\n" +
                "       SELECT player.id_player as playerId,\n" +
                "           player.player_name as playerName,\n" +
                "           count(*) as handCount\n" +
                "       FROM player\n" +
                "       INNER JOIN tourney_hand_player_statistics as tourney_stats\n" +
                "           on tourney_stats.id_player = player.id_player\n" +
                "\n" +
                "       WHERE player.id_site = %d\n" +
                "       GROUP BY player.id_player, player.player_name\n" +
                ") as unionedStats\n" +
                "\n" +
                "GROUP BY playerId, playerName\n" +
                "ORDER BY totalHands DESC\n" +
                "LIMIT %d", siteId, siteId, maxRows);

        ArrayList<Player> players = new ArrayList<Player>();

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Player player = mapPlayer(rs);
                players.add(player);
            }
        }

        return players;
    }

    @Override
    public String getDBVersion() throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT VERSION()")) {

            if (rs.next()) {
                return rs.getString(1);
            }

            throw new SQLException("SELECT VERSION() returned no results, even though a connection appeared to be successful.\n" +
                    "Are you sure you're connecting to a postgres database?");
        }
    }

    private Tag mapTag(ResultSet rs) throws SQLException {
        Tag tag = new Tag();
        tag.id_tag = rs.getInt("id_tag");
        tag.enum_type = rs.getString("enum_type").charAt(0);
        tag.tag = rs.getString("tag");
        tag.icon = rs.getString("icon");
        return tag;
    }

    private Site mapSite(ResultSet rs) throws SQLException {
        Site tag = new Site();
        tag.id_site = rs.getInt("id_site");
        tag.site_name = rs.getString("site_name");
        return tag;
    }

    private Player mapPlayer(ResultSet rs) throws SQLException {
        Player player = new Player();
        player.id_player = rs.getInt("playerId");
        player.player_name = rs.getString("playerName");
        player.total_hands = rs.getInt("totalHands");
        return player;
    }
}