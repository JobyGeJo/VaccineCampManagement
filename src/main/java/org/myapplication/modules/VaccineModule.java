package org.myapplication.modules;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.myapplication.database.DataBaseConnection;
import org.myapplication.database.QueryBuilder;
import org.myapplication.enumerate.Status;
import org.myapplication.exceptions.DataBaseException;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.models.AppointmentModel;
import org.myapplication.models.UserModel;
import org.myapplication.models.VaccineModel;
import org.myapplication.utils.CertificateGenerator;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class VaccineModule {

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

    public static VaccineModel getVaccineDetails(int userId, int vaccineId, DataBaseConnection db) throws DataBaseException, SQLException {

        VaccineModel vaccine = new VaccineModel();

        db.setQuery(
                new QueryBuilder()
                        .select("vaccines", "vaccine_name", "dosages")
                        .where("vaccine_id = ?"),

                vaccineId
        );

        try (ResultSet rs = db.executeQuery()) {
            if (!rs.next()) {
                throw new DataBaseException("Vaccine not found");
            }

            vaccine.setVaccineName(rs.getString("vaccine_name"));
            vaccine.setDosages(rs.getInt("dosages"));
            vaccine.setVaccineId(vaccineId);

        }

        db.setQuery(
            new QueryBuilder()
                .select("appointments",
                        "location", "appointment_id", "slot",
                        "date_of_vaccination", "status")
                .join("camps c", "c.camp_id = appointments.camp_id")
                .where("user_id = ?")
                .where("vaccine_id = ?")
                .where("status = ?"),

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

}
