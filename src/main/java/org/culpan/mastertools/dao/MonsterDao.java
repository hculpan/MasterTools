package org.culpan.mastertools.dao;

import org.culpan.mastertools.model.Monster;
import org.culpan.mastertools.model.MonsterCondition;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MonsterDao extends BaseDao<Monster> {
    @Override
    public boolean exists(Monster item) {
        return find(item) != null;
    }

    @Override
    public List<Monster> load() {
        return null;
    }

    public List<Monster> loadForEncounter(int encounterId) {
        return executeListQuery("select * from monsters " +
                        " where encounter_id = " + encounterId +
                        " order by number ",
                rs -> {
                    List<Monster> monsters = new ArrayList<>();
                    while (rs.next()) {
                        monsters.add(itemFromResultSetRow(rs));
                    }
                    return monsters;
                });
    }

    @SuppressWarnings("unchecked")
    public List<MonsterCondition> loadConditions(int monsterId) {
        return (List<MonsterCondition>) executeQuery("select * from monster_conditions where monster_id = " + monsterId,
                rs -> {
                    List<MonsterCondition> conditions = new ArrayList<>();

                    while (rs.next()) {
                        MonsterCondition mc = new MonsterCondition();
                        mc.setId(rs.getInt("id"));
                        mc.setMonsterId(rs.getInt("monster_id"));
                        mc.setCondition(rs.getString("condition"));
                        conditions.add(mc);
                    }

                    return conditions;
                });
    }

    public void deleteConditions(Monster monster, boolean autocommit) {
        executeUpdate("delete from monster_conditions where monster_id = " + monster.getId(), autocommit);
        monster.getConditions().clear();
    }

    private void clearConditionsInDb(int monsterId, boolean autocommit) {
        executeUpdate("delete from monster_conditions where monster_id = " + monsterId, autocommit);
    }

    public boolean addOrUpdateConditions(Monster monster, boolean autocommit) {
        boolean result = true;

        clearConditionsInDb(monster.getId(), false);

        for (MonsterCondition mc : monster.getConditions()) {
            mc.setMonsterId(monster.getId());
            result = executeUpdate("insert into monster_conditions (monster_id, condition) " +
                    "values (" + monster.getId() + ",'" + mc.getCondition() + "')", false);
            if (!result) break;
            mc.setId(getLastInsertId());
        }

        if (result && autocommit) commit();

        return result;
    }

    @Override
    public boolean addOrUpdate(Monster item, boolean autocommit) {
        boolean result = false;
        if (exists(item)) {
            result = executeUpdate("update monsters " +
                    "set encounter_id = " + item.getEncounterId() +
                    ",   number = " + item.getNumber() +
                    ",   name = '" + item.getName() + "' " +
                    ",   base_hp = " + item.getBaseHp() +
                    ",   health = " + item.getHealth() +
                    ",   ac = " + item.getAc() +
                    ",   attk = " + item.getAttk() +
                    ",   dmg = '" + item.getDmg() + "' " +
                    ",   dc = " + item.getDc() +
                    ",   cr = '" + item.getCr() + "' " +
                    ",   xp = " + item.getXp() +
                    ",   active = " + (item.isActive() ? 1 : 0) +
                    ",   summoned = " + (item.isSummoned() ? 1 : 0) +
                    ",   notes = '" + (item.getNotes() != null ? item.getNotes() : "") + "', " +
                    ",   published_monster_id = " + item.getPublishedMonsterId() +
                    " where id = " + item.getId(), autocommit);
        } else {
            if (executeUpdate("insert into monsters " +
                    "(encounter_id, number, name, base_hp, health, ac, attk, dmg, dc, cr, xp, active, " +
                            "summoned, notes, published_monster_id) " +
                    "values (" + item.getEncounterId() + "," + item.getNumber() + ",'" + item.getName() +
                    "'," + item.getBaseHp() + "," + item.getHealth() + "," + item.getAc() +
                    "," + item.getAttk() + ",'" + item.getDmg() + "'," + item.getDc() +
                    ",'" + item.getCr() + "'," + item.getXp() + "," + (item.isActive() ? 1 : 0) +
                    "," + (item.isSummoned() ? 1 : 0) + ",'" + (item.getNotes() != null ? item.getNotes() : "") +
                    "'," + item.getPublishedMonsterId() + ")"
                    , autocommit)) {
                item.setId(getLastInsertId());
                result = true;
            }
        }
        if (result) result = addOrUpdateConditions(item, autocommit);

        return result;
    }

    @Override
    public void delete(Monster item) {
        executeUpdate("delete from monsters where id = " + item.getId());
    }

    @Override
    public Monster find(Monster item) {
        return findById(item.getId());
    }

    @Override
    public Monster findById(int id) {
        return executeItemQuery("select * from monsters where id = " + id,
                rs -> {
                    if (rs.next()) return itemFromResultSetRow(rs);
                    return null;
                });
    }

    @Override
    protected Monster itemFromResultSetRow(ResultSet rs) throws SQLException {
        Monster monster = new Monster();
        monster.setId(rs.getInt("id"));
        monster.setEncounterId(rs.getInt("encounter_id"));
        monster.setNumber(rs.getInt("number"));
        monster.setName(rs.getString("name"));
        monster.setBaseHp(rs.getInt("base_hp"));
        monster.setHealth(rs.getInt("health"));
        monster.setAc(rs.getInt("ac"));
        monster.setAttk(rs.getInt("attk"));
        monster.setDmg(rs.getString("dmg"));
        monster.setDc(rs.getInt("dc"));
        monster.setCr(rs.getString("cr"));
        monster.setXp(rs.getInt("xp"));
        monster.setActive(rs.getInt("active") > 0);
        monster.setSummoned(rs.getInt("summoned") > 0);
        monster.setNotes(rs.getString("notes"));
        monster.setPublishedMonsterId(rs.getInt("published_monster_id"));

        monster.getConditions().clear();
        monster.getConditions().addAll(loadConditions(monster.getId()));

        return monster;
    }
}
