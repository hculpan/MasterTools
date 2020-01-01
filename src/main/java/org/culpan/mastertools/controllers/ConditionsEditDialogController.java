package org.culpan.mastertools.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.culpan.mastertools.dao.MonsterDao;
import org.culpan.mastertools.model.Monster;
import org.culpan.mastertools.model.MonsterCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConditionsEditDialogController {
    private final static String conditionText [] = {
            "Blinded",
            "Charmed",
            "Deafened",
            "Fatigued",
            "Frightened",
            "Grappled",
            "Incapacitated",
            "Invisible",
            "Paralyzed",
            "Petrified",
            "Poisoned",
            "Prone",
            "Restrained",
            "Stunned",
            "Unconscious"
    };

    private final static MonsterDao monsterDao = new MonsterDao();

    private Set<String> conditionTexts;

    private List<Monster> monsters;

    @FXML
    Pane dialogWindow;

    @FXML
    Button okButton;

    @FXML
    Button cancelButton;

    private List<MonsterCondition> getSelectedConditions() {
        List<MonsterCondition> result = new ArrayList<>();

        for (Node n : dialogWindow.getChildren()) {
            if (n instanceof CheckBox && ((CheckBox)n).isSelected()) {
                MonsterCondition mc = new MonsterCondition();
                mc.setCondition(((CheckBox)n).getText());
                result.add(mc);
            }
        }

        return result;
    }

    public void okClicked() {
        List<MonsterCondition> newConditions = getSelectedConditions();

        if (newConditions.size() > 0) {
            for (Monster monster : monsters) {
                monster.getConditions().clear();
                monster.getConditions().addAll(newConditions);
                monsterDao.addOrUpdateConditions(monster);
            }
        }

        Stage stage = (Stage)dialogWindow.getScene().getWindow();
        stage.close();
    }

    public void cancelClicked() {
        Stage stage = (Stage)dialogWindow.getScene().getWindow();
        stage.close();
    }

    public void initialize(Set<String> conditionTexts, List<Monster> monsters) {
        this.conditionTexts = conditionTexts;
        this.monsters = monsters;

        int currY = 20;
        if (conditionTexts == null) {
            for (String condition : conditionText) {
                CheckBox checkBox = new CheckBox(condition);
                checkBox.setLayoutX(40);
                checkBox.setLayoutY(currY);
                dialogWindow.getChildren().add(checkBox);
                currY += 25;
            }
            okButton.setLayoutY(currY + 10);
            cancelButton.setLayoutY(currY + 10);
            dialogWindow.setPrefHeight(currY + 50);
        } else {
            for (String condition : conditionText) {
                CheckBox checkBox = new CheckBox(condition);
                checkBox.setLayoutX(40);
                checkBox.setLayoutY(currY);
                checkBox.setSelected(conditionTexts.contains(condition));
                dialogWindow.getChildren().add(checkBox);
                currY += 25;
            }
            okButton.setLayoutY(currY + 10);
            cancelButton.setLayoutY(currY + 10);
            dialogWindow.setPrefHeight(currY + 50);
        }
    }
}
