package org.culpan.mastertools.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.culpan.mastertools.AppHelper;
import org.culpan.mastertools.dao.EncounterDao;
import org.culpan.mastertools.dao.MonsterDao;
import org.culpan.mastertools.dao.PartyDao;
import org.culpan.mastertools.dao.SessionDao;
import org.culpan.mastertools.model.*;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainDialogController implements Initializable {
    private final static EncounterDao encounterDao = new EncounterDao();
    private final static PartyDao partyDao = new PartyDao();
    private final static MonsterDao monsterDao = new MonsterDao();
    private final static SessionDao sessionDao = new SessionDao();

    private final static DateFormat sessionDateFormat = new SimpleDateFormat("EEE, MMM d, yyyy  h:mm aaa");

    // 20x4
    private static final int xpThreasholds[][] = {
        {25, 50, 75, 100},
        {50, 100, 150, 200},
        {75, 150, 225, 400},
        {125, 250, 375, 500},
        {250, 500, 750, 1100},
        {300, 600, 900, 1400},
        {350, 750, 1100, 1700},
        {450, 900, 1400, 2100},
        {550, 1100, 1600, 2400},
        {600, 1200, 1900, 2800},
        {800, 1600, 2400, 3600},
        {1000, 2000, 3000, 4500},
        {1100, 2200, 3400, 5100},
        {1250, 2500, 3800, 5700},
        {1400, 2800, 4300, 6400},
        {1600, 3200, 4800, 7200},
        {2000, 3900, 5900, 8800},
        {2100, 4200, 6300, 9500},
        {2400, 4900, 7300, 10900},
        {2800, 5700, 8500, 12700}
    };

    private Encounter encounter;

    private Party party;

    @FXML
    TableView<Monster> tableMonsters;

    @FXML
    Label labelXp;

    @FXML
    Label labelSession;

    @FXML
    Menu menuCombatant;

    public Encounter getEncounter() {
        return encounter;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public TableView<Monster> getTableMonsters() {
        return tableMonsters;
    }

    public void setTableMonsters(TableView<Monster> tableMonsters) {
        this.tableMonsters = tableMonsters;
    }

    public Menu getMenuCombatant() {
        return menuCombatant;
    }

    public void setMenuCombatant(Menu menuCombatant) {
        this.menuCombatant = menuCombatant;
    }

    public void editConditions() {
        if (tableMonsters.getSelectionModel().getSelectedItems().size() == 0) return;

        Set<String> conditions = new HashSet<>();
        for (Monster m : tableMonsters.getSelectionModel().getSelectedItems()) {
            for (MonsterCondition monsterCondition : m.getConditions()) {
                conditions.add(monsterCondition.getCondition());
            }
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/ConditionsEditDialog.fxml"));
        try {
            Stage primaryStage = new Stage();
            Parent root = loader.load();
            ConditionsEditDialogController controller = loader.getController();
            controller.initialize(conditions, tableMonsters.getSelectionModel().getSelectedItems());

            primaryStage.initModality(Modality.APPLICATION_MODAL);
            primaryStage.setScene(new Scene(root));
            primaryStage.showAndWait();
            encounter = encounterDao.getCurrentEncounter();
            refreshScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeAllConditions() {
        if (tableMonsters.getSelectionModel().getSelectedItems().size() == 0) return;

        for (Monster m : tableMonsters.getSelectionModel().getSelectedItems()) {
            monsterDao.deleteConditions(m, false);
        }
        monsterDao.commit();
        refreshScreen();
    }

    public void clearEncounter() {
        if (tableMonsters.getItems().size() == 0 || !okToClear()) return;

        encounterDao.deleteMonsters(encounter);

        refreshScreen();
    }

    public void saveXp() {
        if (tableMonsters.getItems().size() == 0) return;

        saveXpDialog();
    }

    private boolean saveXpDialog() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("XP Description");
        dialog.setHeaderText("Give a description of the encounter");
        dialog.setContentText("Description");

        Optional<String> descr = dialog.showAndWait();
        if (descr.isPresent()) {
            Session session = sessionDao.getOpenSession();
            sessionDao.addXpToSession(session, encounter.getTotalXp(), descr.get());
            return true;
        } else {
            return false;
        }
    }

    private boolean okToClear() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Enounter");
        alert.setHeaderText("You may save the encounter XP before clearing, or cancel.");
        alert.setContentText("Choose your option.");

        ButtonType buttonTypeOne = new ButtonType("Save XP and Clear");
        ButtonType buttonTypeTwo = new ButtonType("Clear");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne){
            return saveXpDialog();
        } else if (result.get() == buttonTypeTwo) {
            return true;
        } else {
            return false;
        }
    }

    private void updateTotalXp() {
        long totalXp = encounter.getTotalXp();
        String xpText = "Total XP: " + NumberFormat.getNumberInstance(Locale.US).format(totalXp);
        xpText += "  -  " + calculateDifficulty(party.getMemberCount(), party.getAverageLevel(), totalXp);
        labelXp.setText(xpText);
    }

    public void refreshScreen() {
        tableMonsters.getItems().clear();

        updateTotalXp();

        updateSessionInfo();

        tableMonsters.getItems().addAll(encounter.getMonsters());

        tableMonsters.refresh();
    }

    private void updateSessionInfo() {
        Session session = sessionDao.getOpenSession();
        if (session == null) {
            labelSession.setText("No open session");
        } else {
            labelSession.setText("Current session started:   " + sessionDateFormat.format(session.getStartDate()));
        }
    }

    public void deleteMonster() {
        List<Monster> monsters = tableMonsters.getSelectionModel().getSelectedItems();
        for (Monster item : monsters) {
            monsterDao.delete(item);
        }
        encounter = encounterDao.getCurrentEncounter();
        refreshScreen();
    }

    protected void damageMonsters(List<Monster> monsters) {
        if (monsters == null || monsters.size() == 0) return;

        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Damage Combatant");
        dialog.setHeaderText("How much damage to do");
        dialog.setContentText("Amount:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            try {
                int amt = Integer.parseInt(result.get().trim());
                for (Monster data : monsters) {
                    data.setHealth(data.getHealth() - amt);
                    if (data.getHealth() < 0) {
                        data.setHealth(0);
                    }
                    if (data.isSummoned() && data.getHealth() == 0) {
                        monsterDao.delete(data);
                        encounter = encounterDao.getCurrentEncounter();
                    } else {
                        monsterDao.addOrUpdate(data);
                    }
                }
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Number");
                alert.setHeaderText("Failed to parse input");
                alert.setContentText("The input '" + result.get() + "' is not a valid number");

                alert.showAndWait();
            }
            refreshScreen();
        }
    }

    protected void healMonsters(List<Monster> monsters) {
        if (monsters == null || monsters.size() == 0) return;

        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Heal Combatant");
        dialog.setHeaderText("How much to heal");
        dialog.setContentText("Amount:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            try {
                int amt = Integer.parseInt(result.get().trim());
                for (Monster data : monsters) {
                    data.setHealth(data.getHealth() + amt);
                    if (data.getHealth() > data.getBaseHp()) {
                        data.setHealth(data.getBaseHp());
                    }
                    monsterDao.addOrUpdate(data);
                }
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Number");
                alert.setHeaderText("Failed to parse input");
                alert.setContentText("The input '" + result.get() + "' is not a valid number");

                alert.showAndWait();
            }
            tableMonsters.refresh();
        }
    }

    public void damage() {
        damageMonsters(tableMonsters.getSelectionModel().getSelectedItems());
    }

    public void damageWithSaves() {
        damageMonstersWithSaves(tableMonsters.getSelectionModel().getSelectedItems());
    }

    public void heal() {
        healMonsters(tableMonsters.getSelectionModel().getSelectedItems());
    }

    public void healToFull() {
        List<Monster> monsters = tableMonsters.getSelectionModel().getSelectedItems();
        if (monsters == null) return;

        for (Monster m : monsters) {
            m.setHealth(m.getBaseHp());
            monsterDao.addOrUpdate(m, false);
        }

        monsterDao.commit();
        tableMonsters.refresh();
    }

    public void addMore() {
        addMoreMonster(tableMonsters.getSelectionModel().getSelectedItems());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (encounter == null) {
            encounter = encounterDao.getCurrentEncounter();
        }

        if (party == null) {
            party = partyDao.getCurrentParty();
        }

        MainDialogInitialize initializer = new MainDialogInitialize(this);
        initializer.initialize();
    }

    private void addMoreMonster(ObservableList<Monster> selectedItems) {
        if (selectedItems == null || selectedItems.size() != 1) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Selection");
            alert.setHeaderText("You selected multiple monsters");
            alert.setContentText("Select only one monster to clone");

            alert.showAndWait();
            return;
        }

        Monster monster = selectedItems.get(0);

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Clone Monster");
        dialog.setHeaderText("How many new copies would you like to add");
        dialog.setContentText("Count:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            try {
                int amt = Integer.parseInt(result.get().trim());
                encounterDao.addMonstersToCurrentEncounter(monster, amt);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Number");
                alert.setHeaderText("Failed to parse number");
                alert.setContentText("'" + result.get() + "' is not a valid number");

                alert.showAndWait();
                return;
            }
            encounter = encounterDao.getCurrentEncounter();
            refreshScreen();
        }
    }

    private void damageMonstersWithSaves(ObservableList<Monster> selectedItems) {
        if (selectedItems == null || selectedItems.size() == 0) return;

        Stage primaryStage = new Stage();
        primaryStage.setTitle("Damage with Saves");
        primaryStage.setWidth(387);
        primaryStage.setHeight(455);

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/DamageWithSave.fxml"));
        try {
            Parent root = loader.load();
            DamageWithSaveDialogController controller = loader.getController();
            controller.initialize(selectedItems);

            primaryStage.initModality(Modality.APPLICATION_MODAL);
            primaryStage.setScene(new Scene(root));
            primaryStage.showAndWait();
            encounter = encounterDao.getCurrentEncounter();
            refreshScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    String calculateDifficulty(int partySize, int averageLevel, long totalXp)
    {
        int [] levelDifficulty = xpThreasholds[averageLevel - 1];
        if (totalXp < levelDifficulty[1] * partySize) {
            return "Easy";
        } else if (totalXp < levelDifficulty[2] * partySize) {
            return "Medium";
        } else if (totalXp < levelDifficulty[3] * partySize) {
            return "Hard";
        } else {
            return "Deadly";
        }
    }

    public void closeApp() {
        Stage stage = (Stage)tableMonsters.getScene().getWindow();
        stage.close();
    }

    public void addMonster() {
        if (sessionDao.getOpenSession() == null) {
            Session session = new Session();
            session.setStartDate(new Date());
            sessionDao.addOrUpdate(session);
            updateSessionInfo();
        }

        try {
            Stage stage = AppHelper.loadFxmlDialog(
                    new Stage(),
                    getClass().getResource("/CreateMonsterDialog.fxml"),
                    "Create Monster",
                    369,
                    335,
                    true);
            stage.showAndWait();
            encounter = encounterDao.getCurrentEncounter();
            refreshScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showCloseSessionDialog(String title, boolean closeSession) {
        Stage primaryStage = new Stage();
        primaryStage.setTitle(title);
        primaryStage.setWidth(538);
        primaryStage.setHeight(379);

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/CloseSessionDialog.fxml"));
        try {
            Parent root = loader.load();
            CloseSessionDialogController controller = loader.getController();
            controller.initialize(closeSession);

            primaryStage.initModality(Modality.APPLICATION_MODAL);
            primaryStage.setScene(new Scene(root));
            primaryStage.showAndWait();
            encounter = encounterDao.getCurrentEncounter();
            refreshScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateSessionInfo();
    }

    public void newSession() {
        if (sessionDao.getOpenSession() != null) {
            showCloseSessionDialog("Close Session", true);
        } else {
            Session session = new Session();
            session.setStartDate(new Date());
            sessionDao.addOrUpdate(session);
            updateSessionInfo();
        }
    }

    public void xpForSession() {
        showCloseSessionDialog("XP for Session", false);
    }

    public void copyEncounter() {
/*        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Copy Encounter");
        dialog.setHeaderText("Enter a name for the encounter");
        dialog.setContentText("Name");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            try {
                String name = result.get();
                if (encounterDao.exists(name)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Name");
                    alert.setHeaderText("Another encounter already has that name");
                    alert.setContentText("Pick another name");

                    alert.showAndWait();
                    return;
                }
                int amt = Integer.parseInt(result.get().trim());
                encounterDao.addMonstersToCurrentEncounter(monster, amt);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Number");
                alert.setHeaderText("Failed to parse number");
                alert.setContentText("'" + result.get() + "' is not a valid number");

                alert.showAndWait();
                return;
            }
            encounter = encounterDao.getCurrentEncounter();
            refreshScreen();
        }*/
    }

    public void editParty() {
        try {
            Stage stage = AppHelper.loadFxmlDialog(
                    new Stage(),
                    getClass().getResource("/PartyEditDialog.fxml"),
                    "Edit Party",
                    191,
                    230,
                    true);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}