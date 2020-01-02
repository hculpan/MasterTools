package org.culpan.mastertools.model;

import java.util.ArrayList;
import java.util.List;

public class Encounter extends BaseModel {
    private String name;

    private final List<Monster> monsters = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Monster> getMonsters() {
        return monsters;
    }

    public long getTotalXp() {
        long totalXp = getMonsters().stream().filter(m -> m.isActive()).mapToInt(m -> m.getXp()).sum();
        long numMonsters = getMonsters().stream().filter(m -> m.isActive() && !m.isSummoned()).count();

        if (numMonsters == 1) {
            // do nothing
        } else if (numMonsters == 2) {
            totalXp *= 1.5;
        } else if (numMonsters < 7) {
            totalXp *= 2;
        } else if (numMonsters < 11) {
            totalXp *= 2.5;
        } else if (numMonsters < 15) {
            totalXp *= 3;
        } else {
            totalXp *= 4;
        }

        return totalXp;
    }
}
