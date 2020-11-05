package com.gtohelper.datamanager;

import com.gtohelper.domain.HandData;

import java.sql.SQLException;
import java.util.ArrayList;

// HandData is a mix of cash_hand_summary & cash_hand_statistics, as we often need a both for GUI and Solve generation
public interface IHandDataDM {

    public ArrayList<HandData> getHandDataByTag(int tagId, int playerId) throws SQLException;


}
