package org.culpan.mastertools.model;

public class Monster {
    private int id;
    private int encounterId;
    private int number;
    private String name;
    private int baseHp;
    private int health;
    private int ac;
    private int attk;
    private int dmg;
    private int dc;
    private String cr;
    private int xp;
    private boolean active = true;
    private boolean summoned = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(int encounterId) {
        this.encounterId = encounterId;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public void setBaseHp(int baseHp) {
        this.baseHp = baseHp;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
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

    public int getDmg() {
        return dmg;
    }

    public void setDmg(int dmg) {
        this.dmg = dmg;
    }

    public int getDc() {
        return dc;
    }

    public void setDc(int dc) {
        this.dc = dc;
    }

    public String getCr() {
        return cr;
    }

    public void setCr(String cr) {
        this.cr = cr;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSummoned() {
        return summoned;
    }

    public void setSummoned(boolean summoned) {
        this.summoned = summoned;
    }

    public String getIdentifier() {
        return String.format("%s %d", getName(), getNumber());
    }

    @Override
    public Object clone() {
        Monster m = new Monster();
        m.setEncounterId(encounterId);
        m.setNumber(number);
        m.setName(name);
        m.setBaseHp(baseHp);
        m.setHealth(health);
        m.setAc(ac);
        m.setAttk(attk);
        m.setDmg(dmg);
        m.setDc(dc);
        m.setCr(cr);
        m.setXp(xp);
        m.setActive(active);
        m.setSummoned(summoned);
        return m;
    }
}
