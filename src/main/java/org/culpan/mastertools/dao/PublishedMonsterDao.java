package org.culpan.mastertools.dao;

import org.culpan.mastertools.model.BaseModel;
import org.culpan.mastertools.model.Monster;
import org.culpan.mastertools.model.PublishedMonster;
import org.culpan.mastertools.util.JsonParser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PublishedMonsterDao extends BaseDao<PublishedMonster> {
    public class MonsterSummary extends BaseModel {
        private String name;
        private String cr;

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
    }

    @Override
    public boolean exists(PublishedMonster item) {
        return (Boolean)executeQuery("select count(*) as cnt from published_monsters where id = " + item.getId(),
                rs -> {
                    if (rs.next()) return rs.getInt("cnt") > 0;
                    return false;
                });
    }

    @Override
    public List<PublishedMonster> load() {
        throw new RuntimeException("load for PublishedMonster not implemenbted");
    }

    @SuppressWarnings("unchecked")
    public List<MonsterSummary> getAllSummaries() {
        return (List<MonsterSummary>)executeQuery("select id, name, cr from published_monsters ",
                rs -> {
                    List<MonsterSummary> result = new ArrayList<>();

                    while (rs.next()) {
                        MonsterSummary summary = new MonsterSummary();
                        summary.setId(rs.getInt("id"));
                        summary.setName(rs.getString("name"));
                        summary.setCr(rs.getString("cr"));
                        result.add(summary);
                    }

                    return result;
                });
    }

    @SuppressWarnings("unchecked")
    public List<MonsterSummary> matchesByName(String name) {
        return (List<MonsterSummary>)executeQuery("select id, name, cr from published_monsters " +
                        "where name like '%" + name + "%'",
                rs -> {
                    List<MonsterSummary> result = new ArrayList<>();

                    while (rs.next()) {
                        MonsterSummary summary = new MonsterSummary();
                        summary.setId(rs.getInt("id"));
                        summary.setName(rs.getString("name"));
                        summary.setCr(rs.getString("cr"));
                        result.add(summary);
                    }

                    return result;
                });
    }

    @Override
    public boolean addOrUpdate(PublishedMonster item, boolean autocommit) {
        boolean result = false;
        result = executeUpdate(String.format("insert into published_monsters " +
                "(name, cr, base_hp, ac, attk, dmg, dc, xp, json) " +
                "values('%s', '%s', %d, %d, %d, '%s', %d, %d, '%s')",
                item.getName().replace("'", ""), item.getCr(), item.getBaseHp(), item.getAc(),
                item.getAttk(), item.getDmg(), item.getDc(), item.getXp(), item.getJson()), autocommit);
        if (result) item.setId(getLastInsertId());
        return result;
    }

    public String getJsonForId(int id) {
        return (String)executeQuery("select json from published_monsters where id = " + id,
                rs -> {
                   if (rs.next()) return rs.getString("json");
                   return null;
                });
    }

    public String getActionsHtml(int publishedMonsterId, String name) {
        if (publishedMonsterId <= 0) return null;

        String json = getJsonForId(publishedMonsterId);
        JsonParser parser = new JsonParser();
        JsonParser.JsonObject root = (JsonParser.JsonObject)parser.parse(json);
        JsonParser.JsonArray actions = (JsonParser.JsonArray)root.getProperty("actions");

        String xml = "";

        if (actions.size() > 0) {
            xml += String.format("<h3>Actions for %s</h3><hr/><br/>", name);
            for (int i = 0; i < actions.size(); i++) {
                JsonParser.JsonObject action = (JsonParser.JsonObject) actions.get(i);
                xml += String.format("<em>%s</em><br/> %s <br/><br/>",
                        action.getPropertyValue("name"),
                        action.getPropertyValue("desc"));
            }
        }

        actions = (JsonParser.JsonArray)root.getProperty("special_abilities");
        if (actions.size() > 0) {
            xml += String.format("<hr/><h3>Special Abilities for %s</h3><hr/><br/>", name);
            for (int i = 0; i < actions.size(); i++) {
                JsonParser.JsonObject action = (JsonParser.JsonObject) actions.get(i);
                xml += String.format("<em>%s</em><br/> %s <br/><br/>",
                        action.getPropertyValue("name"),
                        action.getPropertyValue("desc"));
            }
        }

        return xml;
    }

    public String getActionsHtml(Monster m) {
        return getActionsHtml(m.getPublishedMonsterId(), m.getName());
    }

    public void deleteAll() {
        executeUpdate("delete from published_monsters ");
    }

    @Override
    public void delete(PublishedMonster item) {
        throw new RuntimeException("delete for PublishedMonster not implemented");
    }

    @Override
    public PublishedMonster findById(int id) {
        return executeItemQuery("select * from published_monsters where id = " + id,
                rs -> {
                    if (rs.next()) return itemFromResultSetRow(rs);
                    return null;
                });
    }

    @Override
    protected PublishedMonster itemFromResultSetRow(ResultSet rs) throws SQLException {
        PublishedMonster monster = new PublishedMonster();

        monster.setId(rs.getInt("id"));
        monster.setName(rs.getString("name"));
        monster.setCr(rs.getString("cr"));
        monster.setBaseHp(rs.getInt("base_hp"));
        monster.setAc(rs.getInt("ac"));
        monster.setAttk(rs.getInt("attk"));
        monster.setDmg(rs.getString("dmg"));
        monster.setDc(rs.getInt("dc"));
        monster.setXp(rs.getInt("xp"));
        monster.setJson(rs.getString("json"));

        return monster;
    }
}
