package rl.main;

import java.util.List;

public class TableConfig {
    private String table;
    List<ColumnConfig> columns;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "TableConfig{" +
                "table='" + table + '\'' +
                ", columns=" + columns +
                '}';
    }
}
