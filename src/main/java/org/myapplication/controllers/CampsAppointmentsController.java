package org.myapplication.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.modules.AppointmentModule;
import org.myapplication.utils.ReflectiveUse;
import org.myapplication.utils.ResponseGenerator;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;

@ReflectiveUse
public class CampsAppointmentsController implements Controller {

    public void GET(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseGenerator responseGenerator = new ResponseGenerator(response);
        String[] paths = request.getPathInfo().split("/");

        String d = request.getParameter("date");
        Date date;
        if (d != null) {
            date = Date.valueOf(d);
        } else {
            date = Date.valueOf(LocalDate.now());
        }

        try {
            responseGenerator.Success(
                "Appointments Fetched",
                Arrays.toString(AppointmentModule.getCampAppointments(
                    Integer.parseInt(paths[2]),
                        date
                ))
            );

        } catch (InvalidRequestException e) {
            responseGenerator.ExpectationFailed(e.getMessage());
        } catch (NumberFormatException e) {
            responseGenerator.BadRequest("Invalid URL");
        }
    }

}
