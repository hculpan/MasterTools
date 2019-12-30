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

    private void damageMonsters(List<Monster> monsters) {
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

    private void healMonsters(List<Monster> monsters) {
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (encounter == null) {
            encounter = encounterDao.getCurrentEncounter();
        }

        if (party == null) {
            party = partyDao.getCurrentParty();
        }

        tableMonsters.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableMonsters.setPlaceholder(new Label(""));

        tableMonsters.getColumns().get(0).setCellFactory(getCustomCellFactory("num"));
        tableMonsters.getColumns().get(1).setCellFactory(getCustomCellFactory("name"));
        tableMonsters.getColumns().get(2).setCellFactory(getCustomCellFactory("baseHp"));
        tableMonsters.getColumns().get(3).setCellFactory(getCustomCellFactory("health"));
        tableMonsters.getColumns().get(4).setCellFactory(getCustomCellFactory("ac"));
        tableMonsters.getColumns().get(5).setCellFactory(getCustomCellFactory("attk"));
        tableMonsters.getColumns().get(6).setCellFactory(getCustomCellFactory("dmg"));
        tableMonsters.getColumns().get(7).setCellFactory(getCustomCellFactory("dc"));
        tableMonsters.getColumns().get(8).setCellFactory(getCustomCellFactory("cr"));
        tableMonsters.getColumns().get(9).setCellFactory(getCustomCellFactory("xp"));

        MenuItem damage = new MenuItem("Damage");
        damage.setOnAction(e -> {
            damageMonsters(tableMonsters.getSelectionModel().getSelectedItems());
        });

        MenuItem damageWithSaves = new MenuItem("Damage With Saves");
        damageWithSaves.setOnAction(e -> {
            damageMonstersWithSaves(tableMonsters.getSelectionModel().getSelectedItems());
        });

        MenuItem heal = new MenuItem("Heal");
        heal.setOnAction(e -> {
            healMonsters(tableMonsters.getSelectionModel().getSelectedItems());
        });

        MenuItem addClones = new MenuItem("Add more...");
        addClones.setOnAction(e -> {
            addMoreMonster(tableMonsters.getSelectionModel().getSelectedItems());
        });

        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(
                damage, damageWithSaves, new SeparatorMenuItem(),
                heal, new SeparatorMenuItem(),
                addClones);
        tableMonsters.setContextMenu(contextMenu);

        Callback<TableColumn<Monster, Void>, TableCell<Monster, Void>> cellFactoryHurt = new Callback<TableColumn<Monster, Void>, TableCell<Monster, Void>>() {
            @Override
            public TableCell<Monster, Void> call(final TableColumn<Monster, Void> param) {
                final TableCell<Monster, Void> cell = new TableCell<Monster, Void>() {

                    private final Button btn = new Button("-");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            List<Monster> monsters = new ArrayList<>();
                            monsters.add(getTableView().getItems().get(getIndex()));
                            damageMonsters(monsters);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        };

        TableColumn<Monster, Void> hurtColumn = new TableColumn<>("Hurt");
        hurtColumn.setCellFactory(cellFactoryHurt);
        hurtColumn.setStyle("-fx-alignment: CENTER;");
        hurtColumn.setSortable(false);
        hurtColumn.setEditable(false);
        hurtColumn.setPrefWidth(40);

        Callback<TableColumn<Monster, Void>, TableCell<Monster, Void>> cellFactoryHeal = new Callback<TableColumn<Monster, Void>, TableCell<Monster, Void>>() {
            @Override
            public TableCell<Monster, Void> call(final TableColumn<Monster, Void> param) {
                final TableCell<Monster, Void> cell = new TableCell<Monster, Void>() {

                    private final Button btn = new Button("+");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            List<Monster> monsters = new ArrayList<>();
                            monsters.add(getTableView().getItems().get(getIndex()));
                            healMonsters(monsters);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        };

        TableColumn<Monster, Void> healColumn = new TableColumn<>("Heal");
        healColumn.setCellFactory(cellFactoryHeal);
        healColumn.setStyle("-fx-alignment: CENTER;");
        healColumn.setSortable(false);
        healColumn.setEditable(false);
        healColumn.setPrefWidth(40);

        tableMonsters.getColumns().add(hurtColumn);
        tableMonsters.getColumns().add(healColumn);

        TableColumn select = new TableColumn("Add XP");
        select.setPrefWidth(50);
        select.setStyle("-fx-alignment: CENTER;");
        select.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Monster, CheckBox>, ObservableValue<CheckBox>>() {

            @Override
            public ObservableValue<CheckBox> call(
                    TableColumn.CellDataFeatures<Monster, CheckBox> arg0) {
                Monster m = arg0.getValue();

                CheckBox checkBox = new CheckBox();

                checkBox.selectedProperty().setValue(m.isActive());

                checkBox.selectedProperty().addListener((ov, old_val, new_val) -> {
                    m.setActive(new_val);
                    if (new_val) {
                        m.setSummoned(false);
                    }
                    monsterDao.addOrUpdate(m);
                    refreshScreen();
                });

                return new SimpleObjectProperty<CheckBox>(checkBox);

            }

        });
        tableMonsters.getColumns().addAll( select);

        select = new TableColumn("Summ.");
        select.setPrefWidth(50);
        select.setStyle("-fx-alignment: CENTER;");
        select.setCellValueFactory((Callback<TableColumn.CellDataFeatures<Monster, CheckBox>, ObservableValue<CheckBox>>) arg0 -> {
            Monster m = arg0.getValue();

            CheckBox checkBox = new CheckBox();

            checkBox.selectedProperty().setValue(m.isSummoned());

            checkBox.selectedProperty().addListener((ov, old_val, new_val) -> {
                m.setSummoned(new_val);
                if (new_val) {
                    m.setActive(false);
                }
                monsterDao.addOrUpdate(m);
                refreshScreen();
            });

            return new SimpleObjectProperty<>(checkBox);

        });
        tableMonsters.getColumns().addAll( select);

        refreshScreen();
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

    private String addSignToNumber(Object o) {
        int num = (Integer)o;
        if (num > 0) {
            return String.format("+%d", num);
        } else {
            return Integer.toString(num);
        }
    }

    private boolean fieldIsOneOf(String field, String ... fields) {
        for (String fieldToCheck : fields) {
            if (field.equalsIgnoreCase(fieldToCheck)) {
                return true;
            }
        }

        return false;
    }

    private <Monster, Object> Callback<TableColumn<Monster, Object>, TableCell<Monster, Object>> getCustomCellFactory(final String field) {
        return new Callback<>() {
            @Override
            public TableCell<Monster, Object> call(TableColumn<Monster, Object> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);

                        if (!empty) {
                            if (fieldIsOneOf(field, "attk", "dc")) {
                                setText(addSignToNumber(item));
                            } else if (fieldIsOneOf(field, "xp")) {
                                setText(NumberFormat.getNumberInstance(Locale.US).format(item));
                            } else {
                                setText(item.toString());
                            }

                            if (fieldIsOneOf(field, "num", "baseHp", "health", "ac", "attk", "dmg", "dc", "cr", "xp")) {
                                setStyle("-fx-alignment: CENTER;");
                            } else {
                                setStyle("-fx-alignment: CENTER-LEFT;");
                            }

                            org.culpan.mastertools.model.Monster data = (org.culpan.mastertools.model.Monster) getTableView().getItems().get(getIndex());
                            if (!data.isActive() && !data.isSummoned()) {
                                setTextFill(Color.LIGHTGRAY);
                            } else if (data.getHealth() <= 0) {
                                setTextFill(Color.LIGHTSALMON);
                            } else if (data.isSummoned()) {
                                setTextFill(Color.GREEN);
                            } else {
                                setTextFill(Color.BLACK);
                            }
                        }
                    }

                };
            }
        };
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