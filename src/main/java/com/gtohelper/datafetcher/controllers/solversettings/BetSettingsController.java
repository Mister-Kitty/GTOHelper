package com.gtohelper.datafetcher.controllers.solversettings;

import com.gtohelper.datafetcher.models.solversettings.BetSettingsModel;
import com.gtohelper.domain.BettingOptions;
import com.gtohelper.utility.Popups;
import com.gtohelper.utility.SaveFileHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
    CheckBox flopAllinIP, turnAllinIP, riverAllinIP;
    @FXML
    CheckBox flopDont3BetPlus, turnDont3BetPlus, riverDont3BetPlus;

    @FXML
    TextField flopCBetOOP, flopDonkOOP, flopRaiseOOP,
            turnBetOOP, turnDonkOOP, turnRaiseOOP,
            riverBetOOP, riverDonkOOP, riverRaiseOOP;
    @FXML
    CheckBox flopAllinOOP, turnAllinOOP, riverAllinOOP;

    @FXML
    TextField allInThresholdPercent, addAllinOnlyIfPercentage;

    @FXML
    Button saveButton, deleteButton;

    boolean saveIsOverwrite = false;

    Consumer<List<String>> callback;

    BetSettingsModel betSettingsModel;

    private BettingOptions buildGameTreeData() {
        BettingOptions data = new BettingOptions(settingsName.getText());
        try {
            data.options.allInThresholdPercent = Integer.parseInt(allInThresholdPercent.getText());
        } catch(NumberFormatException e) {
            String error = "'Allin threshold' value is invalid.";
            Popups.showError(error);
            return null;
        }
        try {
            data.options.addAllinOnlyIfPercentage = Integer.parseInt(addAllinOnlyIfPercentage.getText());
        } catch(NumberFormatException e) {
            String error = "'Add allin only if' value is invalid.";
            Popups.showError(error);
            return null;
        }

        data.IPFlop.setActionData(flopAllinIP.isSelected(), flopDont3BetPlus.isSelected(), flopBetIP.getText(), flopRaiseIP.getText());
        data.IPTurn.setActionData(turnAllinIP.isSelected(), turnDont3BetPlus.isSelected(), turnBetIP.getText(), turnRaiseIP.getText());
        data.IPRiver.setActionData(riverAllinIP.isSelected(), riverDont3BetPlus.isSelected(), riverBetIP.getText(), riverRaiseIP.getText());

        data.OOPFlop.setActionData(flopAllinOOP.isSelected(),  flopCBetOOP.getText(), flopRaiseOOP.getText(), flopDonkOOP.getText());
        data.OOPTurn.setActionData(turnAllinOOP.isSelected(),  turnBetOOP.getText(), turnRaiseOOP.getText(), turnDonkOOP.getText());
        data.OOPRiver.setActionData(riverAllinOOP.isSelected(),  riverBetOOP.getText(), riverRaiseOOP.getText(), riverDonkOOP.getText());

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
        BettingOptions newItem = buildGameTreeData();
        if(newItem == null)
            return;

        String settingName = settingsName.getText();
        saveToBetSettingsNames(settingName);

        savedBetSettingsTable.getItems().add(newItem);
        savedBetSettingsTable.getSelectionModel().select(newItem);

        betSettingsModel.saveSubGroupTextField(settingName, "allInThresholdPercent", allInThresholdPercent.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "addAllinOnlyIfPercentage", addAllinOnlyIfPercentage.getText());

        betSettingsModel.saveSubGroupTextField(settingName, "flopBetIP", flopBetIP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "flopRaiseIP", flopRaiseIP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "turnBetIP", turnBetIP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "turnRaiseIP", turnRaiseIP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "riverBetIP", riverBetIP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "riverRaiseIP", riverRaiseIP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "flopAllinIP", Boolean.toString(flopAllinIP.isSelected()));
        betSettingsModel.saveSubGroupTextField(settingName, "turnAllinIP", Boolean.toString(turnAllinIP.isSelected()));
        betSettingsModel.saveSubGroupTextField(settingName, "riverAllinIP", Boolean.toString(riverAllinIP.isSelected()));
        betSettingsModel.saveSubGroupTextField(settingName, "flopDont3BetPlus", Boolean.toString(flopDont3BetPlus.isSelected()));
        betSettingsModel.saveSubGroupTextField(settingName, "turnDont3BetPlus", Boolean.toString(turnDont3BetPlus.isSelected()));
        betSettingsModel.saveSubGroupTextField(settingName, "riverDont3BetPlus", Boolean.toString(riverDont3BetPlus.isSelected()));

        betSettingsModel.saveSubGroupTextField(settingName, "flopCBetOOP", flopCBetOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "flopDonkOOP", flopDonkOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "flopRaiseOOP", flopRaiseOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "turnBetOOP", turnBetOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "turnDonkOOP", turnDonkOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "turnRaiseOOP", turnRaiseOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "riverBetOOP", riverBetOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "riverDonkOOP", riverDonkOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "riverRaiseOOP", riverRaiseOOP.getText());
        betSettingsModel.saveSubGroupTextField(settingName, "flopAllinOOP", Boolean.toString(flopAllinOOP.isSelected()));
        betSettingsModel.saveSubGroupTextField(settingName, "turnAllinOOP", Boolean.toString(turnAllinOOP.isSelected()));
        betSettingsModel.saveSubGroupTextField(settingName, "riverAllinOOP", Boolean.toString(riverAllinOOP.isSelected()));

        betSettingsModel.saveAllAndPopupOnError();
    }

    private void saveToBetSettingsNames(String settingName) {
        String listOfSettingsNames = betSettingsModel.loadTextField("betSettingsNames");
        if(listOfSettingsNames.isEmpty()) {
            listOfSettingsNames = settingName;
        } else {
            if(!Arrays.asList(listOfSettingsNames.split(",")).contains(settingName))
                listOfSettingsNames += "," + settingName;
        }
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
        savedBetSettingsTable.getSelectionModel().select(0);

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
        addAllinOnlyIfPercentage.setText(String.valueOf(treeData.options.addAllinOnlyIfPercentage));
        allInThresholdPercent.setText(String.valueOf(treeData.options.allInThresholdPercent));

        flopBetIP.setText(treeData.IPFlop.getBets().getInitialString());
        flopRaiseIP.setText(treeData.IPFlop.getRaises().getInitialString());
        turnBetIP.setText(treeData.IPTurn.getBets().getInitialString());
        turnRaiseIP.setText(treeData.IPTurn.getRaises().getInitialString());
        riverBetIP.setText(treeData.IPRiver.getBets().getInitialString());
        riverRaiseIP.setText(treeData.IPRiver.getRaises().getInitialString());
        flopAllinIP.setSelected(treeData.IPFlop.getAddAllIn());
        turnAllinIP.setSelected(treeData.IPTurn.getAddAllIn());
        riverAllinIP.setSelected(treeData.IPRiver.getAddAllIn());
        flopDont3BetPlus.setSelected(treeData.IPFlop.getDont3BetPlus());
        turnDont3BetPlus.setSelected(treeData.IPTurn.getDont3BetPlus());
        riverDont3BetPlus.setSelected(treeData.IPRiver.getDont3BetPlus());

        flopCBetOOP.setText(treeData.OOPFlop.getBets().getInitialString());
        flopDonkOOP.setText(treeData.OOPFlop.getDonks().getInitialString());
        flopRaiseOOP.setText(treeData.OOPFlop.getRaises().getInitialString());
        turnBetOOP.setText(treeData.OOPTurn.getBets().getInitialString());
        turnDonkOOP.setText(treeData.OOPTurn.getDonks().getInitialString());
        turnRaiseOOP.setText(treeData.OOPTurn.getRaises().getInitialString());
        riverBetOOP.setText(treeData.OOPRiver.getBets().getInitialString());
        riverDonkOOP.setText(treeData.OOPRiver.getDonks().getInitialString());
        riverRaiseOOP.setText(treeData.OOPRiver.getRaises().getInitialString());
        flopAllinOOP.setSelected(treeData.OOPFlop.getAddAllIn());
        turnAllinOOP.setSelected(treeData.OOPTurn.getAddAllIn());
        riverAllinOOP.setSelected(treeData.OOPRiver.getAddAllIn());
    }

    private void loadIntoTreeData(BettingOptions treeData, String key, String value) {
        switch(key) {
            case "addAllinOnlyIfPercentage":
                treeData.options.addAllinOnlyIfPercentage = Integer.parseInt(value);
                break;
            case "allInThresholdPercent":
                treeData.options.allInThresholdPercent = Integer.parseInt(value);
                break;

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
            case "flopAllinIP":
                treeData.IPFlop.setAddAllIn(Boolean.parseBoolean(value));
                break;
            case "turnAllinIP":
                treeData.IPTurn.setAddAllIn(Boolean.parseBoolean(value));
                break;
            case "riverAllinIP":
                treeData.IPRiver.setAddAllIn(Boolean.parseBoolean(value));
                break;
            case "flopDont3BetPlus":
                treeData.IPFlop.setDont3BetPlus(Boolean.parseBoolean(value));
                break;
            case "turnDont3BetPlus":
                treeData.IPTurn.setDont3BetPlus(Boolean.parseBoolean(value));
                break;
            case "riverDont3BetPlus":
                treeData.IPRiver.setDont3BetPlus(Boolean.parseBoolean(value));
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
            case "flopAllinOOP":
                treeData.OOPFlop.setAddAllIn(Boolean.parseBoolean(value));
                break;
            case "turnAllinOOP":
                treeData.OOPTurn.setAddAllIn(Boolean.parseBoolean(value));
                break;
            case "riverAllinOOP":
                treeData.OOPRiver.setAddAllIn(Boolean.parseBoolean(value));
                break;
            default:
                // todo: log error
                break;
        }

    }


}
