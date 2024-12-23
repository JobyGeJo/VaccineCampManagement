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
public class SuppliesController implements Controller {

    @Override
    public void POST(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseGenerator responseGenerator = new ResponseGenerator(response);
        JsonModel jsonModel = new JsonModel(request.getReader());

        try {
            InventoryModule.addStock(
                    (int) jsonModel.get("camp_id"),
                    (int) jsonModel.get("vaccine_id"),
                    (int) jsonModel.get("stock", false),
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
