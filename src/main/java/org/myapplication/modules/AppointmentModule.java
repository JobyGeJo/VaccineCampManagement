package org.myapplication.modules;

import org.myapplication.database.*;
import org.myapplication.enumerate.Slot;
import org.myapplication.enumerate.Status;
import org.myapplication.exceptions.DataBaseException;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.models.AppointmentModel;
import org.myapplication.models.UserModel;
import org.myapplication.models.VaccineModel;

import javax.swing.plaf.PanelUI;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

public class AppointmentModule extends Module {

    public static final Table table = new Table("appointments", "a");

    public enum AppointmentColumns implements Columns {
        APPOINTMENT_ID("appointment_id"),
        USER_ID("user_id"),
        VACCINE_ID("vaccine_id"),
        CAMP_ID("camp_id"),
        DATE_OF_VACCINATION("date_of_vaccination"),
        SLOT("slot"),
        STATUS("status"),
        DOSAGE("dosage"),;

        private final Column column;

        AppointmentColumns(String columnName) {
            this.column = new Column(columnName, table);
        }

        @Override
        public Column getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return column.toString();
        }
    }

    enum ForeignKeyJoins implements Joins {
        USERS(UserModule.table, AppointmentColumns.USER_ID, UserModule.UserColumns.USER_ID),
        CAMPS(CampModule.table, AppointmentColumns.CAMP_ID, CampModule.CampColumns.CAMP_ID),
        VACCINES(VaccineModule.table, AppointmentColumns.VACCINE_ID, VaccineModule.VaccineColumns.VACCINE_ID);

        private final Join join;

        ForeignKeyJoins(Table table, Columns refColumn, Columns toColumn) {
            this.join = new Join(table, new Condition(Operator.EQUALS, refColumn.getColumn(), toColumn.getColumn()));
        }

        @Override
        public Join getJoin() {
            return join;
        }
    }

    public static int countSlotAppointments(int campId, Slot slot) {
        return countSlotAppointments(campId, slot, Date.valueOf(LocalDate.now()));
    }

    public static int countSlotAppointments(int campId, Slot slot, Date date) {
        try (DataBaseConnection db = new DataBaseConnection()) {
            return countSlotAppointments(campId, slot, date, db);
        } catch (DataBaseException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public static int countSlotAppointments(int campId, Slot slot, Date date ,DataBaseConnection db) throws DataBaseException {

        Condition status = new Condition(Operator.IN, AppointmentColumns.STATUS.getColumn(), Constants.TBD, Constants.TBD);
        Condition setCampId = new Condition(Operator.EQUALS, AppointmentColumns.CAMP_ID.getColumn(), Constants.TBD);
        Condition setSlot = new Condition(Operator.EQUALS, AppointmentColumns.SLOT.getColumn(), Constants.TBD);
        Condition setDate= new Condition(Operator.EQUALS, AppointmentColumns.DATE_OF_VACCINATION.getColumn(), Constants.TBD);

        Query query = new Query(table);
        query.addColumn(AppointmentColumns.APPOINTMENT_ID.getColumn("count", Functions.COUNT));
        query.addConditions(status, setCampId, setSlot, setDate);
        query.select();

        db.setQuery(
                query,
                Status.PENDING, Status.SUCCESS,
                campId, slot, date
        );

        try (ResultSet rs = db.executeQuery()) {
            if (!rs.next()) {
                throw new InvalidRequestException("Something went wrong");
            }

            return rs.getInt("count");

        } catch (SQLException e) {
            throw new DataBaseException(e);
        }

    }

    @Deprecated
    public static int countAppointments(int campId, Date date) {
        try (DataBaseConnection db = new DataBaseConnection()) {
            return countAppointments(campId, date, db);
        } catch (DataBaseException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    @Deprecated
    public static int countAppointments(int campId, Date date, DataBaseConnection db) throws DataBaseException {

        Condition status = new Condition(Operator.NOT_EQUALS, AppointmentColumns.STATUS.getColumn(), Constants.TBD);
        Condition setCampId = new Condition(Operator.EQUALS, AppointmentColumns.CAMP_ID.getColumn(), Constants.TBD);
        Condition setDate= new Condition(Operator.EQUALS, AppointmentColumns.DATE_OF_VACCINATION.getColumn(), Constants.TBD);

        Query query = new Query(table);
        query.addColumn(AppointmentColumns.SLOT.getColumn("count", Functions.COUNT));
        query.addConditions(status, setCampId, setDate);
        query.addGroupBy(AppointmentColumns.SLOT.getColumn());
        query.select();

        db.setQuery(
                query,
                Status.CANCELLED,
                campId, date
        );

        try (ResultSet rs = db.executeQuery()) {
            if (!rs.next()) {
                throw new InvalidRequestException("Something went wrong");
            }

            return rs.getInt("count");

        } catch (SQLException e) {
            throw new DataBaseException(e);
        }

    }

    public static AppointmentModel bookAppointment(int userId, String date, Slot slot, int campId, int vaccineId) {
        return bookAppointment(userId, Date.valueOf(date), slot, campId, vaccineId);
    }

    public static AppointmentModel bookAppointment(int userId, Date date, Slot slot, int campId, int vaccineId) {

        LocalDateTime appointmentDateTime = LocalDate.parse(date.toString()).atTime(slot.getTime() + 3, 0);

        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("The slot is been closed already");
        } else if (appointmentDateTime.isAfter(LocalDate.now().plusWeeks(1).atStartOfDay())) {
            throw new InvalidRequestException("Booking is only available for a week");
        }

        try (DataBaseConnection db = new DataBaseConnection()) {

            if (countSlotAppointments(campId, slot, date, db) >= 10) {
                throw new InvalidRequestException("Slot unavailable");
            }

            if (InventoryModule.getAvaiableStock(campId, vaccineId, db) <= 0) {
                throw new InvalidRequestException("Stock not available");
            };

            db.setQuery(
                new QueryBuilder()
                    .select(
                        "appointments AS a",
                        "vaccine_name", "location", "status",
                        "(dosages > COUNT(a.appointment_id) OVER (PARTITION BY a.user_id, a.vaccine_id)) AS dosage_eligibility",
                        "(? - a.date_of_vaccination) >= 45 AS vaccine_eligibility",
                        "? >= c.start_date AND (c.end_date IS NULL OR ? <= c.end_date) AS camp_availability"
                    ).join("vaccines AS v", "a.vaccine_id = v.vaccine_id")
                    .join("camps AS c", "a.camp_id = c.camp_id")
                    .and("a.user_id = ?")
                    .and("a.vaccine_id = ?")
                    .and("a.camp_id = ?")
                    .orderby("appointment_id DESC")
                    .limit(1),

                date, date, date,

                userId,
                vaccineId,
                campId
            );

            try (ResultSet rs = db.executeQuery()) {
                if (rs.next()) {
                    Status status = Status.fromString(rs.getString("status"));
                    boolean dosage_eligibility = rs.getBoolean("dosage_eligibility");
                    boolean camp_availability = rs.getBoolean("camp_availability");
                    boolean vaccine_eligibility = rs.getBoolean("vaccine_eligibility");

                    if (Status.CANCELLED.equals(status) || Status.EXPIRED.equals(status)) {

                    } else if (Status.PENDING.equals(status)) {
                        throw new InvalidRequestException("Pending Appointment Found");
                    } else if (!dosage_eligibility) {
                        throw new InvalidRequestException("All dosages are taken");
//                    } else if (!date_eligibility) {
//                        throw new InvalidRequestException("Booking is only available between today and the next 7 days");
                    } else if (!vaccine_eligibility) {
                        throw new InvalidRequestException("45 days gap required between vaccines");
                    } else if (!camp_availability) {
                        throw new InvalidRequestException("Camp isn't available on " + date);
                    }
                }

                db.beginTransaction();

                db.setQuery(
                        new QueryBuilder().insertInto(
                                "appointments",
                                "camp_id", "vaccine_id","slot", "date_of_vaccination", "user_id"
                        ).returning("appointment_id"),

                        campId,
                        vaccineId,
                        slot,
                        date,
                        userId
                );

                AppointmentModel appointment = new AppointmentModel();

                try (ResultSet appointment_id = db.executeQuery()) {
                    if (!appointment_id.next()) {
                        db.rollbackTransaction();
                        throw new InvalidRequestException("Something went wrong");
                    }

                    appointment.setAppointmentId(appointment_id.getInt("appointment_id"));
                }

                db.commitTransaction();

                appointment.setDate(date);
                appointment.setSlot(slot);
                appointment.setStatus(Status.PENDING);

                return appointment;

            }

        } catch (DataBaseException | SQLException e) {
            throw new InvalidRequestException(e.getMessage());
        }

    }

    public static VaccineModel[] getUserAppointments(int userId) {

        try (DataBaseConnection db = new DataBaseConnection()) {

            db.setQuery(
                    new QueryBuilder()
                            .select("appointments", "location", "vaccine_name", "v.vaccine_id AS vaccine_id", "dosage",
                                    "date_of_vaccination", "status", "slot", "dosage.appointment_id AS appointment_id")
                            .join(
                                    "(" + new QueryBuilder()
                                            .select("appointments", "appointment_id",
                                                    "ROW_NUMBER() OVER (PARTITION BY vaccine_id ORDER BY appointment_id DESC) AS row_num")
                                            .where("user_id = ?")
                                            .build() + ") AS dosage",

                                    "dosage.appointment_id = appointments.appointment_id"
                            ).join("camps AS c", "appointments.camp_id = c.camp_id")
                            .join("vaccines AS v", "appointments.vaccine_id = v.vaccine_id")
                            .where("row_num = 1")
                            .or("status = ?")
                            .orderby("vaccine_id, row_num DESC"),

                    userId,
                    Status.SUCCESS
            );


            try (ResultSet rs = db.executeQuery()) {
                String vaccineName;
                String currentVaccineName = "";
                ArrayList<AppointmentModel> dosages = new ArrayList<>();
                ArrayList<VaccineModel> vaccinations = new ArrayList<>();
                VaccineModel vaccination = null;

                while (rs.next()) {
                    vaccineName = rs.getString("vaccine_name");

                    if (!vaccineName.equals(currentVaccineName)) {
                        if (!currentVaccineName.isEmpty()) {
                            vaccination.setAppointments(dosages.toArray(new AppointmentModel[0]));
                            vaccinations.add(vaccination);
                        }

                        currentVaccineName = vaccineName;
                        vaccination = new VaccineModel();
                        vaccination.setVaccineName(currentVaccineName);
                        vaccination.setVaccineId(rs.getInt("vaccine_id"));
                        vaccination.setDosages(rs.getInt("dosage"));
                        dosages = new ArrayList<>();
                    }

                    AppointmentModel appointment = new AppointmentModel(rs);

                    dosages.add(appointment);
                }

                if (vaccination != null) {
                    vaccination.setAppointments(dosages.toArray(new AppointmentModel[0]));
                    vaccinations.add(vaccination);
                }

                return vaccinations.toArray(new VaccineModel[0]);
            }

        } catch (DataBaseException | SQLException e) {
            throw new InvalidRequestException(e.getMessage());
        }

    }

    public static AppointmentModel[] getCampAppointments(int campId) {
        return getCampAppointments(campId, Date.valueOf(LocalDate.now()));
    }

    public static AppointmentModel[] getCampAppointments(int campId, Date date) {
        try (DataBaseConnection db = new DataBaseConnection()) {
            return getCampAppointments(campId, date, db);
        } catch (DataBaseException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public static AppointmentModel[] getCampAppointments(int campId, Date date, DataBaseConnection db) throws DataBaseException {

        Query query = new Query(table);
        query.addColumns(
                AppointmentColumns.APPOINTMENT_ID,
                CampModule.CampColumns.CAMP_NAME,
                UserModule.UserColumns.FIRST_NAME,
                UserModule.UserColumns.LAST_NAME,
                UserModule.UserColumns.AADHAR_NUMBER,
                AppointmentColumns.STATUS,
                AppointmentColumns.SLOT,
                VaccineModule.VaccineColumns.VACCINE_NAME,
                AppointmentColumns.DATE_OF_VACCINATION
        );

        query.addJoins(
                ForeignKeyJoins.VACCINES,
                ForeignKeyJoins.CAMPS,
                ForeignKeyJoins.USERS
        );

        query.addConditions(
                new Condition(Operator.EQUALS, CampModule.CampColumns.CAMP_ID.getColumn(), Constants.TBD),
                new Condition(Operator.EQUALS, AppointmentColumns.DATE_OF_VACCINATION.getColumn(), Constants.TBD),
                new Condition(Operator.NOT_EQUALS, AppointmentColumns.STATUS.getColumn(), Constants.TBD)
        );

        query.select();

        db.setQuery(
                query,

                campId,
                date,
                Status.CANCELLED
        );

        try (ResultSet rs = db.executeQuery()) {
            ArrayList<AppointmentModel> appointments = new ArrayList<>();
            UserModel user;

            while (rs.next()) {
                AppointmentModel appointment = new AppointmentModel(rs);
                appointment.setVaccineName(rs.getString("vaccine_name"));
                user = new UserModel();

                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setAadharNumber(rs.getString("aadhar_number"));

                appointment.setUser(user);

                appointments.add(appointment);
            }

            return appointments.toArray(new AppointmentModel[0]);

        } catch (SQLException e) {
            throw new DataBaseException(e);
        }

    }

    public static void cancelAppointment(int appointmentId) {

        try (DataBaseConnection db = new DataBaseConnection()) {
            db.setQuery(
                    new QueryBuilder()
                            .update("appointments")
                            .set("status")
                            .where("appointment_id = ?")
                            .where("status = ?"),

                    Status.CANCELLED,

                    appointmentId,
                    Status.PENDING
            );

            db.beginTransaction();


            if (db.executeUpdate() != 1) {
                db.rollbackTransaction();
                throw new DataBaseException("Something went wrong");
            }

            db.commitTransaction();


        } catch (DataBaseException e) {
            throw new InvalidRequestException(e.getMessage());
        }

    }

    public static void makeAppointmentSuccess(int appointmentId) {
        try (DataBaseConnection db = new DataBaseConnection()) {
            db.setQuery(
                    new QueryBuilder()
                            .update("appointments")
                            .set("status")
                            .where("appointment_id = ?")
                            .where("status = ?")
                            .returning("camp_id", "vaccine_id", "date_of_vaccination","slot"),

                    Status.SUCCESS,

                    appointmentId,
                    Status.PENDING
            );

            db.beginTransaction();

            try (ResultSet rs = db.executeQuery()) {
                if (!rs.next()) {
                    db.rollbackTransaction();
                    throw new DataBaseException("Pending Appointment Doesn't Exist");
                }

                LocalDateTime appointmentDateTime = LocalDate.parse(
                        rs.getString("date_of_vaccination")
                ).atTime(Slot.fromString(rs.getString("slot")).getTime(), 0);

                if (appointmentDateTime.isAfter(LocalDateTime.now())) {
                    db.rollbackTransaction();
                    throw new DataBaseException("Appointment is not Active");
                }

                Slot.fromString(rs.getString("slot"));

                InventoryModule.useOneVaccine(
                        rs.getInt("camp_id"),
                        rs.getInt("vaccine_id"),
                        db
                ); // Commits the transaction here

            } catch (SQLException e) {
                throw new DataBaseException(e);
            }


        } catch (DataBaseException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(getCampAppointments(2, Date.valueOf("2024-12-19"))));
    }

}
