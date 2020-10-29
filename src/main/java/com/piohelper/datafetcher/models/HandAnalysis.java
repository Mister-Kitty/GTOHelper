package com.piohelper.datafetcher.models;

import com.piohelper.PT4DataManager.PT4LookupDM;
import com.piohelper.datamanager.ILookupDM;
import com.piohelper.domain.Tag;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class HandAnalysis {

    public ArrayList<Tag> getHandTags(String url, String user, String password) throws SQLException {
        Connection con = DriverManager.getConnection(url, user, password);

        ILookupDM lookupDM = new PT4LookupDM(con);
        return lookupDM.getsTagsByType('H');
    }


}
