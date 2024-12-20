package org.myapplication.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.myapplication.enumerate.Slot;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.models.CampModel;
import org.myapplication.models.JsonModel;
import org.myapplication.modules.CampModule;
import org.myapplication.utils.ReflectiveUse;
import org.myapplication.utils.ResponseGenerator;

import java.io.IOException;
import java.util.Arrays;

@ReflectiveUse
public class CampsController implements Controller {

    public void POST(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonModel jsonModel = new JsonModel(request.getReader());
        ResponseGenerator responseGenerator = new ResponseGenerator(response);

        CampModel campModel = new CampModel();

        try {
            campModel.setLocation((String) jsonModel.get("location"));
            campModel.setStartDate((String) jsonModel.get("start_date"));
            campModel.setEndDate((String) jsonModel.get("end_date", false));

            CampModule.registerCamp(campModel);

            responseGenerator.Success("Registration Successful", campModel.getCampId());
        } catch (InvalidRequestException e) {
            responseGenerator.ExpectationFailed(e.getMessage());
        }
    }

    public void GET(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseGenerator responseGenerator = new ResponseGenerator(response);

        responseGenerator.Success("Data Fetched", Arrays.toString(CampModule.getCamps()));
    }

    public void PUT(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonModel jsonModel = new JsonModel(request.getReader());
        ResponseGenerator responseGenerator = new ResponseGenerator(response);
        String[] paths = request.getPathInfo().split("/");

        try {
            switch (request.getParameter("action")) {
                case "update":
                    responseGenerator.NotImplemented("API is not implemented");

                case "close_slot":
                    CampModule.closeSlot(
                            Integer.parseInt((paths[2])),
                            Slot.fromCode((int) jsonModel.get("slot"))
                    );

                    responseGenerator.Success("Slot Closed Successfully");
                    break;

                default:
                    responseGenerator.BadRequest("Invalid action");
            }
        } catch (InvalidRequestException e) {
            responseGenerator.ExpectationFailed(e.getMessage());
        } catch (NumberFormatException | ClassCastException | IndexOutOfBoundsException e) {
            responseGenerator.BadRequest("Invalid type or URL");
        }
    }

}
