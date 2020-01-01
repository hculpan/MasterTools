package org.culpan.mastertools.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.culpan.mastertools.dao.MonsterDao;
import org.culpan.mastertools.model.Monster;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MainDialogInitialize {
    interface CheckBoxColumn {
        void action(Boolean newValue, Monster m);
    }

    interface UpdateCellItem {
        void action(Labeled node, Monster m, Object value);
    }

    private final static MonsterDao monsterDao = new MonsterDao();

    private MainDialogController controller;

    public MainDialogInitialize(MainDialogController controller) {
        this.controller = controller;
    }

    private Color getColor(Monster monster) {
        if (!monster.isActive() && !monster.isSummoned()) {
            return Color.LIGHTGRAY;
        } else if (monster.getHealth() <= 0) {
            return Color.LIGHTSALMON;
        } else if (monster.isSummoned()) {
            return Color.GREEN;
        } else {
            return Color.BLACK;
        }
    }

    private void updateItem(Labeled node, Monster monster, String text, boolean centered) {
        node.setStyle("-fx-alignment: " + (centered ? "CENTER;" : "CENTER-LEFT;"));
        node.setTextFill(getColor(monster));
        node.setText(text);
    }

    public void initialize() {
        controller.tableMonsters.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        controller.tableMonsters.setPlaceholder(new Label(""));

        controller.tableMonsters.getColumns().get(0).setCellFactory(getTextCellFactory(
                (node, monster, item) -> updateItem(node, monster, item.toString(), true)));
        controller.tableMonsters.getColumns().get(1).setCellFactory(getTextCellFactory(
                (node, monster, item) -> updateItem(node, monster, item.toString(), false)));
        controller.tableMonsters.getColumns().get(2).setCellFactory(getTextCellFactory(
                (node, monster, item) -> updateItem(node, monster, item.toString(), true)));
        controller.tableMonsters.getColumns().get(3).setCellFactory(getTextCellFactory(
                (node, monster, item) -> updateItem(node, monster, item.toString(), true)));
        controller.tableMonsters.getColumns().get(4).setCellFactory(getTextCellFactory(
                (node, monster, item) -> updateItem(node, monster, item.toString(), true)));
        controller.tableMonsters.getColumns().get(5).setCellFactory(getTextCellFactory(
                (node, monster, item) -> updateItem(node, monster, addSignToNumber(item), true)));
        controller.tableMonsters.getColumns().get(6).setCellFactory(getTextCellFactory(
                (node, monster, item) -> updateItem(node, monster, item.toString(), true)));
        controller.tableMonsters.getColumns().get(7).setCellFactory(getTextCellFactory(
                (node, monster, item) -> updateItem(node, monster, item.toString(), true)));
        controller.tableMonsters.getColumns().get(8).setCellFactory(getTextCellFactory(
                (node, monster, item) -> updateItem(node, monster, item.toString(), true)));
        controller.tableMonsters.getColumns().get(9).setCellFactory(getTextCellFactory(
                (node, monster, item) -> updateItem(node, monster, item.toString(), true)));

        // Pull popup menu from Combatant top-level menu
        final ContextMenu contextMenu = new ContextMenu();
        for (MenuItem menuItem : controller.menuCombatant.getItems()) {
            MenuItem newItem;
            if (menuItem instanceof SeparatorMenuItem) {
                newItem = new SeparatorMenuItem();
            } else {
                newItem = new MenuItem();
                newItem.setText(menuItem.getText());
                newItem.setOnAction(menuItem.getOnAction());
            }
            contextMenu.getItems().add(newItem);
        }
        controller.tableMonsters.setContextMenu(contextMenu);

        TableColumn<Monster, Void> hurtColumn = new TableColumn<>("Hurt");
        hurtColumn.setCellFactory(getButtonCellFactory("-"));
        hurtColumn.setStyle("-fx-alignment: CENTER;");
        hurtColumn.setSortable(false);
        hurtColumn.setEditable(false);
        hurtColumn.setPrefWidth(40);

        TableColumn<Monster, Void> healColumn = new TableColumn<>("Heal");
        healColumn.setCellFactory(getButtonCellFactory("+"));
        healColumn.setStyle("-fx-alignment: CENTER;");
        healColumn.setSortable(false);
        healColumn.setEditable(false);
        healColumn.setPrefWidth(40);

        controller.tableMonsters.getColumns().add(hurtColumn);
        controller.tableMonsters.getColumns().add(healColumn);

        TableColumn<Monster, CheckBox> checkBoxColumn = new TableColumn<>("Add XP");
        checkBoxColumn.setPrefWidth(50);
        checkBoxColumn.setStyle("-fx-alignment: CENTER;");
        checkBoxColumn.setCellValueFactory(getCheckboxCellFactory(
                Monster::isActive,
                (new_val, m) -> {
                    m.setActive(new_val);
                    if (new_val) {
                        m.setSummoned(false);
                    }
                    monsterDao.addOrUpdate(m);
                    controller.refreshScreen();
                }));
        controller.tableMonsters.getColumns().add(checkBoxColumn);

        checkBoxColumn = new TableColumn<>("Summ.");
        checkBoxColumn.setPrefWidth(50);
        checkBoxColumn.setStyle("-fx-alignment: CENTER;");
        checkBoxColumn.setCellValueFactory(getCheckboxCellFactory(
                Monster::isSummoned,
                (new_val, m) -> {
                    m.setSummoned(new_val);
                    if (new_val) {
                        m.setActive(false);
                    }
                    monsterDao.addOrUpdate(m);
                    controller.refreshScreen();
                }));
        controller.tableMonsters.getColumns().add(checkBoxColumn);

        TableColumn<Monster, Object> conditionsColumn = new TableColumn<>("Conditions");
        conditionsColumn.setPrefWidth(100);
        conditionsColumn.setCellFactory(getTextCellFactory(
                (node, monster, item) -> updateItem(node, monster, monster.getConditionAbrText(), false)));
        controller.tableMonsters.getColumns().add(conditionsColumn);

        controller.refreshScreen();
    }

    private Callback<TableColumn.CellDataFeatures<Monster, CheckBox>, ObservableValue<CheckBox>> getCheckboxCellFactory(
            Function<Monster, Boolean> checked,
            CheckBoxColumn action) {
        return arg0 -> {
            Monster m = arg0.getValue();

            CheckBox checkBox = new CheckBox();

            checkBox.selectedProperty().setValue(checked.apply(m));

            checkBox.selectedProperty().addListener((ov, old_val, new_val) -> action.action(new_val, m));

            return new SimpleObjectProperty<>(checkBox);
        };
    }

    private Callback<TableColumn<Monster, Void>, TableCell<Monster, Void>> getButtonCellFactory(
            final String cellText) {
        return new Callback<>() {
            @Override
            public TableCell<Monster, Void> call(final TableColumn<Monster, Void> param) {
                return new TableCell<>() {

                    private final Button btn = new Button(cellText);

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            List<Monster> monsters = new ArrayList<>();
                            monsters.add(getTableView().getItems().get(getIndex()));
                            if (cellText.equalsIgnoreCase("+")) {
                                controller.healMonsters(monsters);
                            } else {
                                controller.damageMonsters(monsters);
                            }
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
            }
        };
    }

    private String addSignToNumber(Object o) {
        int num = (Integer)o;
        if (num > 0) {
            return String.format("+%d", num);
        } else {
            return Integer.toString(num);
        }
    }

    @SuppressWarnings("all")
    private <Monster, Object> Callback<TableColumn<Monster, Object>, TableCell<Monster, Object>> getTextCellFactory(
            UpdateCellItem updateItemFunc) {
        return new Callback<>() {
            @Override
            public TableCell<Monster, Object> call(TableColumn<Monster, Object> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);

                        if (!empty) {
                            org.culpan.mastertools.model.Monster data = (org.culpan.mastertools.model.Monster) getTableView().getItems().get(getIndex());
                            updateItemFunc.action(this, data, item);
                        }
                    }
                };
            }
        };
    }
}
