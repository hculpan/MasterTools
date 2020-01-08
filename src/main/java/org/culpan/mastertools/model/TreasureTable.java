package org.culpan.mastertools.model;

public class TreasureTable extends BaseModel {
    private String tableIdentifier;
    private int selectionNum;
    private String item;

    public String getTableIdentifier() {
        return tableIdentifier;
    }

    public void setTableIdentifier(String tableIdentifier) {
        this.tableIdentifier = tableIdentifier;
    }

    public int getSelectionNum() {
        return selectionNum;
    }

    public void setSelectionNum(int selectionNum) {
        this.selectionNum = selectionNum;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }
}
