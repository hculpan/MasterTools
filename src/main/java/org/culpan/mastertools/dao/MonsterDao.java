package org.culpan.mastertools.dao;

import org.culpan.mastertools.model.Monster;

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
                        "where encounter_id = " + encounterId,
                rs -> {
                    List<Monster> monsters = new ArrayList<>();
                    while (rs.next()) {
                        monsters.add(itemFromResultSetRow(rs));
                    }
                    return monsters;
                });
    }

    @Override
    public void addOrUpdate(Monster item, boolean autocommit) {
        if (exists(item)) {
            executeUpdate("update monsters " +
                    "set encounter_id = " + item.getEncounterId() +
                    ",   number = " + item.getNumber() +
                    ",   name = '" + item.getName() + "' " +
                    ",   base_hp = " + item.getBaseHp() +
                    ",   health = " + item.getHealth() +
                    ",   ac = " + item.getAc() +
                    ",   attk = " + item.getAttk() +
                    ",   dmg = " + item.getDmg() +
                    ",   dc = " + item.getDc() +
                    ",   cr = '" + item.getCr() + "' " +
                    ",   xp = " + item.getXp() +
                    ",   active = " + (item.isActive() ? 1 : 0) +
                    ",   summoned = " + (item.isSummoned() ? 1 : 0) +
                    " where id = " + item.getId(), autocommit);
        } else {
            if (executeUpdate("insert into monsters " +
                    "(encounter_id, number, name, base_hp, health, ac, attk, dmg, dc, cr, xp, active, summoned) " +
                    "values (" + item.getEncounterId() + "," + item.getNumber() + ",'" + item.getName() +
                    "'," + item.getBaseHp() + "," + item.getHealth() + "," + item.getAc() +
                    "," + item.getAttk() + "," + item.getDmg() + "," + item.getDc() +
                    ",'" + item.getCr() + "'," + item.getXp() + "," + (item.isActive() ? 1 : 0) +
                    "," + (item.isSummoned() ? 1 : 0) + ")", autocommit)) {
                item.setId(getLastInsertId());
            }
        }
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
        monster.setDmg(rs.getInt("dmg"));
        monster.setDc(rs.getInt("dc"));
        monster.setCr(rs.getString("cr"));
        monster.setXp(rs.getInt("xp"));
        monster.setActive(rs.getInt("active") > 0);
        monster.setSummoned(rs.getInt("summoned") > 0);
        return monster;
    }
}
