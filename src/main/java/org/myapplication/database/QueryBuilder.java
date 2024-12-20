package org.myapplication.database;

import org.myapplication.enumerate.Status;
import org.myapplication.exceptions.QueryException;
import org.myapplication.utils.ColoredOutput;

public class QueryBuilder {
    private final StringBuilder query;
    private final String schema;
    private boolean hasWhereClause = false;
    private boolean isGroupOpen = false;

    public QueryBuilder(String schema) {
        this.schema = schema;
        this.query = new StringBuilder();
    }

    public QueryBuilder() {
        this.schema = "safedose_v2";
        this.query = new StringBuilder();
    }

    // Start a grouped condition with (
    public QueryBuilder openGroup() {
        query.append("(");
        isGroupOpen = true;
        return this;
    }

    // Close a grouped condition with )
    public QueryBuilder closeGroup() {
        query.append(") ");
        isGroupOpen = false;
        return this;
    }

    // Close a grouped condition with )
    public QueryBuilder closeGroup(String as) {
        query.append(") AS ").append(as).append(" ");
        return this;
    }

    // SELECT clause
    public QueryBuilder select(String table, String... columns) {
        query.append("SELECT ");
        this.columns(columns);
        query.append(" FROM ").append(this.table(table)).append(" ");
        return this;
    }

    public QueryBuilder select() {
        query.append("SELECT ");
        return this;
    }

    private void columns(String... columns) {
        if (columns.length == 0) {
            query.append("*");
        } else {
            query.append(String.join(", ", columns));
        }
    }

    private String table(String table) {
        return schema + "." + table;
    }

    // INNER JOIN clause
    public QueryBuilder join(String table, String... condition) {
        if (condition.length == 0) {
            return this;
        }

        if (table.contains("SELECT")) {
            query.append("JOIN ").append(table).append(" ON ").append(condition[0]).append(" ");
        } else {
            query.append("JOIN ").append(this.table(table)).append(" ON ").append(condition[0]).append(" ");
        }

        for (int i = 1; i < condition.length; i++) {
            query.append("AND ").append(condition[i]).append(" ");
        }
        return this;
    }

    // LEFT JOIN clause
    public QueryBuilder leftJoin(String table, String... condition) {
        query.append("LEFT ");
        return this.join(table, condition);
    }

    // WHERE clause
    public QueryBuilder where(String condition) {
        if (!hasWhereClause) {
            query.append("WHERE ");
            hasWhereClause = true;
        } else if (!isGroupOpen) {
            query.append("AND ");
        }
        query.append(condition).append(" ");
        return this;
    }

    // OR clause
    public QueryBuilder or(String condition) {
        if (!hasWhereClause) {
            query.append("WHERE ");
            hasWhereClause = true;
        } else {
            query.append("OR ");
        }
        query.append(condition).append(" ");
        return this;
    }

    // Open a group
    public QueryBuilder openConditionalGroup() {
        if (!hasWhereClause) {
            query.append("WHERE ");
            hasWhereClause = true;
        }
        query.append("(");
        isGroupOpen = true; // Set flag to indicate group is open
        return this;
    }

    // Close a group
    public QueryBuilder closeOpenConditionalGroup() {
        query.append(") ");
        isGroupOpen = false; // Reset flag after closing the group
        return this;
    }

    // Add an AND condition (reuse where)
    public QueryBuilder and(String condition) {
        return where(condition);
    }

    // INSERT INTO clause
    public QueryBuilder insertInto(String table, String... columns) throws QueryException {
        if (columns.length == 0) {
            throw new QueryException("INSERTION with no columns: NOT SUPPORTED");
        }

        query.append("INSERT INTO ")
                .append(this.table(table))
                .append(" (");
        this.columns(columns);
        query.append(") VALUES ");
        query.append("(").append("?").append(", ?".repeat(columns.length - 1)).append(") ");
        return this;
    }

    // UPDATE clause
    public QueryBuilder update(String tableName) {
        query.append("UPDATE ").append(table(tableName)).append(" SET ");
        return this;
    }

    // Set values for UPDATE
    public QueryBuilder set(String column) {
        if (query.toString().trim().endsWith("SET")) {
            query.append(column).append(" = ").append("? ");
        } else {
            query.append(", ").append(column).append(" = ").append("? ");
        }
        return this;
    }

    public QueryBuilder set(String column, String value) {
        if (query.toString().trim().endsWith("SET")) {
            query.append(column).append(" = ").append(value).append(" ");
        } else {
            query.append(", ").append(column).append(" = ").append(value).append(" ");
        }
        return this;
    }


    // DELETE FROM clause
    public QueryBuilder deleteFrom(String table) {
        query.append("DELETE FROM ").append(this.table(table)).append(" ");
        return this;
    }

    public QueryBuilder returning(String... column) {
        query.append("RETURNING ");
        columns(column);
        return this;
    }

    public QueryBuilder orderby(String column) {
        query.append("ORDER BY ").append(column).append(" ");
        return this;
    }

    public QueryBuilder limit(int limit) {
        query.append("LIMIT ").append(limit).append(" ");
        return this;
    }

    public QueryBuilder groupby(String... columns) {
        if (columns.length == 0) {
            return this;
        }

        query.append("GROUP BY ");
        query.append(String.join(", ", columns)).append(" ");
        return this;
    }

    public QueryBuilder write(String anything) {
        query.append(anything).append(" ");
        return this;
    }

    public QueryBuilder write(QueryBuilder subQuery) {
        query.append(subQuery.build()).append(" ");
        return this;
    }

    // Build the final query
    public String build() {
        return query.toString().trim();
    }

    public static void main(String[] args) {
        // Example Usage for SELECT
        QueryBuilder selectQuery = new QueryBuilder()
                .select()
                .openGroup()
                .write(
                        new QueryBuilder()
                                .select("inventory", "SUM(stock)")
                                .where("camp_id = ?")
                                .where("vaccine_id = ?")
                                .where("expiry_date >= CURRENT_DATE")
                )
                .closeGroup()
                .write("-")
                .openGroup()
                .write(
                        new QueryBuilder()
                                .select("appointments", "COUNT(vaccine_id)")
                                .where("camp_id = ?")
                                .where("vaccine_id = ?")
                                .where("status = ?")
                                .where("date_of_vaccination >= CURRENT_DATE")
                )
                .closeGroup("available_stock");

        ColoredOutput.println("cyan", selectQuery.build());
    }
}
