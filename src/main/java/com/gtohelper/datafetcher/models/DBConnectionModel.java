package com.gtohelper.datafetcher.models;

import com.gtohelper.PT4DataManager.PT4GeneralDM;
import com.gtohelper.PT4DataManager.PT4LookupDM;
import com.gtohelper.database.Database;
import com.gtohelper.datamanager.IGeneralDM;
import com.gtohelper.datamanager.ILookupDM;
import com.gtohelper.domain.Player;
import com.gtohelper.domain.Site;
import com.gtohelper.utility.SaveFileHelper;
import com.gtohelper.utility.Saveable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class DBConnectionModel extends Saveable {
    public DBConnectionModel(SaveFileHelper saveHelper) {
        super(saveHelper, "DBConnection");
    }

    public boolean testConnection(String url, String user, String password) throws SQLException {
        try (Connection con = DriverManager.getConnection(url, user, password);) {
            IGeneralDM generalDM = new PT4GeneralDM(con);
            generalDM.getDBVersion();
            Database.initialize(url, user, password);
            return true;
        } catch(SQLException e) {
            throw e;
        }
    }

    public ArrayList<Site> getSites() throws SQLException {
        try (Connection con = Database.getConnection();) {

            ILookupDM lookupDM = new PT4LookupDM(con);
            return lookupDM.getSites();
        }
    }

    public ArrayList<Player> getSortedPlayersBySite(int siteId, int minSessionCount) throws SQLException {
        try (Connection con = Database.getConnection();) {

            ILookupDM lookupDM = new PT4LookupDM(con);
            return lookupDM.getSortedPlayersBySite(siteId, minSessionCount);
        }
    }

    @Override
    public HashMap<String, String> getDefaultValues() {
        HashMap<String, String> values = new HashMap<String, String>();

        values.put("address", "localhost");
        values.put("port", "5432");
        values.put("name", "PT4 DB");
        values.put("user", "postgres");
        values.put("pass", "dbpass");

        return values;
    }
}
