package org.myapplication.modules;

import org.myapplication.database.*;

public abstract class Module {

    public interface Columns {

        public Column getColumn();

        public default Column getColumn(String alias) {
            return Column.setAlias(getColumn(), alias);
        }

        public default Column getColumn(String alias, Functions function) {
            return Column.setAliasDelimiter(getColumn(), alias, function);
        }

    }

    public interface Joins {
        public Join getJoin();

        public default Join getJoin(String alias) {
            return new Join(
                    AppointmentModule.table,
                    new Condition(Operator.EQUALS, AppointmentModule.AppointmentColumns.APPOINTMENT_ID.getColumn(),
                    AppointmentModule.AppointmentColumns.CAMP_ID.getColumn()));
        }
    }
}
