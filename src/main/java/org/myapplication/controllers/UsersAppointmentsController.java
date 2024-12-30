package org.myapplication.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.myapplication.enumerate.Slot;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.models.AppointmentModel;
import org.myapplication.models.JsonModel;
import org.myapplication.modules.AppointmentModule;
import org.myapplication.tools.ReflectiveUse;
import org.myapplication.tools.ResponseGenerator;

import java.io.IOException;
import java.util.Arrays;

@ReflectiveUse
public class UsersAppointmentsController implements Controller {

    public void POST(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseGenerator responseGenerator = new ResponseGenerator(response);
        String[] paths = request.getPathInfo().split("/");
        JsonModel jsonModel = new JsonModel(request.getReader());

        Integer userId = (Integer) request.getSession().getAttribute("userId");

        try {
            if (!paths[2].equals(userId.toString())) {
                throw new InvalidRequestException("Invalid Session Found");
            }

            AppointmentModel appointment = AppointmentModule.bookAppointment(
                    userId,
                    (String) jsonModel.get("date"),
                    Slot.fromCode((int) jsonModel.get("slot")),
                    (int) jsonModel.get("camp_id"),
                    (int) jsonModel.get("vaccine_id")
            );

            responseGenerator.Success("Booking Successful", appointment);

        } catch (InvalidRequestException e) {
            responseGenerator.ExpectationFailed(e.getMessage());
        }
    }

    public void GET(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseGenerator responseGenerator = new ResponseGenerator(response);
        String[] paths = request.getPathInfo().split("/");

        Integer userId = (Integer) request.getSession().getAttribute("userId");

        try {
            if (!paths[2].equals(userId.toString())) {
                throw new InvalidRequestException("Invalid Session Found");
            }

            responseGenerator.Success(
                    "Appointments Fetched",
                    Arrays.toString(AppointmentModule.getUserAppointments(userId))
            );

        } catch (InvalidRequestException | IndexOutOfBoundsException e) {
            responseGenerator.ExpectationFailed(e.getMessage());
        }
    }



    public void DELETE(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseGenerator responseGenerator = new ResponseGenerator(response);
        String[] paths = request.getPathInfo().split("/");

        Integer userId = (Integer) request.getSession().getAttribute("userId");

        try {
            if (!paths[2].equals(userId.toString())) {
                throw new InvalidRequestException("Invalid Session Found");
            }

            AppointmentModule.cancelAppointment(Integer.parseInt(paths[4]));
            responseGenerator.Success("Appointment Cancelled Successfully");

        } catch (InvalidRequestException e) {
            responseGenerator.ExpectationFailed(e.getMessage());
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            responseGenerator.BadRequest("Invalid Request");
        }
    }

}
