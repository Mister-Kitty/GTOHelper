package com.piohelper.PT4DataManager;

import com.piohelper.datamanager.DataManagerBase;
import com.piohelper.datamanager.IGeneralDM;
import com.piohelper.datamanager.ILookupDM;
import com.piohelper.domain.HandSummary;
import com.piohelper.domain.Tag;

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
        String sql = "select count(*) from lookup_tags where lookup_tags.enum_type = '" + type + "'";

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

    private Tag mapTag(ResultSet rs) throws SQLException {
        Tag tag = new Tag();

        tag.id_tag = rs.getInt("id_tag");
        tag.enum_type = rs.getString("enum_type").charAt(0);
        tag.tag = rs.getString("tag");
        tag.icon = rs.getString("icon");
        return tag;

    }
}