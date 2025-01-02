package org.myapplication.modules;

import org.myapplication.database.DataBaseConnection;
import org.myapplication.database.QueryBuilder;
import org.myapplication.enumerate.Slot;
import org.myapplication.enumerate.Status;
import org.myapplication.exceptions.DataBaseException;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.models.CampModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

public class CampModule {

    public enum Column {
        CAMP_ID("camp_id"),
        LOCATION("location"),
        START_DATE("start_date"),
        END_DATE("end_date"),;

        private final String displayName;

        Column(String displayName) {
            String tableName = "camps";
            this.displayName = String.format("%s.%s", tableName, displayName);
        }

        public static Column fromString(String columnName) {
            if (columnName == null || columnName.equals("null")) {
                return CAMP_ID;
            }
            for (Column column : Column.values()) {
                if (column.name().equalsIgnoreCase(columnName)) {
                    return column;
                }
            }
            throw new IllegalArgumentException("No enum constant for " + columnName);
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static void registerCamp(CampModel camp) {
        try (DataBaseConnection db = new DataBaseConnection()) {
            db.setQuery(
                    new QueryBuilder().insertInto(
                            "camps",
                            "location", "start_date", "end_date"
                    ).returning("camp_id"),

                    camp.getLocation(),
                    camp.getStartDate(),
                    camp.getEndDate()
            );

            try (ResultSet rs = db.executeQuery()){
                if (!rs.next()) {
                    throw new DataBaseException("Something went wrong");
                }

                camp.setCampId(rs.getInt("camp_id"));
            }


        } catch (DataBaseException e) {
            throw new InvalidRequestException(e.getMessage());
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // SQLState code for unique violation
                throw new InvalidRequestException(
                        "The given camp_name is already in use"
                );
            }
        }
    }

    public static CampModel[] getCamps(String sortBy, boolean reverse) {
        try {
            return getCamps(Column.fromString(sortBy), reverse);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(String.format("Invalid column name %s", sortBy));
        }
    }

    public static CampModel[] getCamps(Column sortBy, boolean reverse) {

        ArrayList<CampModel> camps = new ArrayList<>();

        try (DataBaseConnection db = new DataBaseConnection()) {
            db.setQuery(
                    new QueryBuilder()
                            .select(
                                    "camps",
                                    "camps.*",
                                    "COALESCE(inv.total_stock, 0) AS stock",
                                    "COUNT(appointments.appointment_id) AS count"
                            ).write("LEFT JOIN ")
                            .openGroup()
                            .select("inventory", "camp_id", "SUM(stock) AS total_stock")
                            .where("expiry_date >= CURRENT_DATE")
                            .groupby("camp_id")
                            .closeGroup("inv")
                            .write("ON inv.camp_id = camps.camp_id ")
                            .leftJoin("appointments",
                                    "appointments.camp_id = camps.camp_id",
                                    "appointments.date_of_vaccination = CURRENT_DATE",
                                    "appointments.status != ?"
                            ).groupby("camps.camp_id", "inv.total_stock")
                            .orderby(sortBy.toString(), reverse),

                    Status.CANCELLED
            );

            try (ResultSet rs = db.executeQuery()) {

                while (rs.next()) {
                    CampModel camp = new CampModel();

                    camp.setCampId(rs.getInt("camp_id"));
                    camp.setLocation(rs.getString("location"));
                    camp.setStartDate(rs.getDate("start_date"));
                    camp.setEndDate(rs.getDate("end_date"));

                    camp.setTotolStock(rs.getInt("stock"));
                    camp.setAppointmentCount(rs.getInt("count"));

                    camps.add(camp);
                }

            }

            return camps.toArray(new CampModel[0]);

        } catch (SQLException | DataBaseException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static CampModel[] getCamps(String contains) {
        return getCamps(contains, Column.CAMP_ID, false);
    }

    public static CampModel[] getCamps(String contains, String sortBy, boolean reverse) {
        return getCamps(contains, Column.fromString(sortBy), false);
    }

    public static CampModel[] getCamps(String contains, Column sortBy, boolean reverse) {
        contains = contains.toLowerCase();
        ArrayList<CampModel> camps = new ArrayList<>();

        try (DataBaseConnection db = new DataBaseConnection()) {
            db.setQuery(
                    new QueryBuilder()
                            .select(
                                    "camps",
                                    "camps.*",
                                    "COALESCE(inv.total_stock, 0) AS stock",
                                    "COUNT(appointments.appointment_id) AS count"
                            ).write("LEFT JOIN ")
                            .openGroup().write(
                                    new QueryBuilder()
                                            .select("inventory", "camp_id", "SUM(stock) AS total_stock")
                                            .where("expiry_date >= CURRENT_DATE")
                                            .groupby("camp_id")
                            ).closeGroup("inv")
                            .write("ON inv.camp_id = camps.camp_id ")
                            .leftJoin("appointments",
                                    "appointments.camp_id = camps.camp_id",
                                    "appointments.date_of_vaccination = CURRENT_DATE",
                                    "appointments.status != ?"
                            )
                            .and("location LIKE ?")
                            .groupby("camps.camp_id", "inv.total_stock")
                            .orderby(sortBy.toString(), reverse),

                    Status.CANCELLED,
                    '%' + contains + '%'
            );

            try (ResultSet rs = db.executeQuery()) {

                while (rs.next()) {
                    CampModel camp = new CampModel();

                    camp.setCampId(rs.getInt("camp_id"));
                    camp.setLocation(rs.getString("location"));
                    camp.setStartDate(rs.getDate("start_date"));
                    camp.setEndDate(rs.getDate("end_date"));

                    camp.setTotolStock(rs.getInt("stock"));
                    camp.setAppointmentCount(rs.getInt("count"));

                    camps.add(camp);
                }

            }

            return camps.toArray(new CampModel[0]);

        } catch (SQLException | DataBaseException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static void closeSlot(int campId, Slot slot) {

        if (LocalTime.now().getHour() <= slot.getTime() + 3) {
            throw new InvalidRequestException("Camp can't be closed now");
        }

        try (DataBaseConnection db = new DataBaseConnection()) {

            db.setQuery(
                    new QueryBuilder()
                            .update("appointments")
                            .set("status")
                            .where("camp_id = ?")
                            .and("slot = ?")
                            .and("status = ?")
                            .and("date_of_vaccination = CURRENT_DATE"),

                    Status.EXPIRED,
                    campId, slot,
                    Status.PENDING
            );

            db.beginTransaction();

            if (db.executeUpdate() >= 10) {
                db.rollbackTransaction();
                throw new InvalidRequestException("Something went wrong");
            }

            db.commitTransaction();

        } catch (DataBaseException e) {
            throw new InvalidRequestException(e.getMessage());
        }

    }

    public static void main (String[] args) {
//        System.out.println(Arrays.toString(getCamps("a", Column.LOCATION, true)));
    }

}
