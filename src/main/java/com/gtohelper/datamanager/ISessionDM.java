package com.gtohelper.datamanager;

import com.gtohelper.domain.Session;
import com.gtohelper.domain.SessionBundle;

import java.sql.SQLException;
import java.util.ArrayList;

public interface ISessionDM {

    ArrayList<SessionBundle> getAllSessionBundles(int siteId, int playerId) throws SQLException;







}
