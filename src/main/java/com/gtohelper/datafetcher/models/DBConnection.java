package com.gtohelper.datafetcher.models;

import com.gtohelper.PT4DataManager.PT4GeneralDM;
import com.gtohelper.datamanager.IGeneralDM;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection  {
    public String getVersionTest(String url, String user, String password) throws SQLException {
        try (Connection con = getConnection(url, user, password);) {
            IGeneralDM generalDM = new PT4GeneralDM(con);
            return generalDM.getDBVersion();
        } catch(SQLException e) {
            throw e;
        }
    }

    public Connection getConnection(String url, String user, String password) throws SQLException {
        return DriverManager.getConnection(url, user, password);
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
