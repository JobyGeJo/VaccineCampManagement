package org.myapplication.database;

public class Table {

    private final String query;
    private final String tableName;

    public Table(String tableName, Schema schema) {
        this.query = String.format("%s.%s", schema, tableName);
        this.tableName = query;
    }

    public Table(String tableName, Schema schema, String alias) {
        this.query = String.format("%s.%s AS %s", schema, tableName, alias);
        this.tableName = alias;
    }

    public Table(String tableName) {
        String schema = "safedose_v2";
        this.query = String.format("%s.%s", schema, tableName);
        this.tableName = tableName;
    }

    public Table(String tableName, String alias) {
        String schema = "safedose_v2";
        this.query = String.format("%s.%s AS %s", schema, tableName, alias);
        this.tableName = alias;
    }

    @Override
    public String toString() {
        return tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public String build() {
        return query;
    }
}