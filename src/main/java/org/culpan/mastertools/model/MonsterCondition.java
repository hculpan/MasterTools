package org.culpan.mastertools.model;

public class MonsterCondition extends BaseModel {
    private static final String conditionAbr [] = {
            "Blind", "Charm", "Deaf", "Fa", "Fr", "Gr", "Inc", "Inv", "Pa", "Pe", "Po", "Pr", "Re", "St", "Un"
    };

    private static final String CONDITION_UNKNOWN = "UNKOWN";

    private int monsterId;
    private String condition;

    public int getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(int monsterId) {
        this.monsterId = monsterId;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getConditionAbr() {
        if (condition == null || condition.isEmpty()) return CONDITION_UNKNOWN;

        for (String abr : conditionAbr) {
            if (condition.startsWith(abr)) return abr;
        }

        return CONDITION_UNKNOWN;
    }

    @Override
    public Object clone() {
        MonsterCondition m = new MonsterCondition();
        m.setMonsterId(monsterId);
        m.setCondition(condition);
        return m;
    }
}
