package org.culpan.mastertools.dao;

import org.culpan.mastertools.model.TreasureTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TreasureTableDao extends BaseDao<TreasureTable> {
    @Override
    public boolean exists(TreasureTable item) {
        return findById(item.getId()) != null;
    }

    @Override
    public List<TreasureTable> load() {
        throw new RuntimeException("Not implemented");
    }

    public TreasureTable findLastMatch(String tableIdentifier, int selectedNum) {
        return executeItemQuery(String.format("select * from treasure_table " +
                "where table_identifier = '%s' " +
                "  and selection_num >= %d " +
                "  order by selection_num asc; ",
                tableIdentifier,
                selectedNum),
                rs -> {
                    if (rs.next()) return itemFromResultSetRow(rs);
                    return null;
                });
    }

    @Override
    public boolean addOrUpdate(TreasureTable item, boolean autocommit) {
        boolean result;

        if (exists(item)) {
            result = executeUpdate(String.format("update treasure_tables " +
                    "set table_identifier = '%s', " +
                    "    selection_num = %d, " +
                    "    item = '%s' " +
                    "where id = %d",
                    item.getTableIdentifier(),
                    item.getSelectionNum(),
                    item.getItem(),
                    item.getId()), autocommit);
        } else {
            result = executeUpdate(String.format("insert into treasure tables " +
                    "(table_identifier, selection_num, item) " +
                    "values ('%s', %d, '%s')",
                    item.getTableIdentifier(),
                    item.getSelectionNum(),
                    item.getItem()), autocommit);
            item.setId(getLastInsertId());
        }

        return result;
    }

    @Override
    public void delete(TreasureTable item) {
        executeUpdate("delete from treasure_table where id = " + item.getId());
    }

    @Override
    public TreasureTable findById(int id) {
        return executeItemQuery("select * from treasure_table where id = " + id,
                rs -> {
                    if (rs.next()) return itemFromResultSetRow(rs);
                    return null;
                });
    }

    @Override
    protected TreasureTable itemFromResultSetRow(ResultSet rs) throws SQLException {
        TreasureTable result = new TreasureTable();

        result.setId(rs.getInt("id"));
        result.setTableIdentifier(rs.getString("table_identifier"));
        result.setSelectionNum(rs.getInt("selection_num"));
        result.setItem(rs.getString("item"));

        return result;
    }
}
