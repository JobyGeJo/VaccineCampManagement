package org.myapplication.controllers;

import jakarta.servlet.http.*;
import org.myapplication.enumerate.Role;
import org.myapplication.exceptions.DataBaseException;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.models.JsonModel;
import org.myapplication.models.UserModel;
import org.myapplication.utils.ReflectiveUse;
import org.myapplication.utils.ResponseGenerator;
import org.myapplication.modules.UserModule;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;

@ReflectiveUse
public class UsersController implements Controller {

    public void GET(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseGenerator responseGenerator = new ResponseGenerator(response);

        String type = request.getParameter("type");
        String aadharNumber = request.getParameter("aadharNumber");

        if (aadharNumber != null) {
            responseGenerator.Success("User Fetched ", UserModule.fetchUser(aadharNumber));
        } else if ("admin".equals(type)) {
            responseGenerator.Success("Admins users", Arrays.toString(UserModule.getAdmins()));
        }
    }

    public void POST(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseGenerator responseGenerator = new ResponseGenerator(response);
        JsonModel jsonModel = new JsonModel(request.getReader());

        UserModel userModel = jsonModel.getUser();

        try {
            UserModule.registerUser(
                    userModel,
                    (String) jsonModel.get("password")
            );

            System.out.println(userModel);

            responseGenerator.Success("Registration Successful", "{\"user_id\": " + userModel.getUserId() + '}');

            request.getSession().setAttribute("userId", userModel.getUserId());
            request.getSession().setAttribute("role", Role.NULL);
            request.getSession().setMaxInactiveInterval(60 * 60 * 24 * 30);
        } catch (InvalidRequestException e) {
            responseGenerator.ExpectationFailed(e.getMessage());
        }
    }

    public void PUT(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonModel jsonModel = new JsonModel(request.getReader());
        String[] paths = request.getPathInfo().split("/");
        ResponseGenerator responseGenerator = new ResponseGenerator(response);

        Role role;

        try {
            switch (request.getParameter("action")) {

                case "update_details":
                case "update_user":
                case "change_password":
                case "change_username":
                case "update_date_of_birth":

                    Integer userId = (Integer) request.getSession().getAttribute("userId");

                    if (!(paths[2].equals(userId + ""))) {
                        response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
                        throw new InvalidRequestException("User session not found");
                    }

                    UserModule.updateUser(userId, jsonModel);
                    responseGenerator.Success("Update Successful");
                    break;

                case "promote_user":

                    role = (Role) request.getSession().getAttribute("role");
                    if (Role.NULL.equals(role)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        throw new InvalidRequestException("Only admins can access this");
                    }

                    if (paths.length == 3) {
                        responseGenerator.Success(
                            "Role updated",
                                "{\"role\": \"" + UserModule.upgradeUserRole(
                                        Integer.parseInt(paths[2]),
                                        (Integer) request.getSession().getAttribute("userId"),
                                        (Role) request.getSession().getAttribute("role")
                                ) + "\"}"
                        );
                        break;
                    }

                case "demote_user":

                    role = (Role) request.getSession().getAttribute("role");
                    if (Role.NULL.equals(role)) {
                        throw new AccessDeniedException("Only admin can access this");
                    }

                    if (paths.length == 3) {
                        responseGenerator.Success(
                                "Role updated",
                                "{\"role\": \"" + UserModule.downgradeUserRole(
                                        Integer.parseInt(paths[2]),
                                        (Integer) request.getSession().getAttribute("userId"),
                                        (Role) request.getSession().getAttribute("role")
                                ) + "\"}"
                        );
                        break;
                    }

                default:
                    responseGenerator.BadRequest("Invalid Request");

            }
        } catch (DataBaseException | InvalidRequestException | IllegalArgumentException e) {
            responseGenerator.ExpectationFailed(e.getMessage());
        } catch (AccessDeniedException e) {
            responseGenerator.Forbidden(e.getMessage());
        }
    }
}
