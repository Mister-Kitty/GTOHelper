package com.gtohelper.utility;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class SaveFileHelper {
    private static SortedProperties props;

    private ArrayList<Saveable> saveables = new ArrayList<Saveable>();
    public void addSaveable(Saveable saveable) {
        saveables.add(saveable);

        // If a required value doesn't load, we load it's default.
        HashMap<String, String> defaultValues = saveable.getDefaultValues();
        defaultValues.forEach((k, v) -> {
            // In this special case we have to qualify the field names for the user.
            // That way, they they can be 100% agnostic of this implementation requirement
            String qualifiedK = saveable.getQualifiedFieldName(k);
            if(props.get(qualifiedK) == null)
                props.put(qualifiedK,v);
        });
    }

    public String loadTextField(String fieldName) {
        return props.getProperty(fieldName);
    }

    public void saveTextField(String fieldName, String value) {
        props.setProperty(fieldName, value);
    }

    public HashMap<String, String> getAllValuesFor(String saveObjectName) {
        HashMap<String, String> results = new HashMap<String, String>();
        String nameWithDelimiter = saveObjectName + ".";

        // We treat savedObjectName as a prefix and build around it.
        props.forEach((k, v) -> {
            String key = k.toString();
            if(key.startsWith(nameWithDelimiter)) {
                String fieldName = key.substring(key.indexOf(".") + 1);
                results.put(fieldName, v.toString());
            }
        });

        return results;
    }

    public void deleteSubGroup(String subgroup) throws IOException {
        // We're given something like BetSettings.setting1, so we need to add delimiter so we don't delete
        // BetSettings.setting11.
        String subgroupWithDelimiter = subgroup + ".";

        // then we need to list all the keys, and remove them in a separate step to avoid loop modification exceptions
        List<String> keyList = new LinkedList<>();
        props.forEach((k, v) -> {
            String key = k.toString();
            if(key.startsWith(subgroupWithDelimiter)) {
                keyList.add(k.toString());
            }
        });

        keyList.forEach(k -> props.remove(k));

        // write back changes to file.
        saveAll();
    }

    public void loadProperties() {
        try {
            props = new SortedProperties();

            try (InputStream output = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
                props.load(output);
            }

       } catch (IOException e) {
            // todo: log in debugger
            //  results.setText("Error when trying to load existing config file. Regeneration and save was successful.");
        }

    }

    public void saveAll() throws IOException {
        URL configUrl = Thread.currentThread().getContextClassLoader().getResource("config.properties");

        try (OutputStream output = new FileOutputStream(new File(configUrl.toURI()))) {
            props.store(output, "");
      //      Database.buildConnectionProperties();
        } catch (URISyntaxException e) {
            //todo
        }
    }











}
