package com.gtohelper.datamanager;

import com.gtohelper.domain.SessionBundle;
import com.gtohelper.domain.Tournament;

import java.sql.SQLException;
import java.util.ArrayList;

public interface ITournamentDM {

    ArrayList<Tournament> getAllTournaments(int siteId, int playerId) throws SQLException;


}
