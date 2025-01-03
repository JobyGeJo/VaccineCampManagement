package org.myapplication.database;

public enum Operator {

    EQUALS("="),
    NOT_EQUALS("<>"),
    GREATER_THAN(">"),
    LESSER_THAN("<"),
    GREATER_THAN_EQUALS(">="),
    LESSER_THAN_EQUALS("<="),
    LIKE("LIKE"),
    NOT_LIKE("NOT LIKE"),
    BETWEEN("BETWEEN"),
    NOT_BETWEEN("NOT BETWEEN"),
    IN("IN"),
    NOT_IN("NOT IN"),

    AND("AND"),
    OR("OR"),
    NOT("NOT"),
    IS_NULL("IS NULL"),
    IS_NOT_NULL("IS NOT NULL");


    private final String operator;

    Operator(String operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return operator;
    }

}
