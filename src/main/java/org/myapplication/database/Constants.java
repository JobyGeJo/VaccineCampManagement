package org.myapplication.database;

public enum Constants {
    ALL("*"),
    TBD("?"),
    CURRENT_DATE("CURRENT_DATE");

    private final String name;

    Constants(String symbol) {
        this.name = symbol;
    }

    @Override
    public String toString() {
        return name;
    }
}
