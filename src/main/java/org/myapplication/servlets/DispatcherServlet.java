package org.myapplication.servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.myapplication.utils.ResponseGenerator;

import java.io.*;
import java.lang.reflect.*;

public class DispatcherServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response, request.getMethod());
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response, String httpMethod) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/"); //['', 'users', '2', 'appointments]

        if (parts.length < 2) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }

        StringBuilder controllerName = new StringBuilder();

        for (int i=1; i<parts.length; i+=2) {
            controllerName.append(parts[i].toUpperCase().charAt(0)).append(parts[i].substring(1));
        }

        controllerName.append("Controller"); // org.myapplication.controllers.UsersAppointmentsController

        try {
            Class<?> controllerClass = Class.forName("org.myapplication.controllers." + controllerName);
            Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance(); // Create an instance
            Method method = controllerClass.getMethod(httpMethod, HttpServletRequest.class, HttpServletResponse.class);
            method.invoke(controllerInstance, request, response); // Use the instance for method invocation
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            new ResponseGenerator(response).NotFound(httpMethod + pathInfo + " api not found");
        } catch (Exception e) {
            new ResponseGenerator(response).InternalServerError(e + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}