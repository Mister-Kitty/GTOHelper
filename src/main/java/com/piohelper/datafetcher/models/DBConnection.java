package com.piohelper.datafetcher.models;

import com.piohelper.PT4DataManager.PT4GeneralDM;
import com.piohelper.PT4DataManager.PT4HandSummaryDM;
import com.piohelper.datamanager.IGeneralDM;
import javafx.scene.layout.AnchorPane;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection  {
    public String getVersionTest(String url, String user, String password) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);

            IGeneralDM generalDM = new PT4GeneralDM(con);
            return generalDM.getDBVersion();
        } catch (SQLException throwables) {
            return "Connect attempt failed. Error posted in the debug tab.";
           // throwables.printStackTrace();
        }
    }
}
