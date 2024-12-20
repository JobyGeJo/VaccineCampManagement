package org.myapplication.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.models.JsonModel;
import org.myapplication.modules.VaccineModule;
import org.myapplication.utils.ReflectiveUse;
import org.myapplication.utils.ResponseGenerator;

import java.io.IOException;

@ReflectiveUse
public class VaccinesController implements Controller {

    public void POST(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseGenerator responseGenerator = new ResponseGenerator(response);
        JsonModel jsonModel = new JsonModel(request.getReader());

        try {
            int vaccineId = VaccineModule.registerVaccine(
                    (String) jsonModel.get("vaccine_name"),
                    (Integer) jsonModel.get("dosages", false)
            );

            responseGenerator.Success("Vaccine Created Successfully", vaccineId);

        } catch (InvalidRequestException e) {
            responseGenerator.ExpectationFailed(e.getMessage());
        } catch (ClassCastException e) {
            responseGenerator.BadRequest("Invalid Type Found");
        }
    }

}
