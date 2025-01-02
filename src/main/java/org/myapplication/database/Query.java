package org.myapplication.database;

import java.util.ArrayList;
import java.util.Collections;

public class Query {

    private final String schema;
    private final Table table;
    private final ArrayList<Column> columns = new ArrayList<>();

    public Query(String schema, Table table) {
        this.schema = schema;
        this.table = table;
    }

    public Query(Table table) {
        this.schema = "safedose_v2";
        this.table = table;
    }

    public void setColumn(Column column) {
        columns.add(column);
    }

    public void setColumns(Column ...columns) {
        Collections.addAll(this.columns, columns);
    }

    public ArrayList<Column> getColumns() {
        return columns;
    }

    public String select() {
        StringBuilder query = new StringBuilder();

        query.append("SELECT ");
        if (!columns.isEmpty()) {
            query.append(columns.get(0).build());
            for (int i = 1; i < columns.size(); i++) {
                query.append(", ");
                query.append(columns.get(i).build());
            }
        } else {
            query.append("*");
        }
        query.append(" FROM ");
        query.append(table.build());

        return query.toString();
    }

    public static void main(String[] args) {
        Schema schema = new Schema("safedose_v2");
        Table table = new Table("camps", schema, "camp");
        Column campId = new Column("camp_id", table, "id");
        Column campName = new Column("location", table, "name");

        Query queryMaker = new Query(table);
        System.out.println(queryMaker.select());
        queryMaker.setColumns(campId, campName);
        System.out.println(queryMaker.select());
    }
}
