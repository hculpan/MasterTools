package org.culpan.mastertools.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import org.culpan.mastertools.dao.PartyDao;
import org.culpan.mastertools.model.Party;

import java.net.URL;
import java.util.ResourceBundle;

public class PartyEditDialogController implements Initializable {
    private final static PartyDao partyDao = new PartyDao();

    private Party party;

    @FXML
    Spinner<Integer> spinnerCount;

    @FXML
    Spinner<Integer> spinnerAverageLevel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        party = partyDao.getCurrentParty();

        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        spinnerCount.setValueFactory(valueFactory);
        valueFactory.setValue(party.getMemberCount());

        valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        spinnerAverageLevel.setValueFactory(valueFactory);
        valueFactory.setValue(party.getAverageLevel());
    }

    public void save() {
        party.setMemberCount(spinnerCount.getValue());
        party.setAverageLevel(spinnerAverageLevel.getValue());
        partyDao.addOrUpdate(party);

        Stage stage = (Stage)spinnerCount.getScene().getWindow();
        stage.close();
    }

    public void cancel() {
        Stage stage = (Stage)spinnerCount.getScene().getWindow();
        stage.close();
    }
}
