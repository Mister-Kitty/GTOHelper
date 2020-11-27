package com.gtohelper.utility;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class SaveFileHelper {
    private static Properties props;

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
                String fieldName = key.substring(key.lastIndexOf(".") + 1);
                results.put(fieldName, v.toString());
            }
        });

        return results;
    }

    public void loadProperties() {
        try {
            props = new Properties();

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
