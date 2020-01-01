package org.culpan.mastertools.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.culpan.mastertools.dao.EncounterDao;
import org.culpan.mastertools.model.Monster;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateMonsterDialogController implements Initializable {
    private final static EncounterDao encounterDao = new EncounterDao();

    private final static int xpPerCr[] = {
            25, 50, 100, 200, 450, 700, 1100, 1800,
            2300, 2900, 3900, 5000, 5900,
            7200, 8400, 10000, 11500, 13000,
            15000, 18000, 20000, 22000, 25000,
            33000, 41000, 50000, 62000, 75000,
            90000, 105000, 120000, 135000, 155000
    };

    private final static String hpPerCr[] = {
            "7-35", "36-49", "50-70",
            "71-85", "86-100", "101-115", "116-130", "131-145",
            "146-160", "161-175", "176-190", "191-205", "206-220",
            "221-235", "236-250", "251-265", "266-280", "281-295",
            "296-310", "311-325","326-340","341-355","356-400",
            "401-445","446-490","491-535","536-580","581-625",
            "626-670","671-715","716-760","761-805","805-850"
    };

    private final static int acPerCr[] = {
            13, 13, 13,
            13, 13, 13, 14, 15,
            15, 15, 16, 16, 17,
            17, 17, 18, 18, 18,
            18, 19, 19, 19, 19,
            19, 19, 19, 19, 19,
            19, 19, 19, 19, 19
    };

    private final static int abPerCr[] = {
            3, 3, 3,
            3, 3, 4, 5, 6,
            6, 6, 7, 7, 7,
            8, 8, 8, 8, 8,
            9, 10, 10, 10, 10,
            11, 11, 11, 12, 12,
            12, 13, 13, 13, 14
    };

    private final static String dmgPerCr[] = {
            "2-3","4-5","6-8",
            "9-14","15-20","21-26","27-32","33-38",
            "39-44","45-50","51-56","57-62","63-58",
            "69-74","75-80","81-86","87-92","93-98",
            "99-104","105-110","111-116","117-122","123-140",
            "141-158","159-176","177-194","195-212","213-230",
            "231-248","249-266","267-284","285-302","303-320"
    };

    private final static int savePerCr[] = {
            13,13,13,
            13,13,13,14,15,
            15,15,16,16,16,
            17,17,18,18,18,
            18,19,19,19,19,
            20,20,20,21,21,
            21,22,22,22,23
    };

    @FXML
    ComboBox<String> comboCr;

    @FXML
    TextField textXp;

    @FXML
    Spinner<Integer> spinnerNumber;

    @FXML
    TextField textName;

    @FXML
    TextField textHp;

    @FXML
    TextField textAc;

    @FXML
    TextField textAttk;

    @FXML
    TextField textDmg;

    @FXML
    TextField textDc;

    @FXML
    Label labelHp;

    @FXML
    Label labelAc;

    @FXML
    Label labelAttk;

    @FXML
    Label labelDmg;

    @FXML
    Label labelDc;

    @FXML
    CheckBox checkSummoned;

    public void okClicked() {
        if (textName.getText() == null || textName.getText().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Name");
            alert.setHeaderText("Name cannot be empty");
            alert.setContentText("Please specify a name for this monster");

            alert.showAndWait();
            return;
        }

        Monster m = new Monster();
        m.setName(textName.getText());
        m.setBaseHp(Integer.parseInt(textHp.getText()));
        m.setHealth(m.getBaseHp());
        m.setAc(Integer.parseInt(textAc.getText()));
        m.setAttk(Integer.parseInt(textAttk.getText()));
        m.setDmg(textDmg.getText());
        m.setDc(Integer.parseInt(textDc.getText()));
        m.setCr(comboCr.getValue());
        int totalXp = Integer.parseInt(textXp.getText().replace(",",""));
        m.setXp(totalXp/spinnerNumber.getValue());
        if (checkSummoned.isSelected()) {
            m.setActive(false);
            m.setSummoned(true);
        } else {
            m.setActive(true);
            m.setSummoned(false);
        }
        encounterDao.addMonstersToCurrentEncounter(m, spinnerNumber.getValue());

        Stage stage = (Stage)comboCr.getScene().getWindow();
        stage.close();
    }

    public void cancelClicked() {
        Stage stage = (Stage)comboCr.getScene().getWindow();
        stage.close();
    }

    private void updateUi() {
        int crIndex = comboCr.getSelectionModel().getSelectedIndex();
        textHp.setText(calculateAverageValue(hpPerCr[crIndex]));
        labelHp.setText(hpPerCr[crIndex]);
        textAc.setText(Integer.toString(acPerCr[crIndex]));
        labelAc.setText(Integer.toString(acPerCr[crIndex]));
        textAttk.setText(formatWithSign(abPerCr[crIndex]));
        labelAttk.setText(formatWithSign(abPerCr[crIndex]));
        textDmg.setText(calculateAverageValue(dmgPerCr[crIndex]));
        labelDmg.setText(dmgPerCr[crIndex]);
        textDc.setText(formatWithSign(savePerCr[crIndex]));
        labelDc.setText(formatWithSign(savePerCr[crIndex]));

        textXp.setText(NumberFormat.getNumberInstance(Locale.US).format(spinnerNumber.getValue() * xpPerCr[crIndex]));
    }

    private String formatWithSign(int i) {
        return (i > 0 ? String.format("+%s", i) : Integer.toString(i));
    }

    public void crChanged(ActionEvent event) {
        updateUi();
    }

    private String calculateAverageValue(String s) {
        int index = s.indexOf("-");
        int low = Integer.parseInt(s.substring(0, index));
        int high = Integer.parseInt(s.substring(index + 1));
        return Integer.toString((low + high)/2);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboCr.getItems().add("1/8");
        comboCr.getItems().add("1/4");
        comboCr.getItems().add("1/2");

        for (int i = 1; i < 31; i++) {
            comboCr.getItems().add(Integer.toString(i));
        }
        comboCr.getSelectionModel().select(0);

        textXp.setText("0");

        SpinnerValueFactory<Integer> valueFactory = //
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        spinnerNumber.setValueFactory(valueFactory);
        spinnerNumber.valueProperty().addListener((observable, oldValue, newValue) -> updateUi());

        updateUi();
    }
}
