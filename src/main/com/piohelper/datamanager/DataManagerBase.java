package com.piohelper.datamanager;

import com.piohelper.datafetcher.DataFetcher;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DataManagerBase {

    protected Connection con;

    public DataManagerBase(Connection connection) {
        con = connection;
    }




}
