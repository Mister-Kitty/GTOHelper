package com.gtohelper.datamanager;

import java.sql.SQLException;

public interface IGeneralDM {

    public String getDBVersion() throws SQLException;
}