package com.gtohelper.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {

    private static String connectionString = "";
    private static String user = "";
    private static String pass = "";

    public static void initialize(String connection, String usr, String pas) {
        connectionString = connection;
        user = usr;
        pass = pas;
    }

    // Deprecated. Will clean this up later.
    public static void buildConnectionProperties() {
        Properties prop = new Properties();

        try (InputStream output = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
            prop.load(output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = prop.getProperty("DBConnection.address");
        String port = prop.getProperty("DBConnection.port");
        String name = prop.getProperty("DBConnection.name");

        user = prop.getProperty("DBConnection.user");
        pass = prop.getProperty("DBConnection.pass");
        connectionString = "jdbc:postgresql://" + address + ":" + port + "/" + name;
    }

    public static Connection getConnection() throws SQLException {
        if(connectionString.isEmpty())
            buildConnectionProperties();

       return DriverManager.getConnection(connectionString, user, pass);
    }
}
