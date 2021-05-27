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
    public ArrayList<Player> getSortedPlayersBySite(int siteId, int minCount)throws SQLException {
        String sql = String.format(
                "SELECT cash_table_session_summary.id_player, COUNT(*) as cnt, MAX(p.player_name) as player_name,\n" +
                "\t\t\tbool_or(p.flg_note) as flg_note, bool_or(p.flg_tag) as flg_tag\n" +
                "FROM cash_table_session_summary\n" +
                "\tLEFT JOIN \n" +
                "\t\t(SELECT *\n" +
                "\t\t FROM player\n" +
                "\t\tWHERE id_site = %d) AS p\n" +
                "\tON cash_table_session_summary.id_player = p.id_player\n" +
                "WHERE cash_table_session_summary.id_site = %d\n" +
                "GROUP BY cash_table_session_summary.id_player\n" +
             //   "HAVING COUNT(*) > %d" +
                "ORDER BY cnt DESC\n" +
                "LIMIT %d", siteId, siteId, minCount);

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
        player.id_player = rs.getInt("id_player");
        player.player_name = rs.getString("player_name");
        player.flg_note = rs.getBoolean("flg_note");
        player.flg_tag = rs.getBoolean("flg_tag");
        return player;
    }
}