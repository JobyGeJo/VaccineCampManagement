package org.myapplication.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.myapplication.utils.ResponseGenerator;

import java.io.IOException;
import java.io.PrintWriter;

public class ErrorHandlingServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processError(request, response);
    }

    private void processError(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseGenerator responseGenerator = new ResponseGenerator(response);

        // Get error details from the request
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("jakarta.servlet.error.message");

        // Construct JSON string for error details
        responseGenerator.SendStatus(
                statusCode != null ? statusCode : HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                errorMessage
        );

    }
}