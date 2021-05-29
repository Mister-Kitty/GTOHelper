package com.gtohelper.utility;

import java.io.IOException;
import java.util.HashMap;

public abstract class Saveable {
    SaveFileHelper saveHelper;
    final String objectName;
    final String delimiter = ".";

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

    public void deleteSubGroup(String subgroup) throws IOException {
        saveHelper.deleteSubGroup(getQualifiedFieldName(subgroup));
    }

    public void saveSubGroupTextField(String subGroup, String fieldName, String value) {
        saveTextField(subGroup + delimiter + fieldName, value);
    }

    public void saveTextField(String fieldName, String value) {
        saveHelper.saveTextField(getQualifiedFieldName(fieldName), value);
    }

    public String getQualifiedFieldName(String fieldname) {
        return objectName + delimiter + fieldname;
    }

    // deprecated
    public void saveAll() throws IOException {
        saveHelper.saveAll();
    }

    public boolean saveAllAndPopupOnError() {
        try {
            saveHelper.saveAll();
            return true;
        } catch (IOException e) {
            String error = "Disk error while trying to update config file. Ensure we have write permissions and that the file is not open elsewhere.";
            Logger.log(error);
            Popups.showWarning(error);
            return false;
        }
    }
}
