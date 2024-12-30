package org.myapplication.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.myapplication.exceptions.AuthenticationFailedException;
import org.myapplication.exceptions.DataBaseException;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.modules.UserModule;
import org.myapplication.models.UserModel;
import org.myapplication.models.JsonModel;
import org.myapplication.tools.ReflectiveUse;
import org.myapplication.tools.ResponseGenerator;

import java.io.IOException;

@ReflectiveUse
public class SessionsController implements Controller {

    public void POST(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonModel jsonModel = new JsonModel(request.getReader());
        ResponseGenerator responseGenerator = new ResponseGenerator(response);

        try {
            UserModel user = UserModule.loginUser(
                    (String) jsonModel.get("user_name"),
                    (String) jsonModel.get("password")
            );

            request.getSession().setAttribute("userId", user.getUserId());
            request.getSession().setAttribute("role", user.getRole());
            System.out.println(request.getSession().getAttribute("role"));
            request.getSession().setMaxInactiveInterval(60 * 60 * 24 * 365);

            responseGenerator.Success("Login Successful", user);
        } catch (DataBaseException | AuthenticationFailedException | InvalidRequestException e) {
            responseGenerator.ExpectationFailed(e.getMessage());
        }
    }

    public void GET(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseGenerator responseGenerator = new ResponseGenerator(response);

        try {
            Integer userId = (Integer) request.getSession().getAttribute("userId");
            UserModel user = UserModule.getUser(userId);
            responseGenerator.Success("Active Session Found", user);

        } catch (DataBaseException | ClassCastException | InvalidRequestException e) {
            responseGenerator.ExpectationFailed(e.getMessage());
        }
    }

    public void DELETE(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseGenerator responseGenerator = new ResponseGenerator(response);

        request.getSession().invalidate();

        responseGenerator.Success("Logged out successfully!");
    }
}
