package com.piohelper.PT4DataManager;

import com.piohelper.datafetcher.models.PioHelper;
import com.piohelper.datamanager.DataManagerBase;
import com.piohelper.datamanager.IGeneralDM;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PT4GeneralDM  extends DataManagerBase implements IGeneralDM {
    public PT4GeneralDM(Connection connection) {
        super(connection);
    }

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
}
