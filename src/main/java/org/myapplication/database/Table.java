package org.myapplication.database;

public class Table {

    private final String query;
    private final String tableName;

    Table(String tableName, Schema schema) {
        this.query = String.format("%s.%s", schema, tableName);
        this.tableName = query;
    }

    Table(String tableName, Schema schema, String alias) {
        this.query = String.format("%s.%s AS %s", schema, tableName, alias);
        this.tableName = alias;
    }

    @Override
    public String toString() {
        return tableName;
    }

    public String build() {
        return query;
    }
}