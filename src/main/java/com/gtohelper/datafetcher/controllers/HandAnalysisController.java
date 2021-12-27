package com.gtohelper.datafetcher.controllers;

import com.gtohelper.datafetcher.models.HandAnalysisModel;
import com.gtohelper.domain.*;
import com.gtohelper.fxml.Board;
import com.gtohelper.fxml.Hand;
import com.gtohelper.utility.Logger;
import com.gtohelper.utility.Popups;
import com.gtohelper.utility.SaveFileHelper;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class HandAnalysisController {

    /*
        Session & Tag tab controls.
     */
    @FXML
    TableView<Tag> tagTable;
    ObservableList<Tag> tagTableItems = FXCollections.observableArrayList();
    @FXML TableColumn<Tag, String> tagTableIdColumn;
    @FXML TableColumn<Tag, String> tagTableTagColumn;

    @FXML
    TableView<SessionBundle> sessionTable;
    ObservableList<SessionBundle> sessionTableItems = FXCollections.observableArrayList();
    @FXML TableColumn<SessionBundle, String> sessionTableDateColumn;
    @FXML TableColumn<SessionBundle, String> sessionTableLengthColumn;
    @FXML TableColumn<SessionBundle, String> sessionTableFlopsColumn;
    @FXML TableColumn<SessionBundle, String> sessionTableHandsColumn;
    @FXML TableColumn<SessionBundle, String> sessionTableMoneyColumn;

    /*
        Position v position tab controls.
     */
    @FXML ChoiceBox<SeatGroup> heroSeatChoiceBox;
    @FXML ComboBox<SeatGroup> villainSeatComboBox;
    @FXML ChoiceBox<String> situationChoiceBox;
    @FXML ChoiceBox<HandData.SolvabilityLevel> solveabilityChoiceBox;

    /*
        Tournament tab controls
     */

    @FXML
    TableView<Tournament> tournamentTable;
    ObservableList<Tournament> tournamentTableItems = FXCollections.observableArrayList();
    @FXML TableColumn<Tournament, String> tournamentTableDateColumn;
    @FXML TableColumn<Tournament, String> tournamentTableHandsColumn;
    @FXML TableColumn<Tournament, String> tournamentTablePlayersColumn;
    @FXML TableColumn<Tournament, String> tournamentTableBuyinColumn;

    /*
        Hand table controls
    */
    @FXML
    TableView<HandData> handsTable;
    ObservableList<HandData> handsTableItems = FXCollections.observableArrayList();
    @FXML TableColumn<HandData, String> handsTableDateColumn;
    @FXML TableColumn<HandData, String> handsTableCWonColumn;
    @FXML TableColumn<HandData, Hand> handsTableHandColumn;
    @FXML TableColumn<HandData, Board> handsTableBoardColumn;


    /*
        Work item build controls
     */

    @FXML TextField workName;
    @FXML ChoiceBox<String> betSizingsChoiceBox;
    @FXML CheckBox rakeHands;
    @FXML RadioButton percentPotRadio, bbOneHundredRadio;
    @FXML TextField percentPotField, bbOneHundredField;

    @FXML Button solveButton;
    ToggleGroup toggleGroup = new ToggleGroup();

    HandAnalysisModel handAnalysisModel;
    Player player;
    Site site;

    @FXML
    private void initialize() {
        initializeSituationMaps();
        initializeControls();
    }

    public void loadModel(SaveFileHelper saveHelper) {
        handAnalysisModel = new HandAnalysisModel(saveHelper);
        loadFieldsFromModel();
    }

    public void onConnectionSuccessStateReceive(Site site, Player player) {
        this.site = site;
        this.player = player;

        try {
            tagTableItems.clear();
            tagTableItems.addAll(handAnalysisModel.getHandTags());

            sessionTableItems.clear();
            sessionTableItems.addAll(handAnalysisModel.getSessionBundles(site.id_site, player.id_player));
            sessionTable.sort();
            Platform.runLater(() -> sessionTable.refresh()); // workaround for column header alignment bug in javafx

            tournamentTableItems.clear();
            tournamentTableItems.addAll(handAnalysisModel.getTournaments(site.id_site, player.id_player));
            tournamentTable.sort();
           // Platform.runLater(() -> sessionTable.refresh());

        }  catch (SQLException ex) {
        }
    }

    public void refreshBetSettings(List<String> betSettings) {
        betSizingsChoiceBox.getItems().clear();
        betSizingsChoiceBox.getItems().addAll(betSettings);
        if(betSettings.size() > 0)
            betSizingsChoiceBox.getSelectionModel().select(0);
    }

    private Work.WorkSettings buildWorkSettings() {
        String workItemName = workName.getText();
        String betSettingName = betSizingsChoiceBox.getSelectionModel().getSelectedItem();
        boolean useRake = rakeHands.isSelected();
        float percentField;
        if(percentPotRadio.isSelected()) {
            percentField = Float.parseFloat(percentPotField.getText());
            return new Work.WorkSettings(workItemName, player, percentField, 0f, useRake, betSettingName);

        } else {
            percentField = Float.parseFloat(bbOneHundredField.getText());
            return new Work.WorkSettings(workItemName, player, 0f, percentField, useRake, betSettingName);
        }
    }

    private BiConsumer<List<HandData>, Work.WorkSettings> solveHandsCallback;
    public void saveSolveHandsCallback(BiConsumer<List<HandData>, Work.WorkSettings> callback) {
        solveHandsCallback = callback;
    }

    @FXML
    private void onClearSelectionGridClicked(MouseEvent event) {
        tagTable.getSelectionModel().clearSelection();
        sessionTable.getSelectionModel().clearSelection();
        handsTableItems.clear();
    }

    void loadFieldsFromModel() {


    }

    private void initializeControls() {
        /*
            Settings controls start here.
         */
        workName.textProperty().addListener((observableValue, oldValue, newValue) -> updateSolveButtonDisabledState());
        percentPotRadio.setToggleGroup(toggleGroup);
        percentPotField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            percentPotRadio.setSelected(true);
            updateSolveButtonDisabledState();
        });
        bbOneHundredRadio.setToggleGroup(toggleGroup);
        bbOneHundredField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            bbOneHundredRadio.setSelected(true);
            updateSolveButtonDisabledState();
        });
        toggleGroup.selectedToggleProperty().addListener((observableValue, oldValue, newValue) -> updateSolveButtonDisabledState());

        initializeSessionAndTagControls();
        initializePositionVPositionControls();
        initializeTournamentControls();

        /*
            Hands table
         */
        handsTable.setItems(handsTableItems);
        handsTable.setPlaceholder(new Label("No hands"));
        handsTable.setFixedCellSize(24.0); // This is a bypass around how the TableView seems to blow up the height of Board objects
        handsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        handsTable.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> updateSolveButtonDisabledState());
        handsTableDateColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().date_played.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        handsTableCWonColumn.setCellValueFactory(p -> new SimpleStringProperty("" + p.getValue().amt_pot));
        handsTableHandColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper(new Hand(p.getValue().getHandDataForPlayer(player.id_player))));
        handsTableHandColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<HandData, Hand> call(TableColumn<HandData, Hand> param) {
                return new TableCell<>() {
                    @Override
                    public void updateItem(Hand hand, boolean empty) {
                        super.updateItem(hand, empty);

                        if (empty || hand == null) {
                            setGraphic(null);
                        } else {
                            setGraphic(hand);
                        }
                    }
                };
            }
        });
        handsTableBoardColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper(new Board(p.getValue())));
        handsTableBoardColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<HandData, Board> call(TableColumn<HandData, Board> param) {
                return new TableCell<>() {
                    @Override
                    public void updateItem(Board board, boolean empty) {
                        super.updateItem(board, empty);

                        if (empty || board == null) {
                            setGraphic(null);
                        } else {
                            setGraphic(board);
                        }
                    }
                };
            }
        });

    }

    /*
        Session and Tag code should be gathered below.
     */

    private void initializeSessionAndTagControls() {
        /*
            Then we'll start with Tag table
         */
        tagTableIdColumn.setCellValueFactory(p -> new SimpleStringProperty("" + p.getValue().id_tag));
        tagTableTagColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().tag));
        tagTable.getSelectionModel().selectedItemProperty().addListener((observableValue, oldTag, newTag) -> {
            if(newTag == null) {
                sessionTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            } else {
                sessionTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            }
        });
        tagTable.setItems(tagTableItems);
        tagTable.setPlaceholder(new Label(""));
        tagTable.setRowFactory(tv -> {
            TableRow<Tag> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(row.isEmpty() && event.getButton()== MouseButton.PRIMARY) {
                    handsTable.getItems().clear();
                    tagTable.getSelectionModel().clearSelection();
                } else if (event.getButton()== MouseButton.PRIMARY && event.getClickCount() == 1) {
                    getSessionAndTagHands();
                }
            });
            return row;
        });

        /*
            Session table
         */
        sessionTable.setItems(sessionTableItems);
        sessionTable.setPlaceholder(new Label(""));
        sessionTable.getSortOrder().add(sessionTableDateColumn);
        sessionTableDateColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getMinSessionStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        sessionTableDateColumn.setSortType(TableColumn.SortType.DESCENDING);
        sessionTableHandsColumn.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().getHandCount())));
        sessionTableFlopsColumn.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().getFlopsCount())));
        sessionTableMoneyColumn.setCellValueFactory(p -> new SimpleStringProperty(new BigDecimal(p.getValue().getAmountWon()).setScale(2, RoundingMode.HALF_UP).toString()));
        sessionTableLengthColumn.setCellValueFactory(p -> new SimpleStringProperty(
                // No elegant way to display this apparently. Use this weird Stack Overflow suggestion.
                String.format("%d:%02d",
                        p.getValue().getDuration().getSeconds()/3600,
                        (p.getValue().getDuration().getSeconds()%3600)/60)
        ));
        sessionTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            getSessionAndTagHands();
        });
    }

    private void getSessionAndTagHands() {
        ObservableList<SessionBundle> sessions = sessionTable.getSelectionModel().getSelectedItems();
        Tag tag = tagTable.getSelectionModel().getSelectedItem();

        ArrayList<HandData> results;
        try {
            if(tag == null && sessions.isEmpty()) {
                return;
            } else if (tag == null) {
                // Without tag, we allow single selection.
                if (sessions.size() > 1)
                    return;

                results = handAnalysisModel.getHandDataBySessionBundle(sessions.get(0), site.id_site, player.id_player);
            } else if (sessions.isEmpty()) {
                // with no sessions, we just bulk select by tag.
                results = handAnalysisModel.getHandDataByTag(tag.id_tag, player.id_player);
            } else {
                // And finally, when we have an at least one of both selected.
                results = handAnalysisModel.getHandDataByTaggedHandsInSessions(sessions, tag.id_tag, player.id_player);
            }
        } catch(SQLException e) {
            Popups.showError("Database exception while trying to fetch hands. See logger for more info.");
            Logger.log(Logger.Channel.HUD, "Database exception while trying to fetch hands.\n");
            Logger.log(e);
            return;
        }

        handsTableItems.clear();
        handsTableItems.addAll(results);
    }

    /*
        Position vs Position code should be gathered below.
     */

    private void initializePositionVPositionControls() {
        heroSeatChoiceBox.getItems().addAll(SeatGroup.allByPreflopPosition);
        heroSeatChoiceBox.setConverter(SeatGroup.indentFormattedSeatGroupConverter);

        villainSeatComboBox.getItems().addAll(SeatGroup.allByPreflopPosition);
        villainSeatComboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<SeatGroup> call(ListView<SeatGroup> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(SeatGroup item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item != null || !empty) {
                            // converter doesn't seem to trigger naturally when setConverter() is used.
                            setText(SeatGroup.indentFormattedSeatGroupConverter.toString(item));

                            SeatGroup heroSeatGroup = heroSeatChoiceBox.getSelectionModel().getSelectedItem();
                            if(heroSeatGroup == item)
                                this.setDisable(true);
                            else if(item == SeatGroup.MP && (heroSeatGroup == SeatGroup.HJ || heroSeatGroup == SeatGroup.LJ))
                                this.setDisable(true);
                            else if(item == SeatGroup.EP && (heroSeatGroup == SeatGroup.Tth_Seat || heroSeatGroup == SeatGroup.UTG ||
                                                                heroSeatGroup == SeatGroup.UTG1 || heroSeatGroup == SeatGroup.UTG2))
                                this.setDisable(true);
                            else
                                this.setDisable(false);
                        }
                    }
                };
            }
        });

        solveabilityChoiceBox.getItems().addAll(HandData.SolvabilityLevel.values());
        solveabilityChoiceBox.getSelectionModel().select(HandData.SolvabilityLevel.MULTI_PRE_HU_FLOP);
    }

    /*
        And finally, Tournament controls
     */

    private void initializeTournamentControls() {
        tournamentTable.setItems(tournamentTableItems);
        tournamentTable.setPlaceholder(new Label(""));
        tournamentTable.getSortOrder().add(tournamentTableDateColumn);

        // Ideally, these should have accessors
        tournamentTableDateColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().date_start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        tournamentTableDateColumn.setSortType(TableColumn.SortType.DESCENDING);
        tournamentTableHandsColumn.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().cnt_hands)));
        tournamentTablePlayersColumn.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().cnt_players)));
        tournamentTableBuyinColumn.setCellValueFactory(p -> new SimpleStringProperty(new BigDecimal(p.getValue().amt_buyin).setScale(2, RoundingMode.HALF_UP).toString()));

        tournamentTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            getTournamentHands();
        });
    }

    private void getTournamentHands() {
        Tournament selectedTournament = tournamentTable.getSelectionModel().getSelectedItem();


        ArrayList<HandData> results;
        try {

            results = handAnalysisModel.getHandDataByTournament(selectedTournament.id_tourney, player.id_player);


        } catch(SQLException e) {
            Popups.showError("Database exception while trying to fetch hands. See logger for more info.");
            Logger.log(Logger.Channel.HUD, "Database exception while trying to fetch hands.\n");
            Logger.log(e);
            return;
        }

        handsTableItems.clear();
        handsTableItems.addAll(results);
    }

    /*
        FXML and etc below
     */

    @FXML
    private void heroSeatSelected() {
        villainSeatComboBox.setDisable(false);

        // This is gross ... But AFAIK there's actually no refresh() or equivilent for this. Only this works.
        List<SeatGroup> items = List.copyOf(villainSeatComboBox.getItems());
        villainSeatComboBox.getItems().clear();
        villainSeatComboBox.getItems().addAll(items);
    }

    @FXML
    private void villainSeatSelected() {
        SeatGroup heroSeatGroup = heroSeatChoiceBox.getSelectionModel().getSelectedItem();
        SeatGroup villainSeatGroup = villainSeatComboBox.getSelectionModel().getSelectedItem();

        // this function triggers when heroSeat changes, even though I don't think it should.
        if(heroSeatGroup == null || villainSeatGroup == null)
            return;

        situationChoiceBox.setDisable(false);
        situationChoiceBox.getItems().clear();
        boolean heroIsIP = heroSeatGroup.areWeIPPreflop(villainSeatGroup);
        if(heroIsIP)
            heroIPSituationList.forEach(k -> situationChoiceBox.getItems().add(k.situationName));
        else
            heroOOPSituationList.forEach(k -> situationChoiceBox.getItems().add(k.situationName));

    }

    @FXML
    private void situationSelected() {
        String situationString = situationChoiceBox.getSelectionModel().getSelectedItem();
        if(situationString == null)
            return;

        getPositionVsPositionHands();
    }

    @FXML
    private void solvabilitySelected() {
        getPositionVsPositionHands();
    }

    private class SituationComboBoxMapping {
        final String situationName;
        final Situation mappedSituation;
        final LastAction mappedAction;
        public SituationComboBoxMapping(String name, Situation situation, LastAction action) {
            situationName = name; mappedSituation = situation; mappedAction = action;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof SituationComboBoxMapping)) { return false; }
            return situationName.equals(((SituationComboBoxMapping)obj).situationName);
        }
    }

    // These class members are down here to keep them obscure. They're really only used in the above 3 functions and shouldn't be edited.
    private ArrayList<SituationComboBoxMapping> heroIPSituationList = new ArrayList<>() ;
    private ArrayList<SituationComboBoxMapping> heroOOPSituationList = new ArrayList<>();
    private void initializeSituationMaps() {
        heroIPSituationList.add(new SituationComboBoxMapping("Limp - Hero calls vs Villain", Situation.LIMP, LastAction.CALL));
        heroIPSituationList.add(new SituationComboBoxMapping("2Bet - Hero calls vs Villain", Situation.VRFI, LastAction.CALL));
        heroIPSituationList.add(new SituationComboBoxMapping("3Bet - Villain calls vs Hero", Situation.VRFI, LastAction.RAISE));
        heroIPSituationList.add(new SituationComboBoxMapping("4Bet - Hero calls vs Villain", Situation.V4BET, LastAction.CALL));
        heroIPSituationList.add(new SituationComboBoxMapping("5Bet - Villain calls vs Hero", Situation.V4BET, LastAction.RAISE));

        heroOOPSituationList.add(new SituationComboBoxMapping("Limp - Villain calls vs Hero", Situation.LIMP, LastAction.CALL));
        heroOOPSituationList.add(new SituationComboBoxMapping("2Bet - Villain calls vs Hero", Situation.RFI, LastAction.RAISE));
        heroOOPSituationList.add(new SituationComboBoxMapping("3Bet - Hero calls vs Villain", Situation.V3BET, LastAction.CALL));
        heroOOPSituationList.add(new SituationComboBoxMapping("4Bet - Villain calls vs Hero", Situation.V3BET, LastAction.RAISE));
        heroOOPSituationList.add(new SituationComboBoxMapping("5Bet - Hero calls vs Villain", Situation.CALL5BET, LastAction.CALL));
    }

    private void getPositionVsPositionHands() {
        SeatGroup heroSeatGroup = heroSeatChoiceBox.getSelectionModel().getSelectedItem();
        SeatGroup villainSeatGroup = villainSeatComboBox.getSelectionModel().getSelectedItem();
        String situationString = situationChoiceBox.getSelectionModel().getSelectedItem();
        HandData.SolvabilityLevel solvability = solveabilityChoiceBox.getSelectionModel().getSelectedItem();

        if(heroSeatGroup == null || villainSeatGroup == null || solvability == null || situationString == null || situationString.isEmpty())
            return;

        SituationComboBoxMapping mapping;
        boolean heroIsIP = heroSeatGroup.areWeIPPreflop(villainSeatGroup);
        if(heroIsIP)
            mapping = heroIPSituationList.stream().filter(s -> s.situationName.equals(situationString)).findFirst().get();
        else
            mapping = heroOOPSituationList.stream().filter(s -> s.situationName.equals(situationString)).findFirst().get();

        ArrayList<HandData> results;
        try {
            results = handAnalysisModel.getHandDataByPositionVsPosition(heroSeatGroup, villainSeatGroup,
                                    mapping.mappedSituation, mapping.mappedAction, solvability, player.id_player);
        } catch(SQLException e) {
            Popups.showError("Database exception while trying to fetch hands. See logger for more info.");
            Logger.log(Logger.Channel.HUD, "Database exception while trying to fetch hands.\n");
            Logger.log(e);
            return;
        }

        handsTableItems.clear();
        handsTableItems.addAll(results);
    }

    /*
        Hand table and associated buttons/field code should be gathered below.
     */

    @FXML
    private void selectAll() {
        handsTable.getSelectionModel().selectAll();
    }

    @FXML
    private void solveSelected() {
        List<HandData> handsToSolve = handsTable.getSelectionModel().getSelectedItems();
        solveHandsCallback.accept(handsToSolve, buildWorkSettings());
    }

    private void updateSolveButtonDisabledState() {
        if(areAllSolveFieldsValid())
            solveButton.disableProperty().setValue(false);
        else
            solveButton.disableProperty().setValue(true);
    }

    private boolean areAllSolveFieldsValid() {
        if(!workName.getText().isEmpty() && !handsTable.getSelectionModel().getSelectedItems().isEmpty() &&
                !betSizingsChoiceBox.getSelectionModel().getSelectedItem().isEmpty()) {
            if(toggleGroup.getSelectedToggle() != null) {
                RadioButton button = (RadioButton) toggleGroup.getSelectedToggle();
                if(button == percentPotRadio) { // note: object reference address comparison is intentional
                    if(!percentPotField.getText().isEmpty()) {
                        try { Float.parseFloat(percentPotField.getText());  } catch (NumberFormatException e) { return false; }
                        return true;
                    }
                } else {
                    if(!bbOneHundredField.getText().isEmpty()) {
                        try { Float.parseFloat(bbOneHundredField.getText());  } catch (NumberFormatException e) { return false; }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
