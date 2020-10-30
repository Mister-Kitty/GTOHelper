package com.gtohelper.datamanager;

import java.sql.*;

public abstract class DataManagerBase {

    protected Connection con;

    public DataManagerBase(Connection connection) {
        con = connection;
    }




}
