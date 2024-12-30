package org.myapplication.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.models.JsonModel;
import org.myapplication.modules.InventoryModule;
import org.myapplication.tools.ReflectiveUse;
import org.myapplication.tools.ResponseGenerator;

import java.io.IOException;

@ReflectiveUse
public class CampsVaccinesController implements Controller {

    public void POST(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseGenerator responseGenerator = new ResponseGenerator(response);
        JsonModel jsonModel = new JsonModel(request.getReader());

        String[] paths = request.getPathInfo().split("/");

        try {
            InventoryModule.addStock(
                    Integer.parseInt(paths[2]),
                    Integer.parseInt(paths[4]),
                    (int) jsonModel.get("stock"),
                    (String) jsonModel.get("expiry_date")
            );

            responseGenerator.Success("Stocked added Successfully");
        } catch (InvalidRequestException e) {
            responseGenerator.ExpectationFailed(e.getMessage());
        } catch (NumberFormatException | ClassCastException | IndexOutOfBoundsException e) {
            responseGenerator.BadRequest("Invalid URL or Data");
        }

    }

}
