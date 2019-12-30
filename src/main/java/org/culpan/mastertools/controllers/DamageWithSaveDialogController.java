package org.culpan.mastertools.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.culpan.mastertools.dao.MonsterDao;
import org.culpan.mastertools.model.Monster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DamageWithSaveDialogController {
    private final static MonsterDao monsterDao = new MonsterDao();

    enum DamageSave { dsNone, dsHalf, dsFull };

    private Map<String, DamageSave> saves = new HashMap<>();

    @FXML
    TableView<Monster> tableMonsters;

    @FXML
    TextField textDamage;

    public void initialize(List<Monster> monsterList) {
        tableMonsters.getItems().addAll(monsterList);
        tableMonsters.refresh();

        for (Monster m : monsterList) {
            saves.put(m.getIdentifier(), DamageSave.dsNone);
        }

        TableColumn<Monster, CheckBox> select = new TableColumn("Half Dmg");
        select.setPrefWidth(60);
        select.setStyle("-fx-alignment: CENTER;");
        select.setCellValueFactory((Callback<TableColumn.CellDataFeatures<Monster, CheckBox>, ObservableValue<CheckBox>>) arg0 -> {
            Monster m = arg0.getValue();

            CheckBox checkBox = new CheckBox();

            checkBox.selectedProperty().setValue(saves.get(m.getIdentifier()) == DamageSave.dsHalf);

            checkBox.selectedProperty().addListener((ov, old_val, new_val) -> {
                if (new_val) {
                    saves.put(m.getIdentifier(), DamageSave.dsHalf);
                } else {
                    saves.put(m.getIdentifier(), DamageSave.dsNone);
                }
            });

            return new SimpleObjectProperty<>(checkBox);

        });
        tableMonsters.getColumns().addAll( select);

        select = new TableColumn("No Dmg");
        select.setPrefWidth(60);
        select.setStyle("-fx-alignment: CENTER;");
        select.setCellValueFactory(arg0 -> {
            Monster m = arg0.getValue();

            CheckBox checkBox = new CheckBox();

            checkBox.selectedProperty().setValue(saves.get(m.getIdentifier()) == DamageSave.dsFull);

            checkBox.selectedProperty().addListener((ov, old_val, new_val) -> {
                if (new_val) {
                    saves.put(m.getIdentifier(), DamageSave.dsFull);
                } else {
                    saves.put(m.getIdentifier(), DamageSave.dsNone);
                }
            });

            return new SimpleObjectProperty<>(checkBox);

        });
        tableMonsters.getColumns().addAll( select);
    }

    public void ok() {
        int dmg = 0;
        try {
            if (textDamage.getText() == null || textDamage.getText().trim().isEmpty()) {
                throw new NumberFormatException("Empty text");
            }
            dmg = Integer.parseInt(textDamage.getText());
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Number");
            alert.setHeaderText("'" + textDamage.getText() + "' is not a valid number");
            alert.setContentText(e.getLocalizedMessage());

            alert.showAndWait();
        }

        for (Monster m : tableMonsters.getItems()) {
            String id = m.getIdentifier();
            if (saves.get(id) == DamageSave.dsHalf) {
                m.setHealth(m.getHealth() - (int)Math.floor(dmg / 2));
            } else if (saves.get(id) == DamageSave.dsNone) {
                m.setHealth(m.getHealth() - dmg);
            }
            if (m.getHealth() < 0) {
                m.setHealth(0);
            }
            monsterDao.addOrUpdate(m, false);
        }
        monsterDao.commit();

        Stage stage = (Stage)tableMonsters.getScene().getWindow();
        stage.close();
    }

    public void cancel() {
        Stage stage = (Stage)tableMonsters.getScene().getWindow();
        stage.close();
    }
}
