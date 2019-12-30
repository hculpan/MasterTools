package org.culpan.mastertools.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Session {
    private int id;
    private Date startDate;
    private Date endDate;

    private final List<SessionXp> sessionXpList = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<SessionXp> getSessionXpList() {
        return sessionXpList;
    }

    public int getSessionXp() {
        return sessionXpList.stream().mapToInt(s -> s.getXp()).sum();
    }
}
