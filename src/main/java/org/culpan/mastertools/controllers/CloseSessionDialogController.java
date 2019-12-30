package org.culpan.mastertools.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.culpan.mastertools.dao.PartyDao;
import org.culpan.mastertools.dao.SessionDao;
import org.culpan.mastertools.model.Party;
import org.culpan.mastertools.model.Session;
import org.culpan.mastertools.model.SessionXp;

import java.net.URL;
import java.text.NumberFormat;
import java.util.*;

public class CloseSessionDialogController {
    private final static SessionDao sessionDao = new SessionDao();
    private final static PartyDao partyDao = new PartyDao();

    @FXML
    TableView<SessionXp> tableSession;

    @FXML
    TextField textTotalXp;

    @FXML
    Spinner<Integer> spinnerNumPlayers;

    @FXML
    TextField textXpPerPlayer;

    @FXML
    Button closeButton;

    @FXML
    Button closeSessionButton;

    @FXML
    Button cancelButton;


    private Session session;
    private Party party;
    private Map<Integer, Boolean> includeXp = new HashMap<>();

    public void closeSession() {
        Session session = sessionDao.getOpenSession();
        if (session != null) {
            session.setEndDate(new Date());
            sessionDao.addOrUpdate(session);
        }

        session = new Session();
        session.setStartDate(new Date());
        sessionDao.addOrUpdate(session);

        cancel();
    }

    public void cancel() {
        Stage stage = (Stage)tableSession.getScene().getWindow();
        stage.close();
    }

    private int calculateSessionTotal() {
        return session.getSessionXpList().stream().mapToInt(s -> {
            if (includeXp.get(s.getId())) {
                return s.getXp();
            } else {
                return 0;
            }
        }).sum();
    }

    private void updateUi() {
        tableSession.getItems().clear();
        tableSession.getItems().addAll(session.getSessionXpList());
        tableSession.refresh();
        int totalXp = calculateSessionTotal();
        int numPlayers = spinnerNumPlayers.getValue();
        textTotalXp.setText(NumberFormat.getNumberInstance(Locale.US).format(totalXp));
        textXpPerPlayer.setText(NumberFormat.getNumberInstance(Locale.US).format(totalXp/numPlayers));
    }

    public void initialize(boolean closeSession) {
        session = sessionDao.getOpenSession();
        if (session == null) {
            session = new Session();
            session.setStartDate(new Date());
            sessionDao.addOrUpdate(session);
        }
        party = partyDao.getCurrentParty();

        if (closeSession) {
            closeButton.setVisible(false);
            closeSessionButton.setVisible(true);
            cancelButton.setVisible(true);
        } else {
            closeButton.setVisible(true);
            closeSessionButton.setVisible(false);
            cancelButton.setVisible(false);
        }

        for (SessionXp sessionXp : session.getSessionXpList()) {
            includeXp.put(sessionXp.getId(), true);
        }

        SpinnerValueFactory<Integer> valueFactory = //
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, party.getMemberCount());
        spinnerNumPlayers.setValueFactory(valueFactory);
        spinnerNumPlayers.valueProperty().addListener((observable, oldValue, newValue) -> updateUi());

        tableSession.setPlaceholder(new Label(""));

        tableSession.getColumns().get(1).setCellFactory(getCustomCellFactory());
        tableSession.getColumns().get(1).setStyle("-fx-alignment: CENTER; ");

        TableColumn<SessionXp, CheckBox> select = new TableColumn("Include");
        select.setPrefWidth(60);
        select.setStyle("-fx-alignment: CENTER;");
        select.setCellValueFactory( arg0 -> {
            SessionXp m = arg0.getValue();

            CheckBox checkBox = new CheckBox();

            checkBox.selectedProperty().setValue(includeXp.get(m.getId()));

            checkBox.selectedProperty().addListener((ov, old_val, new_val) -> {
                includeXp.put(m.getId(), new_val);
                updateUi();
            });

            return new SimpleObjectProperty<>(checkBox);

        });
        tableSession.getColumns().addAll( select);

        updateUi();
    }

    private <SessionXp, Object> Callback<TableColumn<SessionXp, Object>, TableCell<SessionXp, Object>> getCustomCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<SessionXp, Object> call(TableColumn<SessionXp, Object> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);

                        if (!empty) {
                            setText(NumberFormat.getNumberInstance(Locale.US).format(item));
                        }
                    }
                };
            }
        };
    }


}
