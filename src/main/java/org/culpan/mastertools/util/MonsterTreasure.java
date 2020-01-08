package org.culpan.mastertools.util;

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

    public void reset() {
        copper = 0;
        silver = 0;
        electrum = 0;
        gold = 0;
        platinum = 0;
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

