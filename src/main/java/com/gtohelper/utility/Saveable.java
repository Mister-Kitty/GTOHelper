package com.gtohelper.utility;

import java.io.IOException;
import java.util.HashMap;

public abstract class Saveable {
    SaveFileHelper saveHelper;
    final String objectName;

    public Saveable(SaveFileHelper s, String name) {
        saveHelper = s;
        objectName = name;
        saveHelper.addSaveable(this);
    }

    public abstract HashMap<String, String> getDefaultValues();

    public String loadTextField(String fieldName) {
        return saveHelper.loadTextField(getQualifiedFieldName(fieldName));
    }

    public HashMap<String, String> getAllOurSavedValues() {
        return saveHelper.getAllValuesFor(objectName);
    }

    public void saveTextField(String fieldName, String value) {
        saveHelper.saveTextField(getQualifiedFieldName(fieldName), value);
    }

    public String getQualifiedFieldName(String fieldname) {
        return objectName + "." + fieldname;
    }

    public void saveAll() throws IOException {
        saveHelper.saveAll();
    }
}
