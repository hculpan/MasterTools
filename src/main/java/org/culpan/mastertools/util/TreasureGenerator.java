package org.culpan.mastertools.util;

import org.culpan.mastertools.dao.TreasureTableDao;
import org.culpan.mastertools.model.TreasureTable;

import java.util.List;
import java.util.Random;

public class TreasureGenerator {
    private final static TreasureTableDao tableDao = new TreasureTableDao();

    private final static Random rnd = new Random();

    public TreasureTable generateItem(String table) {
        int num = rnd.nextInt(100) + 1;
        System.out.println("Roll: " + num);
        return tableDao.findLastMatch(table, num);
    }

    public void generateForEachMonster(List<MonsterTreasure> monsters) {
        for (MonsterTreasure mt : monsters) {
            mt.reset();
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
                if (roll < 21) {
                    mt.setSilver(rollDice("4d6x100"));
                    mt.setGold(rollDice("1d6x100"));
                } else if (roll < 36) {
                    mt.setElectrum(rollDice("1d6x100"));
                    mt.setGold(rollDice("1d6x100"));
                } else if (roll < 76) {
                    mt.setGold(rollDice("2d6x100"));
                    mt.setPlatinum(rollDice("1d6x10"));
                } else  {
                    mt.setGold(rollDice("2d6x100"));
                    mt.setPlatinum(rollDice("2d6x10"));
                }
            } else { // cr 17+
                if (roll < 16) {
                    mt.setElectrum(rollDice("2d6x100"));
                    mt.setGold(rollDice("8d6x100"));
                } else if (roll < 56) {
                    mt.setGold(rollDice("1d6x1000"));
                    mt.setPlatinum(rollDice("1d6x10"));
                } else  {
                    mt.setGold(rollDice("1d6x1000"));
                    mt.setPlatinum(rollDice("2d6x100"));
                }
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

}
