package com.gtohelper.datamanager;

import com.gtohelper.domain.Tag;

import java.sql.SQLException;
import java.util.ArrayList;

public interface ILookupDM {

    ArrayList<Tag> getsTagsByType(char type) throws SQLException;





}
