package org.culpan.mastertools.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.culpan.mastertools.dao.MonsterDao;
import org.culpan.mastertools.dao.PublishedMonsterDao;
import org.culpan.mastertools.model.Monster;
import org.culpan.mastertools.util.JsonParser;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class MainDialogInitialize {
    interface CheckBoxColumn {
        void action(Boolean newValue, Monster m);
    }

    interface UpdateCellItem {
        void action(Labeled node, Monster m, Object value);
    }

    private final static MonsterDao monsterDao = new MonsterDao();
    private final static PublishedMonsterDao publishedMonsterDao = new PublishedMonsterDao();

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

    private void popupAttackWindow(Monster m, double x, double y) {
        if (controller.getAttackPopupWindow() != null) return;
        if (m.getPublishedMonsterId() <= 0) return;

        AnchorPane pane = new AnchorPane();
        Scene scene = new Scene(pane);
        controller.setAttackPopupWindow(new Stage());
        controller.getAttackPopupWindow().setWidth(400);
        controller.getAttackPopupWindow().setHeight(400);
        controller.getAttackPopupWindow().setX(x + 30);
        controller.getAttackPopupWindow().setY(y - 200);
        controller.getAttackPopupWindow().setScene(scene);
        controller.getAttackPopupWindow().initStyle(StageStyle.UNDECORATED);
        controller.getAttackPopupWindow().focusedProperty().addListener((ov, onHidden, onShown) -> {
            if (onShown) {
                Stage stage = (Stage)controller.tableMonsters.getScene().getWindow();
                stage.setOnHiding(e -> {
                    if (controller.getAttackPopupWindow() != null) controller.getAttackPopupWindow().close();
                });
            } else if (onHidden) {
                if (controller.getAttackPopupWindow() != null) {
                    controller.getAttackPopupWindow().close();
                    controller.setAttackPopupWindow(null);
                }
            }
        });
        controller.getAttackPopupWindow().addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                controller.getAttackPopupWindow().close();
                controller.setAttackPopupWindow(null);
            }
        });
        String json = publishedMonsterDao.getJsonForId(m.getPublishedMonsterId());
        JsonParser parser = new JsonParser();
        JsonParser.JsonObject root = (JsonParser.JsonObject)parser.parse(json);
        JsonParser.JsonArray actions = (JsonParser.JsonArray)root.getProperty("actions");

        String xml = "";

        if (actions.size() > 0) {
            xml += String.format("<h3>Actions for %s</h3><hr/><br/>", m.getName());
            for (int i = 0; i < actions.size(); i++) {
                JsonParser.JsonObject action = (JsonParser.JsonObject) actions.get(i);
                xml += String.format("<em>%s</em><br/> %s <br/><br/>",
                        action.getPropertyValue("name"),
                        action.getPropertyValue("desc"));
            }
        }

        actions = (JsonParser.JsonArray)root.getProperty("special_abilities");
        if (actions.size() > 0) {
            xml += String.format("<hr/><h3>Special Abilities for %s</h3><hr/><br/>", m.getName());
            for (int i = 0; i < actions.size(); i++) {
                JsonParser.JsonObject action = (JsonParser.JsonObject) actions.get(i);
                xml += String.format("<em>%s</em><br/> %s <br/><br/>",
                        action.getPropertyValue("name"),
                        action.getPropertyValue("desc"));
            }
        }
        WebView webView = new WebView();
        webView.setPrefSize(400, 400);
        pane.getChildren().add(webView);
        webView.getEngine().loadContent(xml);
        controller.getAttackPopupWindow().show();
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
        TableColumn<Monster, Void> attkColumn = new TableColumn<>("Actions");
        attkColumn.setCellFactory(getButtonCellFactory("->",
                (TableCell<Monster, Void> t) -> event -> {
                    Monster m = t.getTableView().getItems().get(t.getIndex());
                    // This is kludge.  For some reason, the TableCell's localToScreen gives me a
                    // bad X but good Y.  So I grabbed X from TableView's localToScreen and then nudge a tad
                    double x, y;
                    Point2D screenCoordinates = t.localToScreen(t.getLayoutX(), t.getLayoutY());
                    Point2D windowCoordinates = controller.tableMonsters.localToScreen(t.getLayoutX(), t.getLayoutY());
                    x = windowCoordinates.getX() + 10;
                    y = screenCoordinates.getY();
                    if (y < 200) y = 200;
                    popupAttackWindow(m, x, y);
                }));
        attkColumn.setStyle("-fx-alignment: CENTER;");
        attkColumn.setSortable(false);
        attkColumn.setEditable(false);
        attkColumn.setPrefWidth(50);
        controller.tableMonsters.getColumns().set(5, attkColumn);
        controller.tableMonsters.getColumns().get(6).setCellFactory(getTextCellFactory(
                (node, monster, item) -> updateItem(node, monster, item.toString(), true)));
        controller.tableMonsters.getColumns().get(7).setCellFactory(getTextCellFactory(
                (node, monster, item) -> updateItem(node, monster, NumberFormat.getNumberInstance(Locale.US).format(item), true)));

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
        hurtColumn.setCellFactory(getButtonCellFactory("-",
                (TableCell<Monster, Void> t) -> event -> {
                    List<Monster> monsters = new ArrayList<>();
                    monsters.add(t.getTableView().getItems().get(t.getIndex()));
                    controller.damageMonsters(monsters);
                },
                m -> {
                    return m.getPublishedMonsterId() <= 0;
                }));
        hurtColumn.setStyle("-fx-alignment: CENTER;");
        hurtColumn.setSortable(false);
        hurtColumn.setEditable(false);
        hurtColumn.setPrefWidth(40);

        TableColumn<Monster, Void> healColumn = new TableColumn<>("Heal");
        healColumn.setCellFactory(getButtonCellFactory("+",
        (TableCell<Monster, Void> t) -> event -> {
            List<Monster> monsters = new ArrayList<>();
            monsters.add(t.getTableView().getItems().get(t.getIndex()));
            controller.healMonsters(monsters);
        }));
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
        conditionsColumn.setPrefWidth(125);
        conditionsColumn.setCellFactory(getTextCellFactory(
                (node, monster, item) -> updateItem(node, monster, monster.getConditionAbrText(), false)));
        controller.tableMonsters.getColumns().add(conditionsColumn);

        TableColumn<Monster, Object> notesColumn = new TableColumn<>("Notes");
        notesColumn.setPrefWidth(200);
        notesColumn.setCellFactory(getTextCellFactory(
                (node, monster, item) -> updateItem(node, monster, monster.getNotes(), false)));
        controller.tableMonsters.getColumns().add(notesColumn);

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
            final String cellText, Function<TableCell<Monster,Void>, EventHandler<ActionEvent>> action) {
        return getButtonCellFactory(cellText, action, m -> false );
    }

    private Callback<TableColumn<Monster, Void>, TableCell<Monster, Void>> getButtonCellFactory(
            final String cellText, Function<TableCell<Monster,Void>, EventHandler<ActionEvent>> action, Function<Monster,Boolean> disableButton) {
        return new Callback<>() {
            @Override
            public TableCell<Monster, Void> call(final TableColumn<Monster, Void> param) {
                return new TableCell<>() {

                    private final Button btn = new Button(cellText);

                    {
                        btn.setOnAction(action.apply(this));
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                            Monster m = getTableView().getItems().get(getIndex());
                            if (m != null) btn.setDisable(disableButton.apply(m));
                            else btn.setDisable(true);
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
