package com.gtohelper.PT4DataManager;

import com.gtohelper.datamanager.DataManagerBase;
import com.gtohelper.datamanager.IGeneralDM;

import java.sql.*;

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
