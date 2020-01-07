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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenerateTreasureDialogController {
    interface CheckBoxColumn {
        void action(Boolean newValue, MonsterTreasure m);
    }

    public class MonsterTreasure {
        private String identifier;
        private boolean addTreasure = true;
        private int cr;

        private int copper;
        private int silver;
        private int electrum;
        private int gold;
        private int platinum;

        public MonsterTreasure(String identifier, String cr) {
            this.identifier = identifier;
            if (cr.startsWith("1/") || cr.startsWith("0.")) {
                this.cr = 0;
            } else {
                this.cr = Integer.parseInt(cr);
            }
        }

        public String getIdentifier() {
            return identifier;
        }

        public int getCr() {
            return cr;
        }

        public boolean isAddTreasure() {
            return addTreasure;
        }

        public void setAddTreasure(boolean addTreasure) {
            this.addTreasure = addTreasure;
        }

        public int getCopper() {
            return copper;
        }

        public void setCopper(int copper) {
            this.copper = copper;
        }

        public int getSilver() {
            return silver;
        }

        public void setSilver(int silver) {
            this.silver = silver;
        }

        public int getElectrum() {
            return electrum;
        }

        public void setElectrum(int electrum) {
            this.electrum = electrum;
        }

        public int getGold() {
            return gold;
        }

        public void setGold(int gold) {
            this.gold = gold;
        }

        public int getPlatinum() {
            return platinum;
        }

        public void setPlatinum(int platinum) {
            this.platinum = platinum;
        }
    }

    private final static Random rnd = new Random();

    private final static PartyDao partyDao = new PartyDao();

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
        generateForEachMonster();
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

    private void generateForEachMonster() {
        for (MonsterTreasure mt : monsters) {
            int roll = rnd.nextInt(100) + 1;
            if (mt.getCr() < 5) {
                if (roll < 31) mt.setCopper(rollDice("5d6"));
                else if (roll < 61) mt.setSilver(rollDice("4d6"));
                else if (roll < 71) mt.setElectrum(rollDice("3d6"));
                else if (roll < 96) mt.setGold(rollDice("3d6"));
                else  mt.setPlatinum(rollDice("1d6"));
            } else if (mt.getCr() < 11) {
                if (roll < 31) {
                    mt.setCopper(rollDice("4d6x100"));
                    mt.setElectrum(rollDice("1d6x10"));
                } else if (roll < 61) {
                    mt.setSilver(rollDice("6d6x10"));
                    mt.setGold(rollDice("2d6x10"));
                } else if (roll < 71) {
                    mt.setElectrum(rollDice("3d6x10"));
                    mt.setGold(rollDice("2d6x10"));
                } else if (roll < 96) {
                    mt.setGold(rollDice("4d6x10"));
                } else  {
                    mt.setGold(rollDice("2d6x10"));
                    mt.setPlatinum(rollDice("3d6"));
                }
            } else if (mt.getCr() < 17) {

            } else { // cr 17+

            }
        }
    }

    protected int rollDice(String s) {
        int dLoc = s.indexOf("d");
        int plusLoc = s.indexOf("+");
        int multLoc = s.indexOf("x");

        int numDice = Integer.parseInt(s.substring(0, dLoc));
        int diceSize;
        if (plusLoc > -1 || multLoc > -1) {
            diceSize = Integer.parseInt(s.substring(dLoc + 1, (multLoc > -1 ? multLoc : plusLoc)));
        } else {
            diceSize = Integer.parseInt(s.substring(dLoc + 1));
        }

        int total = 0;
        for (int i = 0; i < numDice; i++) {
            total += rnd.nextInt(diceSize) + 1;
        }

        if (multLoc > -1) {
            total *= Integer.parseInt(s.substring(multLoc + 1));
        }

        if (plusLoc >- -1) {
            total += Integer.parseInt(s.substring(plusLoc + 1));
        }

        return total;
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
