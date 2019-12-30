package org.culpan.mastertools.dao;

import org.culpan.mastertools.model.Party;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PartyDao extends BaseDao<Party> {
    @Override
    public boolean exists(Party item) {
        return findById(item.getId()) != null;
    }

    @Override
    public List<Party> load() {
        List<Party> parties = new ArrayList<>();
        parties.add(findById(1));
        return parties;
    }

    public Party getCurrentParty() {
        List<Party> parties = load();
        if (parties.size() != 1) {
            throw new RuntimeException("Invalid number of rows in Party table");
        }
        return parties.get(0);
    }

    @Override
    public void addOrUpdate(Party item, boolean autocommit) {
        if (!exists(item)) {
            throw new RuntimeException("add operation not supported on Party");
        }

        executeUpdate("update party " +
                "set member_count = " + item.getMemberCount() +
                ",   average_level = " + item.getAverageLevel() +
                " where id = 1");
    }

    @Override
    public void delete(Party item) {
        throw new RuntimeException("Delete operation not supported on Party");
    }

    @Override
    public Party find(Party item) {
        return findById(item.getId());
    }

    @Override
    public Party findById(int id) {
        return executeItemQuery("select * from party where id = " + id,
                rs -> {
                    Party party = null;

                    if (rs.next()) {
                        return itemFromResultSetRow(rs);
                    }

                    return party;
                });
    }

    @Override
    protected Party itemFromResultSetRow(ResultSet rs) throws SQLException {
        Party party = new Party();

        party.setId(rs.getInt("id"));
        party.setAverageLevel(rs.getInt("average_level"));
        party.setMemberCount(rs.getInt("member_count"));

        return party;
    }
}
