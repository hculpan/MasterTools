package org.culpan.mastertools.model;

public class Party {
    private int id;
    private int memberCount;
    private int averageLevel;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getAverageLevel() {
        return averageLevel;
    }

    public void setAverageLevel(int averageLevel) {
        this.averageLevel = averageLevel;
    }
}
