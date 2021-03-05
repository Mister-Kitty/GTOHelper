package com.gtohelper.datafetcher.controllers.solversettings;

import com.gtohelper.datafetcher.models.solversettings.BetSettingsModel;
import com.gtohelper.domain.BettingOptions;
import com.gtohelper.utility.SaveFileHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class BetSettingsController {

    @FXML
    TableView<BettingOptions> savedBetSettingsTable;
    @FXML TableColumn<BettingOptions, String> savedBetSettingsTableNameColumn;

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

    BetSettingsModel betSettingsModel;

    private BettingOptions buildGameTreeData() {
        BettingOptions data = new BettingOptions(settingsName.getText());

        data.IPFlop.setActionData(false, false, flopBetIP.getText(), flopRaiseIP.getText());
        data.IPTurn.setActionData(false, false, turnBetIP.getText(), turnRaiseIP.getText());
        data.IPRiver.setActionData(false, false, riverBetIP.getText(), riverRaiseIP.getText());

        data.OOPFlop.setActionData(false,  flopCBetOOP.getText(), flopRaiseOOP.getText(), flopDonkOOP.getText());
        data.OOPTurn.setActionData(false,  turnBetOOP.getText(), turnRaiseOOP.getText(), turnDonkOOP.getText());
        data.OOPRiver.setActionData(false,  riverBetOOP.getText(), riverRaiseOOP.getText(), riverDonkOOP.getText());

        return data;
    }

    public void loadModel(SaveFileHelper saveHelper) {
        betSettingsModel = new BetSettingsModel(saveHelper);
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

                for(BettingOptions data : savedBetSettingsTable.getItems()) {
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
                BettingOptions selectedItem = savedBetSettingsTable.getSelectionModel().getSelectedItem();
                loadTreeDataIntoGUI(selectedItem);
                deleteButton.disableProperty().set(false);
            } else {
                deleteButton.disableProperty().set(true);
            }
        });

    }

    @FXML
    private void onSaveButtonPress() {
        String settingName = settingsName.getText();
        saveToBetSettingsNames(settingName);

        BettingOptions newItem = buildGameTreeData();
        savedBetSettingsTable.getItems().add(newItem);
        savedBetSettingsTable.getSelectionModel().select(newItem);

        betSettingsModel.saveSubGroupTextField(settingName, "flopBetIP", flopBetIP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "flopRaiseIP", flopRaiseIP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "turnBetIP", turnBetIP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "turnRaiseIP", turnRaiseIP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "riverBetIP", riverBetIP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "riverRaiseIP", riverRaiseIP.getText());

        betSettingsModel.saveSubGroupTextField(settingName, "flopCBetOOP", flopCBetOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "flopDonkOOP", flopDonkOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "flopRaiseOOP", flopRaiseOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "turnBetOOP", turnBetOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "turnDonkOOP", turnDonkOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "turnRaiseOOP", turnRaiseOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "riverBetOOP", riverBetOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "riverDonkOOP", riverDonkOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "riverRaiseOOP", riverRaiseOOP.getText());


        betSettingsModel.saveAllAndPopupOnError();
    }

    private void saveToBetSettingsNames(String settingName) {
        String listOfSettingsNames = betSettingsModel.loadTextField("betSettingsNames");
        if(listOfSettingsNames.isEmpty())
            listOfSettingsNames = settingName;
        else
            listOfSettingsNames += "," + settingName;
        betSettingsModel.saveTextField("betSettingsNames", listOfSettingsNames);
        callback.accept(Arrays.asList(listOfSettingsNames.split(",")));
    }

    private void deleteFromBetSettingsNames(String settingName) {
        String listOfSettingsNames = betSettingsModel.loadTextField("betSettingsNames");

        String result = null;
        for(String name : listOfSettingsNames.split(",")) {
            if(!name.equals(settingName)) {
                if(result == null)
                    result = name;
                else
                    result += "," + name;
            }
        }

        betSettingsModel.saveTextField("betSettingsNames", result);
    }

    @FXML
    private void onDeleteButtonPress() {
        try {
            // Let's try to remove the data before we purge the GUI
            BettingOptions item = savedBetSettingsTable.getSelectionModel().getSelectedItem();
            deleteFromBetSettingsNames(item.name);
            betSettingsModel.deleteSubGroup(item.name);

            // Still here? Cool, delete from props succeeded. Remove from GUI
            savedBetSettingsTable.getItems().remove(item);

        } catch (Exception e) {
            // todo: log me.
            System.out.println(e);
        }
    }

    void loadFieldsFromModel() {
        // We save a list of all subfields (aka all saved bet settings) in a list.
        String listOfSettingsNames = betSettingsModel.loadTextField("betSettingsNames");

        // Because we don't enforce ordering in the props file, we create buckets for each Bet Setting
        ArrayList<BettingOptions> settingNameToTreeData = new ArrayList<>();
        for(String settingName : listOfSettingsNames.split(","))
            settingNameToTreeData.add(new BettingOptions(settingName));

        // Then we bucketize the read in settings.
        HashMap<String, String> valuesToLoad = betSettingsModel.getAllOurSavedValues();
        valuesToLoad.forEach((k, v) -> {
            if(k.equals("betSettingsNames"))
                return;

            // At this point we have something like "betSetting1.riverRaiseOOP". Split around the '.'
            String[] splitResult = k.split("[.]");
            if(splitResult.length != 2)
                return;

            // There's probably a more elegant data structure for this... but I can't think of it ATM
            BettingOptions treeData = null;
            for(BettingOptions data : settingNameToTreeData) {
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

    public BettingOptions getBetSettingByName(String name) {
        for(BettingOptions data :savedBetSettingsTable.getItems()) {
            if(data.name.equals(name))
                return data;
        }
        assert false;
        return null;
    }

    private void loadTreeDataIntoGUI(BettingOptions treeData) {
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

    private void loadIntoTreeData(BettingOptions treeData, String key, String value) {
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
