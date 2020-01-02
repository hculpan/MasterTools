package org.culpan.mastertools.model;

public class Party extends BaseModel {
    private int memberCount;
    private int averageLevel;

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
