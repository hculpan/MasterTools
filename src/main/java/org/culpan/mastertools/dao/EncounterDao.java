package org.culpan.mastertools.dao;

import org.culpan.mastertools.model.Encounter;
import org.culpan.mastertools.model.Monster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EncounterDao extends BaseDao<Encounter> {
    private final static String CURRENT_ENCOUNTER = "current encounter";
    private final static int CURRENT_ENCOUNTER_ID = 1;

    private final static MonsterDao monsterDao = new MonsterDao();

    @Override
    public boolean exists(Encounter item) {
        return findById(item.getId()) != null;
    }

    public boolean exists(String name) {
        return (Boolean)executeQuery("select * from encounters where name = '" + name + "'",
                rs -> !rs.next());
    }

    @Override
    public List<Encounter> load() {
        return null;
    }

    @Override
    public boolean addOrUpdate(Encounter item, boolean autocommit) {
        return false;
    }

    public void deleteMonsters(Encounter item) {
        executeUpdate("delete from monsters where encounter_id = " + item.getId());
        item.getMonsters().clear();
    }

    @Override
    public void delete(Encounter item) {

    }

    @Override
    public Encounter find(Encounter item) {
        return findById(item.getId());
    }

    @Override
    public Encounter findById(int id) {
        return executeItemQuery("select * from encounters where id = " + id,
                rs -> {
                    if (rs.next()) return itemFromResultSetRow(rs);
                    return null;
                });
    }

    @Override
    protected Encounter itemFromResultSetRow(ResultSet rs) throws SQLException {
        Encounter encounter = new Encounter();
        encounter.setId(rs.getInt("id"));
        encounter.setName(rs.getString("name"));
        encounter.getMonsters().addAll(monsterDao.loadForEncounter(encounter.getId()));
        return encounter;
    }

    public void addMonstersToCurrentEncounter(Monster monster, int count) {
        int monsterNum = getMaxNumberInEncounter(CURRENT_ENCOUNTER_ID, monster.isSummoned());
        if (monsterNum == 0 && monster.isSummoned()) monsterNum = 99;
        for (int i = 0; i < count; i++) {
            Monster m = (Monster)monster.clone();
            m.setEncounterId(CURRENT_ENCOUNTER_ID);
            m.setHealth(m.getBaseHp());
            m.setNumber(monsterNum + i + 1);
            monsterDao.addOrUpdate(m, false);
        }
        commit();
    }

    public int getMaxNumberInEncounter(int encounterId, boolean summoned) {
        Object o = executeQuery("select max(number) as max from monsters " +
                        "where encounter_id = " + encounterId +
                        "  and summoned = " + (summoned ? 1 : 0),
                rs -> {
                    Integer result = 0;

                    if (rs.next()) {
                        result = rs.getInt("max");
                    }

                    return result;
                });
        return (Integer)o;
    }

    public Encounter getCurrentEncounter() {
        return findById(CURRENT_ENCOUNTER_ID);
    }
}
