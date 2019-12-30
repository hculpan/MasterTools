package org.culpan.mastertools.dao;

import java.io.File;
import java.sql.*;
import java.util.List;

public abstract class BaseDao<T> {
    private static Connection conn;

    public interface QueryResultsInterface {
        Object evaluateResults(ResultSet set) throws SQLException;
    }

    public void logDbError(Throwable t) {
        t.printStackTrace();
    }

    protected void initDb(String dbPath) throws ClassNotFoundException, SQLException {
        conn = null;

        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        conn.setAutoCommit(false);
    }

    public static void closeDb() throws SQLException {
        if (conn != null) {
            conn.close();;
            conn = null;
        }
    }

    public Connection connection() {
        if (conn == null) {
            try {
                initDb(String.format("%s%s/.mastertools/mastertools.sqlite", System.getProperty("user.home"), File.separator));
            } catch (ClassNotFoundException | SQLException e) {
                logDbError(e);
            }
        }

        return conn;
    }

    public abstract boolean exists(T item);

    public abstract List<T> load();

    public abstract void addOrUpdate(T item, boolean autocommit);

    public void addOrUpdate(T item) {
        addOrUpdate(item, true);
    }

    public abstract void delete(T item);

    public abstract T find(T item);

    public abstract T findById(int id);

    protected abstract T itemFromResultSetRow(ResultSet rs) throws SQLException;

    protected boolean existsHelper(String tableName, int id) {
        boolean result = false;

        String sql = "select count(*) as cnt from " + tableName +
                " where id = " + id;
        Object o = executeItemQuery(sql, rs -> {
            if (rs.next()) {
                return (rs.getInt("cnt") > 0);
            }
            return false;
        });
        result = ((Boolean)o).booleanValue();

        return result;
    }

    protected T findHelper(String tableName, int id) {
        return executeItemQuery("select * from " + tableName + " where id = " + id,
                rs -> {
                    if (rs.next()) {
                        return itemFromResultSetRow(rs);
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    protected List<T> executeListQuery(String sql, QueryResultsInterface queryResultsInterface) {
        List<T> result = null;

        try {
            Statement stmt = connection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            result = (List<T>)queryResultsInterface.evaluateResults(rs);

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            logDbError(e);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    protected T executeItemQuery(String sql, QueryResultsInterface queryResultsInterface) {
        T result = null;

        try {
            Statement stmt = connection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            result = (T)queryResultsInterface.evaluateResults(rs);

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            logDbError(e);
        }

        return result;
    }

    public int getLastInsertId() {
        int result = -1;

        try {
            Statement stmt = connection().createStatement();
            ResultSet rs = stmt.executeQuery("select last_insert_rowid() as last_id");

            if (rs.next()) {
                result = rs.getInt("last_id");
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            logDbError(e);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    protected Object executeQuery(String sql, QueryResultsInterface queryResultsInterface) {
        Object result = null;

        try {
            Statement stmt = connection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            result = queryResultsInterface.evaluateResults(rs);

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            logDbError(e);
        }

        return result;
    }

    protected boolean executeUpdate(String sql, boolean commit) {
        try {
            int rowCount;
            Statement stmt = connection().createStatement();
            rowCount = stmt.executeUpdate(sql);
            stmt.close();
            if (commit) connection().commit();
            return rowCount > 0;
        } catch (SQLException e) {
            logDbError(e);

            return false;
        }
    }

    protected boolean executeUpdate(String sql) {
        return executeUpdate(sql, true);
    }



    public void commit() {
        try {
            connection().commit();
        } catch (SQLException e) {
            logDbError(e);
        }
    }
}
