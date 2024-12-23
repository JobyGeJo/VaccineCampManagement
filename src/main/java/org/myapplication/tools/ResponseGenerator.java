package org.myapplication.tools;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class ResponseGenerator {

    private PrintWriter out;
    private HttpServletResponse response;

    public ResponseGenerator(HttpServletResponse response) throws IOException {
        this.response = response;
        response.reset();
        this.response.setContentType("application/json");
        out = response.getWriter();
    }

    private void generate(int statusCode, String message) {
        out.println("{");
        out.println("\"status\" : " + statusCode);
        out.println(", \"message\": \"" + message + '\"');
        out.println("}");
    }

    public void SendStatus(int statusCode, String message){
        response.setStatus(statusCode);
        generate(statusCode, message);
    }

    public void Success(String message, Object data){
        response.setStatus(HttpServletResponse.SC_OK);
        out.println("{");
        out.println("\"status\" : " + HttpServletResponse.SC_OK);
        out.println(", \"message\": \"" + message + '\"');
        out.println(", \"data\" : " + data);
        out.println("}");
    }

    public void Success(String message){
        SendStatus(HttpServletResponse.SC_OK, message);
    }

    public void BadRequest(String message){
        SendStatus(HttpServletResponse.SC_BAD_REQUEST, message);
    }

    public void Unauthorized(String message){
        SendStatus(HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    public void Forbidden(String message){
        SendStatus(HttpServletResponse.SC_FORBIDDEN, message);
    }

    public void ExpectationFailed(String message){
        SendStatus(HttpServletResponse.SC_EXPECTATION_FAILED, message);
    }

    public void NotFound(String message){
        SendStatus(HttpServletResponse.SC_NOT_FOUND, message);
    }

    public void InternalServerError(String message){
        SendStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
    }

    public void NotImplemented(String message){
        SendStatus(HttpServletResponse.SC_NOT_IMPLEMENTED, message);
    }

}
