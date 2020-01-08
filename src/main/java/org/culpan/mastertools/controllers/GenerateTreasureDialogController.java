package org.culpan.mastertools.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.culpan.mastertools.dao.PartyDao;
import org.culpan.mastertools.model.Monster;
import org.culpan.mastertools.model.Party;
import org.culpan.mastertools.util.MonsterTreasure;
import org.culpan.mastertools.util.TreasureGenerator;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenerateTreasureDialogController {
    interface CheckBoxColumn {
        void action(Boolean newValue, MonsterTreasure m);
    }

    private final static PartyDao partyDao = new PartyDao();

    private final static TreasureGenerator treasureGenerator = new TreasureGenerator();

    @FXML
    TableView<MonsterTreasure> tableMonsters;

    @FXML
    WebView webTotal;

    @FXML
    Spinner<Integer> spinnerCount;

    ObservableList<MonsterTreasure> monsters;

    public void close() {
        Stage stage = (Stage)tableMonsters.getScene().getWindow();
        stage.close();
    }

    public void initialize(List<Monster> monstersList) {
        monsters = FXCollections.observableArrayList(monstersList.stream()
                .filter(m -> !m.isSummoned())
                .map(m -> new MonsterTreasure(m.getIdentifier(), m.getCr()))
                .collect(Collectors.toList()));
        tableMonsters.setItems(monsters);
        TableColumn<MonsterTreasure, CheckBox> checkBoxColumn = new TableColumn<>("Add");
        checkBoxColumn.setPrefWidth(35);
        checkBoxColumn.setStyle("-fx-alignment: CENTER;");
        checkBoxColumn.setCellValueFactory(getCheckboxCellFactory(
                MonsterTreasure::isAddTreasure,
                (new_val, m) -> {
                    m.setAddTreasure(new_val);
                    getTreasureHtml();
                }));
        tableMonsters.getColumns().add(checkBoxColumn);

        Party party = partyDao.getCurrentParty();

        SpinnerValueFactory<Integer> valueFactory = //
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, party.getMemberCount());
        spinnerCount.setValueFactory(valueFactory);
        spinnerCount.valueProperty().addListener((observable, oldValue, newValue) -> getTreasureHtml());

        generate();
    }

    public void generate() {
        treasureGenerator.generateForEachMonster(monsters);
        getTreasureHtml();
    }

    public void getTreasureHtml() {
        String result = "<style>" +
                ".cnt { text-align: center; border-bottom: 1px solid lightgray }" +
                "</style>" +
                "<h3>Treasure</h3><table>";

        result += "<tr>" +
                "<th style='width: 150px;'>Name</th>" +
                "<th class='cnt' style='width: 75px;'>Copper</th>" +
                "<th class='cnt' style='width: 75px;'>Silver</th>" +
                "<th class='cnt' style='width: 75px;'>Electrum</th>" +
                "<th class='cnt' style='width: 75px;'>Gold</th>" +
                "<th class='cnt' style='width: 75px;'>Platinum</th>" +
                "</tr>";

        int copper = 0, silver = 0, electrum = 0, gold = 0, platinum = 0;
        for (MonsterTreasure mt : monsters.stream().filter(MonsterTreasure::isAddTreasure).collect(Collectors.toList())) {
            result += String.format("<tr>" +
                    "<td style='text-align: left;'>%s</td>" +
                    "<td class='cnt'>%s</td>" +
                    "<td class='cnt'>%s</td>" +
                    "<td class='cnt'>%s</td>" +
                    "<td class='cnt'>%s</td>" +
                    "<td class='cnt'>%s</td></tr>",
                    mt.getIdentifier(),
                    NumberFormat.getNumberInstance(Locale.US).format(mt.getCopper()),
                    NumberFormat.getNumberInstance(Locale.US).format(mt.getSilver()),
                    NumberFormat.getNumberInstance(Locale.US).format(mt.getElectrum()),
                    NumberFormat.getNumberInstance(Locale.US).format(mt.getGold()),
                    NumberFormat.getNumberInstance(Locale.US).format(mt.getPlatinum()));

            copper += mt.getCopper();
            silver += mt.getSilver();
            electrum += mt.getElectrum();
            gold += mt.getGold();
            platinum += mt.getPlatinum();
        }

        result += String.format("<tr style='font-weight: bold'>" +
                        "<td style='text-align: right;'>%s</td>" +
                        "<td class='cnt'>%s</td>" +
                        "<td class='cnt'>%s</td>" +
                        "<td class='cnt'>%s</td>" +
                        "<td class='cnt'>%s</td>" +
                        "<td class='cnt'>%s</td></tr>",
                "Total",
                NumberFormat.getNumberInstance(Locale.US).format(copper),
                NumberFormat.getNumberInstance(Locale.US).format(silver),
                NumberFormat.getNumberInstance(Locale.US).format(electrum),
                NumberFormat.getNumberInstance(Locale.US).format(gold),
                NumberFormat.getNumberInstance(Locale.US).format(platinum));

        int cPer = copper/spinnerCount.getValue();
        int sPer = silver/spinnerCount.getValue();
        int ePer = electrum/spinnerCount.getValue();
        int gPer = gold/spinnerCount.getValue();
        int pPer = platinum/spinnerCount.getValue();
        result += String.format("<tr style='font-weight: bold'>" +
                        "<td style='text-align: right;'>%s</td>" +
                        "<td class='cnt'>%s</td>" +
                        "<td class='cnt'>%s</td>" +
                        "<td class='cnt'>%s</td>" +
                        "<td class='cnt'>%s</td>" +
                        "<td class='cnt'>%s</td></tr>",
                "Total per Player",
                NumberFormat.getNumberInstance(Locale.US).format(cPer),
                NumberFormat.getNumberInstance(Locale.US).format(sPer),
                NumberFormat.getNumberInstance(Locale.US).format(ePer),
                NumberFormat.getNumberInstance(Locale.US).format(gPer),
                NumberFormat.getNumberInstance(Locale.US).format(pPer));

        if (cPer * spinnerCount.getValue() != copper ||
            sPer * spinnerCount.getValue() != silver ||
                ePer * spinnerCount.getValue() != electrum ||
                gPer * spinnerCount.getValue() != gold ||
                pPer * spinnerCount.getValue() != platinum) {
            result += String.format("<tr style='font-weight: bold'>" +
                            "<td style='text-align: right;'>%s</td>" +
                            "<td class='cnt'>%s</td>" +
                            "<td class='cnt'>%s</td>" +
                            "<td class='cnt'>%s</td>" +
                            "<td class='cnt'>%s</td>" +
                            "<td class='cnt'>%s</td></tr>",
                    "Remaining",
                    NumberFormat.getNumberInstance(Locale.US).format(copper - (cPer * spinnerCount.getValue())),
                    NumberFormat.getNumberInstance(Locale.US).format(silver - (sPer * spinnerCount.getValue())),
                    NumberFormat.getNumberInstance(Locale.US).format(electrum - (ePer * spinnerCount.getValue())),
                    NumberFormat.getNumberInstance(Locale.US).format(gold - (gPer * spinnerCount.getValue())),
                    NumberFormat.getNumberInstance(Locale.US).format(platinum - (pPer * spinnerCount.getValue())));
        }

        result += "</table>";

        WebEngine engine = webTotal.getEngine();
        engine.loadContent(result);
    }

    private Callback<TableColumn.CellDataFeatures<MonsterTreasure, CheckBox>, ObservableValue<CheckBox>> getCheckboxCellFactory(
            Function<MonsterTreasure, Boolean> checked,
            GenerateTreasureDialogController.CheckBoxColumn action) {
        return arg0 -> {
            MonsterTreasure m = arg0.getValue();

            CheckBox checkBox = new CheckBox();

            checkBox.selectedProperty().setValue(checked.apply(m));

            checkBox.selectedProperty().addListener((ov, old_val, new_val) -> action.action(new_val, m));

            return new SimpleObjectProperty<>(checkBox);
        };
    }


}
