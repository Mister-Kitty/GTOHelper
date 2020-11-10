package com.gtohelper.datafetcher.models;

import com.gtohelper.PT4DataManager.PT4GeneralDM;
import com.gtohelper.PT4DataManager.PT4LookupDM;
import com.gtohelper.database.Database;
import com.gtohelper.datamanager.IGeneralDM;
import com.gtohelper.datamanager.ILookupDM;
import com.gtohelper.domain.Player;
import com.gtohelper.domain.Site;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class DBConnection  {
    public boolean testConnection(String url, String user, String password) throws SQLException {
        try (Connection con = getConnection(url, user, password);) {
            IGeneralDM generalDM = new PT4GeneralDM(con);
            generalDM.getDBVersion();
            return true;
        } catch(SQLException e) {
            throw e;
        }
    }

    public Connection getConnection(String url, String user, String password) throws SQLException {
        return DriverManager.getConnection(url, user, password);
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


    public Properties loadProperties() throws IOException {
        Properties prop = new Properties();

        try (InputStream output = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
            prop.load(output);
        }

        return prop;
    }

    public void saveProperties(Properties prop) throws IOException {
        URL configUrl = Thread.currentThread().getContextClassLoader().getResource("config.properties");

        try (OutputStream output = new FileOutputStream(new File(configUrl.toURI()))) {
            prop.store(output, "");
        } catch (URISyntaxException e) {
            //todo
        }
    }

    public Properties createDefaultProp() {
        Properties prop = new Properties();

        // fill with default values
        prop.setProperty("db.address", "localhost");
        prop.setProperty("db.port", "5432");
        prop.setProperty("db.name", "PT4 DB");
        prop.setProperty("db.user", "postgres");
        prop.setProperty("db.pass", "dbpass");

        return prop;
    }
}