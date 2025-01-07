package org.myapplication.database;

import org.myapplication.modules.Module.*;

import java.util.*;
import java.util.stream.Collectors;

public class Query {

//    private final String schema;
    private final Table table;
    private final ArrayList<Column> columns = new ArrayList<>();
    private final ArrayList<Condition> conditions = new ArrayList<>();
    private final ArrayList<Column> groupBy = new ArrayList<>();
    private final ArrayList<Join> joinConditions = new ArrayList<>();

    private String query;

    public Query(Table table) {
        this.table = table;
    }


    public void addColumn(Column column) {
        columns.add(column);
    }

    public void addColumn(Columns column) {
        columns.add(column.getColumn());
    }

    public void addColumns(Column ...columns) {
        Collections.addAll(this.columns, columns);
    }

    public void addColumns(Columns ...columns) {
        Collections.addAll(this.columns, Arrays.stream(columns).map(Columns::getColumn).toArray(Column[]::new));
    }

    public String setColumns() {
        if (columns.isEmpty()) {
            return Constants.ALL.toString();
        }
        return columns.stream().map(Column::build).collect(Collectors.joining(", "));
    }


    public void addCondition(Condition condition) {
        conditions.add(condition);
    }

    public void addConditions(Condition ...conditions) {
        Collections.addAll(this.conditions, conditions);
    }

    public String setConditions() {
        if (conditions.isEmpty()) {
            return "";
        }
        return "WHERE " + conditions.stream().map(Condition::toString).collect(Collectors.joining(" AND "));
    }


    public void addJoin(Join join) {
        joinConditions.add(join);
    }

    public void addJoins(Join ...joins) {
        Collections.addAll(this.joinConditions, joins);
    }

    public void addJoin(Joins defaultJoin) {
        joinConditions.add(defaultJoin.getJoin());
    }

    public void addJoins(Joins...defaultJoins) {
        Collections.addAll(this.joinConditions, Arrays.stream(defaultJoins).map(Joins::getJoin).toArray(Join[]::new));
    }

    public String setJoins() {
        if (joinConditions.isEmpty()) {
            return "";
        }
        return joinConditions.stream().map(Join::toString).collect(Collectors.joining(" "));
    }

    public void addGroupBy(Column column) {
        groupBy.add(column);
    }

    public void addGroupBy(Column ...columns) {
        Collections.addAll(this.groupBy, columns);
    }

    public String setGroupBy() {
        if (groupBy.isEmpty()) {
            return "";
        }
        return "GROUP BY " + groupBy.stream().map(Column::build).collect(Collectors.joining(", "));
    }

    public void select() {
        this.query = String.format(
                "SELECT %s FROM %s %s %s %s",
                setColumns(),
                table.build(),
                setJoins(),
                setConditions(),
                setGroupBy()
        );
    }


    public String getQuery() {
        if (query == null) {
            throw new IllegalStateException("Query has not been initialized");
        }
        String result = query;
        query = null;
        columns.clear();
        conditions.clear();
        joinConditions.clear();
        groupBy.clear();
        return result;
    }

    public static void main(String[] args) {
        Schema schema = new Schema("safedose_v2");
        Table table = new Table("camps", schema, "camp");
        Column campId = new Column("camp_id", table, "id");
        Column campName = new Column("location", table, "name");
        Condition not_in = new Condition(Operator.NOT_IN, campName, "chennai", "banglore");
        Condition between = new Condition(Operator.BETWEEN, campId, 2, 10);
        Condition condition = new Condition(Operator.OR, not_in, between);


        Query queryMaker = new Query(table);
        queryMaker.select();
        System.out.println(queryMaker.getQuery());
        queryMaker.addColumns(campId, campName);
        queryMaker.select();
        System.out.println(queryMaker.getQuery());
        queryMaker.addCondition(condition);
        queryMaker.select();
        System.out.println(queryMaker.getQuery());
    }
}
