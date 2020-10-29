package com.piohelper.datamanager;

import com.piohelper.domain.Tag;

import java.sql.SQLException;
import java.util.ArrayList;

public interface ILookupDM {

    ArrayList<Tag> getsTagsByType(char type) throws SQLException;





}
