package org.myapplication.database;

public class Column {

    private final Table table;
    private final String columnName; // Actual column name
    private String alias;            // Alias for the column, if provided
    private Functions delimiter;

    // Constructor without alias
    public Column(String columnName, Table table) {
        this.table = table;
        this.columnName = columnName;
    }

    // Constructor with alias
    public Column(String columnName, Table table, String alias) {
        this.table = table;
        this.columnName = columnName;
        this.alias = alias;
    }

    // Constructor with alias and delimiter
    public Column(String columnName, Table table, String alias, Functions delimiter) {
        this.table = table;
        this.columnName = columnName;
        this.alias = alias;
        this.delimiter = delimiter;
    }

    // Returns full column reference: tablename.columnname
    public String getFullyQualifiedName() {
        return String.format("%s.%s", table, columnName);
    }

    // Returns only the column name
    public String getColumnName() {
        return this.columnName;
    }

    // Returns the alias if present, otherwise the fully qualified name
    @Override
    public String toString() {
        return alias != null ? alias : getFullyQualifiedName();
    }

    // Build method to handle different SQL representations
    public String build() {
        if (delimiter != null && alias != null) {
            return String.format("%s(%s.%s) AS %s", delimiter, table, columnName, alias);
        } else if (delimiter != null) {
            return String.format("%s(%s.%s)", delimiter, table, columnName);
        } else if (alias != null) {
            return String.format("%s.%s AS %s", table, columnName, alias);
        } else {
            return getFullyQualifiedName();
        }
    }

    // Returns the alias if it exists
    public String getAlias() {
        return alias;
    }

    // Static method to set alias
    public static Column setAlias(Column column, String alias) {
        return new Column(column.columnName, column.table, alias, column.delimiter);
    }

    // Static method to set alias and delimiter
    public static Column setAliasDelimiter(Column column, String alias, Functions delimiter) {
        return new Column(column.columnName, column.table, alias, delimiter);
    }
}