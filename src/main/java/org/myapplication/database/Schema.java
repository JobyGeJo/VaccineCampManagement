package org.myapplication.database;

public class Schema {

    private final String schemaName;

    Schema(String schemaName) {
        this.schemaName = schemaName;
    }

    @Override
    public String toString() {
        return schemaName;
    }

}
