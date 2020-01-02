package org.culpan.mastertools.dao;

import org.culpan.mastertools.model.Session;
import org.culpan.mastertools.model.SessionXp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SessionDao extends BaseDao<Session> {
    private final static DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm z");

    @Override
    public boolean exists(Session item) {
        return find(item) != null;
    }

    @Override
    public List<Session> load() {
        return null;
    }

    private void deleteSessionXps(int sessiondId, boolean autocommit) {
        executeUpdate("delete from session_xp where session_id = " + sessiondId, autocommit);
    }

    private void addSessionXps(List<SessionXp> sessionXpList, boolean autocommit) {
        for (SessionXp sessionXp : sessionXpList) {
            executeUpdate("insert into session_xp (session_id, xp, description) " +
                    "values (" + sessionXp.getSessionId() + "," + sessionXp.getXp() +
                    ",'" + sessionXp.getDescription() + "')", false);
            sessionXp.setId(getLastInsertId());
        }

        if (autocommit) commit();
    }

    public void addXpToSession(Session session, long xp, String description) {
        SessionXp sessionXp = new SessionXp();
        sessionXp.setXp((int)xp);
        sessionXp.setDescription(description);
        sessionXp.setSessionId(session.getId());
        List<SessionXp> sessionXpList = new ArrayList<>();
        sessionXpList.add(sessionXp);
        addSessionXps(sessionXpList, true);
        session.getSessionXpList().add(sessionXp);
    }

    @Override
    public boolean addOrUpdate(Session item, boolean autocommit) {
        boolean result = false;
        if (exists(item)) {
            String startDate = (item.getStartDate() != null ? dateFormat.format(item.getStartDate()) : null);
            String endDate = (item.getEndDate() != null ? dateFormat.format(item.getEndDate()) : null);

            String sql = "update sessions ";
            if (startDate != null) {
                sql += "set start_date = '" +  startDate + "',";
            } else {
                sql += "set start_date = null,";
            }
            if (endDate != null) {
                sql += "end_date = '" +  endDate + "' ";
            } else {
                sql += "end_date = null ";
            }
            sql += "where id = " + item.getId();

            result = executeUpdate(sql, false);
            if (result) deleteSessionXps(item.getId(), false);
        } else {
            String startDate = (item.getStartDate() != null ? dateFormat.format(item.getStartDate()) : null);
            String endDate = (item.getEndDate() != null ? dateFormat.format(item.getEndDate()) : null);

            String sql = "insert into sessions (start_date, end_date) values (";
            if (startDate != null) {
                sql += "'" +  startDate + "',";
            } else {
                sql += "null,";
            }
            if (endDate != null) {
                sql += "'" +  endDate + "' )";
            } else {
                sql += "null )";
            }

            result = executeUpdate(sql, false);
            if (result) item.setId(getLastInsertId());
        }
        addSessionXps(item.getSessionXpList(), false);
        commit();

        return result;
    }

    @Override
    public void delete(Session item) {

    }

    @Override
    public Session find(Session item) {
        return findById(item.getId());
    }

    public List<SessionXp> getSessionXps(int sessionId) {
        List<SessionXp> sessionXps = new ArrayList<>();

        executeQuery("select * from session_xp where session_id = " + sessionId,
                rs -> {
                    while (rs.next()) {
                        SessionXp sessionXp = new SessionXp();
                        sessionXp.setId(rs.getInt("id"));
                        sessionXp.setSessionId(sessionId);
                        sessionXp.setXp(rs.getInt("xp"));
                        sessionXp.setDescription(rs.getString("description"));
                        sessionXps.add(sessionXp);
                    }
                   return null;
                });

        return sessionXps;
    }

    public Session getOpenSession() {
        List<Session> sessions = executeListQuery("select id from sessions " +
                        "where end_date is null and start_date is not null",
                rs -> {
                    List<Session> sessionList = new ArrayList<>();

                    while (rs.next()) {
                        int id = rs.getInt("id");
                        sessionList.add(findById(id));
                    }

                    return sessionList;
                });
        if (sessions.size() == 0) {
            return null;
        } else if (sessions.size() > 1) {
            throw new RuntimeException("More than one open session");
        } else {
            return sessions.get(0);
        }
    }

    @Override
    public Session findById(int id) {
        return executeItemQuery("select * from sessions where id = " + id,
                rs -> {
                    if (rs.next()) {
                        return itemFromResultSetRow(rs);
                    }
                    return null;
                });
    }

    @Override
    protected Session itemFromResultSetRow(ResultSet rs) throws SQLException {
        Session session = new Session();

        session.setId(rs.getInt("id"));
        try {
            String startDate = rs.getString("start_date");
            if (startDate != null && !startDate.isEmpty()) {
                session.setStartDate(dateFormat.parse(rs.getString("start_date")));
            }

            String endDate = rs.getString("end_date");
            if (endDate != null && !endDate.isEmpty()) {
                session.setEndDate(dateFormat.parse(rs.getString("end_date")));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        session.getSessionXpList().addAll(getSessionXps(session.getId()));
        return session;
    }
}
