package org.myapplication.modules;

import org.myapplication.database.DataBaseConnection;
import org.myapplication.database.QueryBuilder;
import org.myapplication.enumerate.Slot;
import org.myapplication.enumerate.Status;
import org.myapplication.exceptions.DataBaseException;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.models.CampModel;
import org.myapplication.models.VaccineModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public class CampModule {

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

    public static CampModel[] getCamps() {
        ArrayList<CampModel> camps = new ArrayList<>();

        try (DataBaseConnection db = new DataBaseConnection()) {

            db.setQuery(new QueryBuilder()
                    .write("SELECT\n" +
                            "    camps.*,\n" +
                            "    COALESCE(inv.total_stock, 0) AS stock,\n" +
                            "    COUNT(appointments.appointment_id) AS count\n" +
                            "FROM safedose_v2.camps\n" +
                            "         LEFT JOIN (\n" +
                            "    SELECT\n" +
                            "        camp_id,\n" +
                            "        SUM(stock) AS total_stock\n" +
                            "    FROM safedose_v2.inventory\n" +
                            "    WHERE expiry_date >= CURRENT_DATE\n" +
                            "    GROUP BY camp_id\n" +
                            ") inv ON inv.camp_id = camps.camp_id\n" +
                            "         LEFT JOIN safedose_v2.appointments\n" +
                            "                   ON appointments.camp_id = camps.camp_id\n" +
                            "                       AND appointments.date_of_vaccination = CURRENT_DATE\n" +
                            "                       AND appointments.status != 'Cancelled'\n" +
                            "GROUP BY camps.camp_id, inv.total_stock\n" +
                            "ORDER BY camps.camp_id;")
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

}
