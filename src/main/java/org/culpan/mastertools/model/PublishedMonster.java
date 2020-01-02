package org.culpan.mastertools.model;

public class PublishedMonster extends BaseModel {
    private String name = "";
    private String cr = "";
    private int baseHp;
    private int ac;
    private int attk;
    private String dmg = "";
    private int dc;
    private int xp;
    private String json = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCr() {
        return cr;
    }

    public void setCr(String cr) {
        this.cr = cr;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public void setBaseHp(int baseHp) {
        this.baseHp = baseHp;
    }

    public int getAc() {
        return ac;
    }

    public void setAc(int ac) {
        this.ac = ac;
    }

    public int getAttk() {
        return attk;
    }

    public void setAttk(int attk) {
        this.attk = attk;
    }

    public String getDmg() {
        return dmg;
    }

    public void setDmg(String dmg) {
        this.dmg = dmg;
    }

    public int getDc() {
        return dc;
    }

    public void setDc(int dc) {
        this.dc = dc;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
