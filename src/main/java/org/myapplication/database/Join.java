package org.myapplication.database;

public class Join {


    enum JoinType {
        INNER("JOIN"),
        LEFT("LEFT JOIN"),
        RIGHT("RIGHT JOIN"),
        OUTER("OUTER JOIN");

        private final String joinType;

        JoinType(String joinType) {
            this.joinType = joinType;
        }

        @Override
        public String toString() {
            return joinType;
        }
    }

    private final JoinType joinType;
    private final Table table;
    private final Condition condition;
    private String query;

    public Join(Table table, Condition condition) {
        this.joinType = JoinType.INNER;
        this.table = table;
        this.condition = condition;
    }

    Join(JoinType join, Table table, Condition condition) {
        this.joinType = join;
        this.table = table;
        this.condition = condition;
    }

    Join(JoinType join, Table table, Condition ...conditions) {
        this.joinType = join;
        this.table = table;
        if (conditions.length==0) {
            throw new IllegalArgumentException("Condition array is empty");
        }
        this.condition = new Condition(Operator.AND, conditions);
    }

    @Override
    public String toString() {
        return String.format("%s %s ON %s", joinType, table.build(), condition);
    }

    public static void main(String[] args) {
        Table users = new Table("users");
        Column userId = new Column("user_id", users);
        Column name = new Column("user_name", users);

        Table appointments = new Table("appointments", "apt");
        Column appointmentId = new Column("appointment_id", appointments);
        Column userID = new Column("user_id", appointments);

        Condition condition = new Condition(Operator.EQUALS, userId, userID);
        Condition condition2 = new Condition(Operator.GREATER_THAN, appointmentId, 10);
        System.out.println(new Join(JoinType.LEFT, appointments, new Condition(Operator.OR, condition, condition2)).toString());
    }
}
