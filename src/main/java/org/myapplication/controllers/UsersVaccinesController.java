package org.myapplication.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.modules.VaccineModule;
import org.myapplication.utils.ResponseGenerator;

import java.io.IOException;

public class UsersVaccinesController implements Controller {

    public void GET(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] paths = request.getPathInfo().split("/");

        try {
            Integer userId = (Integer) request.getSession().getAttribute("userId");

            if (!paths[2].equals(userId + "")) {
                throw new IllegalArgumentException("Invalid session found");
            }

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"viewed.pdf\"");
            VaccineModule.generateCertificate(
                    userId,
                    Integer.parseInt(paths[4]),
                    request,
                    response.getOutputStream()
            );
            response.getOutputStream().flush();

        } catch (IllegalArgumentException | InvalidRequestException e) {
            new ResponseGenerator(response).ExpectationFailed(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            new ResponseGenerator(response).BadRequest(e.getMessage());
        }
    }

}
