package org.myapplication.database;


public class Column {

    private final String query;
    private final String columnName;

    Column(String columnName, Table table) {
        this.query = String.format("%s.%s", table, columnName);
        this.columnName = query;
    }

    Column(String columnName, Table table, String alias) {
        this.query = String.format("%s.%s AS %s", table, columnName, alias);
        this.columnName = alias;
    }

    @Override
    public String toString() {
        return columnName;
    }

    public String build() {
        return query;
    }

}
