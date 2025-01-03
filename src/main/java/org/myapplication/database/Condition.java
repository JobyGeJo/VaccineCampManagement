package org.myapplication.database;

import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Condition {

    private final String condition;

    // Constructor for binary operations
    public Condition(Operator operator, Column self, Object ...others) {
        switch (operator) {
            case BETWEEN:
            case NOT_BETWEEN:
                if (others.length != 2) {
                    throw new IllegalArgumentException("Need exactly two arguments");
                }

                this.condition = String.format(
                        "%s %s %s AND %s",
                        self.getColumnName(),
                        operator,
                        formatValue(others[0]),
                        formatValue(others[1])
                );
                break;

            case IN:
            case NOT_IN:
                String inClause = Arrays.stream(others)
                        .map(this::formatValue)
                        .collect(Collectors.joining(", "));
                this.condition = String.format("%s %s (%s)", self.getFullyQualifiedName(), operator, inClause);
                break;

            case EQUALS:
            case NOT_EQUALS:
            case GREATER_THAN:
            case LESSER_THAN:
            case GREATER_THAN_EQUALS:
            case LESSER_THAN_EQUALS:
            case LIKE:
            case NOT_LIKE:
                if (others.length != 1) {
                    throw new IllegalArgumentException("Need exactly one arguments for operator " + operator);
                }
                this.condition = String.format("%s %s %s", self.getFullyQualifiedName(), operator, formatValue(others[0]));
                break;

            case IS_NULL:
            case IS_NOT_NULL:
                if (others.length != 0) {
                    throw new IllegalArgumentException("Need exactly no arguments for operator " + operator);
                }
                this.condition = String.format("%s %s", self.getFullyQualifiedName(), operator);
                break;

            default:
                throw new IllegalArgumentException("Illegal operator: " + operator);

        }

    }

    public Condition(Operator operator, Condition ...conditions) {
        switch (operator) {
            case AND:
            case OR:
                this.condition = Arrays.stream(conditions)
                        .map(Condition::toString)
                        .collect(Collectors.joining(String.format(" %s ", operator)));
                break;

            case NOT:
                if (conditions.length > 1) {
                    throw new IllegalArgumentException("More than one condition for operator: " + operator);
                }
                this.condition = String.format("%s %s", operator, conditions[0]);
                break;

            default:
                throw new IllegalArgumentException("Illegal operator: " + operator);
        }
    }

    @Override
    public String toString() {
        return condition;
    }

    // Helper method to format values
    private String formatValue(Object value) {
        if (value instanceof String) {
            return String.format("'%s'", value);
        } else if (value == null) {
            return "?";
        }
        return value.toString();
    }

    public static void main(String [] args){
        System.out.println(
            new Condition(
                Operator.BETWEEN,
                new Column("user_id", new Table("users", new Schema("safedose_v2"), "user")),
                1, 5
            )
        );
    }
}
