package com.gtohelper.datafetcher.controllers.solversettings;

import com.gtohelper.domain.BetSettings;
import com.gtohelper.utility.SaveFileHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class BetSettingsController {

    @FXML
    TableView<BetSettings> savedBetSettingsTable;
    @FXML TableColumn<BetSettings, String> savedBetSettingsTableNameColumn;

    @FXML
    TextField settingsName;

    @FXML
    TextField flopBetIP, flopRaiseIP, turnBetIP,
            turnRaiseIP, riverBetIP, riverRaiseIP;

    @FXML
    TextField flopCBetOOP, flopDonkOOP, flopRaiseOOP,
            turnBetOOP, turnDonkOOP, turnRaiseOOP,
            riverBetOOP, riverDonkOOP, riverRaiseOOP;

    @FXML
    Button saveButton, deleteButton;

    boolean saveIsOverwrite = false;

    Consumer<List<String>> callback;

    com.gtohelper.datafetcher.models.solversettings.BetSettings betSettings;

    private BetSettings buildGameTreeData() {
        BetSettings data = new BetSettings(settingsName.getText());

        data.IPFlop.setActionData(false, false, flopBetIP.getText(), flopRaiseIP.getText());
        data.IPTurn.setActionData(false, false, turnBetIP.getText(), turnRaiseIP.getText());
        data.IPRiver.setActionData(false, false, riverBetIP.getText(), riverRaiseIP.getText());

        data.OOPFlop.setActionData(false,  flopCBetOOP.getText(), flopRaiseOOP.getText(), flopDonkOOP.getText());
        data.OOPTurn.setActionData(false,  turnBetOOP.getText(), turnRaiseOOP.getText(), turnDonkOOP.getText());
        data.OOPRiver.setActionData(false,  riverBetOOP.getText(), riverRaiseOOP.getText(), riverDonkOOP.getText());

        return data;
    }

    public void loadModel(SaveFileHelper saveHelper) {
        betSettings = new com.gtohelper.datafetcher.models.solversettings.BetSettings(saveHelper);
        loadFieldsFromModel();
    }

    public void saveBetSettingsChangedCallback(Consumer<List<String>> callback) {
        this.callback = callback;
    }

    @FXML
    private void initialize() {
        initializeControls();
    }

    private void initializeControls() {
        settingsName.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.isEmpty()) {
                saveButton.disableProperty().set(true);
            }
            else {
                saveButton.disableProperty().set(false);
                saveButton.textProperty().setValue("New Save");
                saveIsOverwrite = false;

                for(BetSettings data : savedBetSettingsTable.getItems()) {
                    if(data.name.equals(newValue)) {
                        saveButton.setText("Overwrite '" + newValue + "'");
                        saveIsOverwrite = true;
                        break;
                    }
                }
            }
        });

        savedBetSettingsTableNameColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().name));

        savedBetSettingsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                BetSettings selectedItem = savedBetSettingsTable.getSelectionModel().getSelectedItem();
                loadTreeDataIntoGUI(selectedItem);
                deleteButton.disableProperty().set(false);
            } else {
                deleteButton.disableProperty().set(true);
            }
        });

    }

    @FXML
    private void onSaveButtonPress() {
        try {
            String settingName = settingsName.getText();
            saveToBetSettingsNames(settingName);

            BetSettings newItem = buildGameTreeData();
            savedBetSettingsTable.getItems().add(newItem);
            savedBetSettingsTable.getSelectionModel().select(newItem);

            betSettings.saveSubGroupTextField(settingName, "flopBetIP", flopBetIP.getText());
            betSettings.saveSubGroupTextField(settingName, "flopRaiseIP", flopRaiseIP.getText());
            betSettings.saveSubGroupTextField(settingName, "turnBetIP", turnBetIP.getText());
            betSettings.saveSubGroupTextField(settingName, "turnRaiseIP", turnRaiseIP.getText());
            betSettings.saveSubGroupTextField(settingName, "riverBetIP", riverBetIP.getText());
            betSettings.saveSubGroupTextField(settingName, "riverRaiseIP", riverRaiseIP.getText());

            betSettings.saveSubGroupTextField(settingName, "flopCBetOOP", flopCBetOOP.getText());
            betSettings.saveSubGroupTextField(settingName, "flopDonkOOP", flopDonkOOP.getText());
            betSettings.saveSubGroupTextField(settingName, "flopRaiseOOP", flopRaiseOOP.getText());
            betSettings.saveSubGroupTextField(settingName, "turnBetOOP", turnBetOOP.getText());
            betSettings.saveSubGroupTextField(settingName, "turnDonkOOP", turnDonkOOP.getText());
            betSettings.saveSubGroupTextField(settingName, "turnRaiseOOP", turnRaiseOOP.getText());
            betSettings.saveSubGroupTextField(settingName, "riverBetOOP", riverBetOOP.getText());
            betSettings.saveSubGroupTextField(settingName, "riverDonkOOP", riverDonkOOP.getText());
            betSettings.saveSubGroupTextField(settingName, "riverRaiseOOP", riverRaiseOOP.getText());


            betSettings.saveAll();
        } catch (IOException e) {
            //todo log error
        }
    }

    private void saveToBetSettingsNames(String settingName) {
        String listOfSettingsNames = betSettings.loadTextField("betSettingsNames");
        if(listOfSettingsNames.isEmpty())
            listOfSettingsNames = settingName;
        else
            listOfSettingsNames += "," + settingName;
        betSettings.saveTextField("betSettingsNames", listOfSettingsNames);
        callback.accept(Arrays.asList(listOfSettingsNames.split(",")));
    }

    private void deleteFromBetSettingsNames(String settingName) {
        String listOfSettingsNames = betSettings.loadTextField("betSettingsNames");

        String result = null;
        for(String name : listOfSettingsNames.split(",")) {
            if(!name.equals(settingName)) {
                if(result == null)
                    result = name;
                else
                    result += "," + name;
            }
        }

        betSettings.saveTextField("betSettingsNames", result);
    }

    @FXML
    private void onDeleteButtonPress() {
        try {
            // Let's try to remove the data before we purge the GUI
            BetSettings item = savedBetSettingsTable.getSelectionModel().getSelectedItem();
            deleteFromBetSettingsNames(item.name);
            betSettings.deleteSubGroup(item.name);

            // Still here? Cool, delete from props succeeded. Remove from GUI
            savedBetSettingsTable.getItems().remove(item);

        } catch (Exception e) {
            // todo: log me.
            System.out.println(e);
        }
    }

    void loadFieldsFromModel() {
        // We save a list of all subfields (aka all saved bet settings) in a list.
        String listOfSettingsNames = betSettings.loadTextField("betSettingsNames");

        // Because we don't enforce ordering in the props file, we create buckets for each Bet Setting
        ArrayList<BetSettings> settingNameToTreeData = new ArrayList<>();
        for(String settingName : listOfSettingsNames.split(","))
            settingNameToTreeData.add(new BetSettings(settingName));

        // Then we bucketize the read in settings.
        HashMap<String, String> valuesToLoad = betSettings.getAllOurSavedValues();
        valuesToLoad.forEach((k, v) -> {
            if(k.equals("betSettingsNames"))
                return;

            // At this point we have something like "betSetting1.riverRaiseOOP". Split around the '.'
            String[] splitResult = k.split("[.]");
            if(splitResult.length != 2)
                return;

            // There's probably a more elegant data structure for this... but I can't think of it ATM
            BetSettings treeData = null;
            for(BetSettings data : settingNameToTreeData) {
                if(data.name.equals(splitResult[0])) {
                    treeData = data;
                    break;
                }
            }

            if(treeData != null) {
                loadIntoTreeData(treeData, splitResult[1], v);
            } else {
                System.out.println("Treedata for " + splitResult[0] + "." + splitResult[1] + "could not be found");
                // log that
            }

        });

        savedBetSettingsTable.getItems().clear();
        savedBetSettingsTable.getItems().addAll(settingNameToTreeData);

        callback.accept(Arrays.asList(listOfSettingsNames.split(",")));
    }

    public BetSettings getBetSettingByName(String name) {
        for(BetSettings data :savedBetSettingsTable.getItems()) {
            if(data.name.equals(name))
                return data;
        }
        assert false;
        return null;
    }

    private void loadTreeDataIntoGUI(BetSettings treeData) {
        settingsName.setText(treeData.name);

        flopBetIP.setText(treeData.IPFlop.getBets().getInitialString());
        flopRaiseIP.setText(treeData.IPFlop.getRaises().getInitialString());
        turnBetIP.setText(treeData.IPTurn.getBets().getInitialString());
        turnRaiseIP.setText(treeData.IPTurn.getRaises().getInitialString());
        riverBetIP.setText(treeData.IPRiver.getBets().getInitialString());
        riverRaiseIP.setText(treeData.IPRiver.getRaises().getInitialString());

        flopCBetOOP.setText(treeData.OOPFlop.getBets().getInitialString());
        flopDonkOOP.setText(treeData.OOPFlop.getDonks().getInitialString());
        flopRaiseOOP.setText(treeData.OOPFlop.getRaises().getInitialString());
        turnBetOOP.setText(treeData.OOPTurn.getBets().getInitialString());
        turnDonkOOP.setText(treeData.OOPTurn.getDonks().getInitialString());
        turnRaiseOOP.setText(treeData.OOPTurn.getRaises().getInitialString());
        riverBetOOP.setText(treeData.OOPRiver.getBets().getInitialString());
        riverDonkOOP.setText(treeData.OOPRiver.getDonks().getInitialString());
        riverRaiseOOP.setText(treeData.OOPRiver.getRaises().getInitialString());
    }

    private void loadIntoTreeData(BetSettings treeData, String key, String value) {
        switch(key) {
            case "flopBetIP":
                treeData.IPFlop.setBets(value);
                break;
            case "flopRaiseIP":
                treeData.IPFlop.setRaises(value);
                break;
            case "turnBetIP":
                treeData.IPTurn.setBets(value);
                break;
            case "turnRaiseIP":
                treeData.IPTurn.setRaises(value);
                break;
            case "riverBetIP":
                treeData.IPRiver.setBets(value);
                break;
            case "riverRaiseIP":
                treeData.IPRiver.setRaises(value);
                break;

            case "flopCBetOOP":
                treeData.OOPFlop.setBets(value);
                break;
            case "flopDonkOOP":
                treeData.OOPFlop.setDonks(value);
                break;
            case "flopRaiseOOP":
                treeData.OOPFlop.setRaises(value);
                break;
            case "turnBetOOP":
                treeData.OOPTurn.setBets(value);
                break;
            case "turnDonkOOP":
                treeData.OOPTurn.setDonks(value);
                break;
            case "turnRaiseOOP":
                treeData.OOPTurn.setRaises(value);
                break;
            case "riverBetOOP":
                treeData.OOPRiver.setBets(value);
                break;
            case "riverDonkOOP":
                treeData.OOPRiver.setDonks(value);
                break;
            case "riverRaiseOOP":
                treeData.OOPRiver.setRaises(value);
                break;
            default:
                // todo: log error
                break;
        }

    }


}
