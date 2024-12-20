package org.myapplication.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.modules.AppointmentModule;
import org.myapplication.utils.ReflectiveUse;
import org.myapplication.utils.ResponseGenerator;

import java.io.IOException;

@ReflectiveUse
public class AppointmentsController implements Controller {

    public void PUT(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseGenerator responseGenerator = new ResponseGenerator(response);
        String[] paths = request.getPathInfo().split("/");

        try {
            switch (request.getParameter("action")) {
                case "mark_success":

                    AppointmentModule.makeAppointmentSuccess(
                            Integer.parseInt(paths[2])
                    );
                    responseGenerator.Success("Appointment Mark Successful");
                    break;

                default:
                    responseGenerator.BadRequest("Invalid Action");
            }
        } catch (InvalidRequestException e) {
            responseGenerator.ExpectationFailed(e.getMessage());
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            responseGenerator.BadRequest("Invalid URL");
        }
    }

}
