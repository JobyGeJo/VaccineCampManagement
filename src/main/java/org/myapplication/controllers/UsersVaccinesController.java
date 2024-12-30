package org.myapplication.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.models.VaccineModel;
import org.myapplication.modules.VaccineModule;
import org.myapplication.tools.ReflectiveUse;
import org.myapplication.tools.ResponseGenerator;

import java.io.IOException;

@ReflectiveUse
public class UsersVaccinesController implements Controller {

//    public void GET(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        String[] paths = request.getPathInfo().split("/");
//
//        try {
//            Integer userId = (Integer) request.getSession().getAttribute("userId");
//
//            if (!paths[2].equals(userId + "")) {
//                throw new IllegalArgumentException("Invalid session found");
//            }
//
//            response.setContentType("application/pdf");
//            response.setHeader("Content-Disposition", "inline; filename=\"viewed.pdf\"");
//            VaccineModule.generateCertificate(
//                    userId,
//                    Integer.parseInt(paths[4]),
//                    request,
//                    response.getOutputStream()
//            );
//            response.getOutputStream().flush();
//
//        } catch (IllegalArgumentException | InvalidRequestException e) {
//            new ResponseGenerator(response).ExpectationFailed(e.getMessage());
//        } catch (IndexOutOfBoundsException e) {
//            new ResponseGenerator(response).BadRequest(e.getMessage());
//        }
//    }


    @Override
    public void GET(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] paths = request.getPathInfo().split("/");
        ResponseGenerator responseGenerator = new ResponseGenerator(response);

        try {
            Integer userId = (Integer) request.getSession().getAttribute("userId");

            if (!paths[2].equals(userId + "")) {
                throw new IllegalArgumentException("Invalid session found");
            }

            responseGenerator.Success("Data fetched successfully", VaccineModule.getVaccineDetails(
                    userId,
                    Integer.parseInt(paths[4])
            ).toString());



        } catch (IllegalArgumentException | InvalidRequestException e) {
            new ResponseGenerator(response).ExpectationFailed(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            new ResponseGenerator(response).BadRequest(e.getMessage());
        }
    }
}
