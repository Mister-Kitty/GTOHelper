package com.gtohelper.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
    public static Connection getConnection() throws SQLException {
        Properties prop = new Properties();

        try (InputStream output = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
            prop.load(output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = prop.getProperty("db.address");
        String port = prop.getProperty("db.port");
        String name = prop.getProperty("db.name");
        String user = prop.getProperty("db.user");
        String password= prop.getProperty("db.pass");
        String url = "jdbc:postgresql://" + address + ":" + port + "/" + name;

       return DriverManager.getConnection(url, user, password);
    }
}
