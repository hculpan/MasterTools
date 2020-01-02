package org.culpan.mastertools.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Session extends BaseModel {
    private Date startDate;
    private Date endDate;

    private final List<SessionXp> sessionXpList = new ArrayList<>();

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
