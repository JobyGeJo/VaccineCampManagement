package org.myapplication.modules;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.myapplication.database.*;
import org.myapplication.enumerate.Status;
import org.myapplication.exceptions.DataBaseException;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.models.AppointmentModel;
import org.myapplication.models.UserModel;
import org.myapplication.models.VaccineModel;
import org.myapplication.tools.CertificateGenerator;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class VaccineModule extends Module {

    public static final Table table = new Table("vaccines", "v");

    enum VaccineColumns implements Columns {
        VACCINE_ID("vaccine_id"),
        VACCINE_NAME("vaccine_name"),
        BOOKED_APPOINTMENT("booked_appointments"),
        DOSAGES("dosages");

        private final Column column;

        VaccineColumns(String columnName) {
            column = new Column(columnName, table);
        }

        @Override
        public Column getColumn() {
            return column;
        }
    }

    public static void generateCertificate(int userId, int vaccinationId, HttpServletRequest request, ServletOutputStream outputStream) {

        try (DataBaseConnection db = new DataBaseConnection()) {
            UserModel user = UserModule.getUser(userId, db);
            VaccineModel vaccination = getVaccineDetails(userId, vaccinationId, db);

            if (vaccination.getAppointments().length == 0) {
                throw new InvalidRequestException("Vaccinations Not Taken");
            }

            try (PDDocument certificate = CertificateGenerator.generatePDF(
                    user, vaccination,
                    request.getServletContext().getRealPath("/WEB-INF/assets/SafeDoseLogo.png")
            )) {
                certificate.save(outputStream);
            }
        } catch (IOException | DataBaseException | SQLException e) {
            throw new InvalidRequestException(e.getMessage());
        }

    }

    public static VaccineModel getVaccineDetails(int userId, int vaccineId) {
        try (DataBaseConnection db = new DataBaseConnection()) {
            return getVaccineDetails(userId, vaccineId, db);
        } catch (DataBaseException | SQLException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public static VaccineModel getVaccineDetails(int userId, int vaccineId, DataBaseConnection db) throws DataBaseException, SQLException {

        VaccineModel vaccine = new VaccineModel();

        Query query = new Query(table);
        query.addColumns(VaccineColumns.VACCINE_NAME, VaccineColumns.DOSAGES);
        query.addCondition(new Condition(Operator.EQUALS, VaccineColumns.VACCINE_ID, Constants.TBD));
        query.select();

        db.setQuery(query, vaccineId);

        try (ResultSet rs = db.executeQuery()) {
            if (!rs.next()) {
                throw new DataBaseException("Vaccine not found");
            }

            vaccine.setVaccineName(rs.getString("vaccine_name"));
            vaccine.setDosages(rs.getInt("dosages"));
            vaccine.setVaccineId(vaccineId);

        }

        Query query2 = new Query(AppointmentModule.table);

        query2.addColumns(
                CampModule.CampColumns.CAMP_NAME,
                AppointmentModule.AppointmentColumns.APPOINTMENT_ID,
                AppointmentModule.AppointmentColumns.SLOT,
                AppointmentModule.AppointmentColumns.DATE_OF_VACCINATION,
                AppointmentModule.AppointmentColumns.STATUS
        );

        query2.addJoin(AppointmentModule.ForeignKeyJoins.CAMPS);

        query2.addConditions(
                new Condition(Operator.EQUALS, AppointmentModule.AppointmentColumns.USER_ID, Constants.TBD),
                new Condition(Operator.EQUALS, AppointmentModule.AppointmentColumns.VACCINE_ID, Constants.TBD),
                new Condition(Operator.EQUALS, AppointmentModule.AppointmentColumns.STATUS, Constants.TBD)
        );

        query2.select();

        db.setQuery(
            query2,

            userId,
            vaccineId,
            Status.SUCCESS
        );

        try (ResultSet rs = db.executeQuery()) {

            ArrayList<AppointmentModel> dosages = new ArrayList<>();

            while (rs.next()) {
                dosages.add(new AppointmentModel(rs));
            }

            vaccine.setAppointments(dosages.toArray(new AppointmentModel[0]));

        }

        return vaccine;
    }

    public static int registerVaccine(String vaccineName, Integer dosages) {

        try (DataBaseConnection db = new DataBaseConnection()) {
            db.setQuery(
                    new QueryBuilder()
                            .insertInto("vaccines",
                                    "vaccine_name", "dosages")
                            .returning("vaccine_id"),

                    vaccineName,
                    (dosages != null) ? dosages : 2
            );

            try (ResultSet vaccineId = db.executeQuery()) {
                if (!vaccineId.next()) {
                    throw new DataBaseException("Vaccine not found");
                }

                return vaccineId.getInt("vaccine_id");

            } catch (SQLException e) {
                throw new DataBaseException(e);
            }

        } catch (DataBaseException e) {
            throw new InvalidRequestException(e.getMessage());
        }

    }

    public static void main(String[] args) {
        System.out.println(getVaccineDetails(4, 1));
    }

}
