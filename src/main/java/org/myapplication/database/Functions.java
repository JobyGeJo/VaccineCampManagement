package org.myapplication.database;

public enum Functions {
    SUM("SUM"),
    COUNT("COUNT"),;

    private final String delimiter;

    Functions(String name) {
        this.delimiter = name;
    }

    @Override
    public String toString() {
        return delimiter;
    }

}
