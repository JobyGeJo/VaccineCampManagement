package org.myapplication.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.myapplication.utils.ResponseGenerator;

import java.io.IOException;

public interface Controller {

    // Default methods for HTTP operations
    default void GET(HttpServletRequest request, HttpServletResponse response) throws IOException {
        sendMethodNotAllowed(response, "GET");
    }

    default void POST(HttpServletRequest request, HttpServletResponse response) throws IOException {
        sendMethodNotAllowed(response, "POST");
    }

    default void PUT(HttpServletRequest request, HttpServletResponse response) throws IOException {
        sendMethodNotAllowed(response, "PUT");
    }

    default void DELETE(HttpServletRequest request, HttpServletResponse response) throws IOException {
        sendMethodNotAllowed(response, "DELETE");
    }

    // Private method to avoid redundancy
    private void sendMethodNotAllowed(HttpServletResponse response, String method) throws IOException {
        ResponseGenerator responseGenerator = new ResponseGenerator(response);
        responseGenerator.NotImplemented(method + " method not supported");
    }
}