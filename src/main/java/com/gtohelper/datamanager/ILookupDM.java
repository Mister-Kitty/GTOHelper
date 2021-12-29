package com.gtohelper.datamanager;

import com.gtohelper.domain.Player;
import com.gtohelper.domain.Site;
import com.gtohelper.domain.Tag;

import java.sql.SQLException;
import java.util.ArrayList;

public interface ILookupDM {
    ArrayList<Tag> getsTagsByType(char type) throws SQLException;
    String getsHandHistory(int handId) throws SQLException;
    ArrayList<Site> getSites() throws SQLException;
    ArrayList<Player> getSortedPlayersBySite(int siteId, int maxRows) throws SQLException;
    String getDBVersion() throws SQLException;
}
