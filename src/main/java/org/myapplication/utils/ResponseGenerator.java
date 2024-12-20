package org.myapplication.utils;

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

    public void Success(String message, Object data){
        response.setStatus(HttpServletResponse.SC_OK);
        out.println("{");
        out.println("\"status\" : " + HttpServletResponse.SC_OK);
        out.println(", \"message\": \"" + message + '\"');
        out.println(", \"data\" : " + data);
        out.println("}");
    }

    public void Success(String message){
        response.setStatus(HttpServletResponse.SC_OK);
        out.println("{");
        out.println("\"status\" : " + HttpServletResponse.SC_OK);
        out.println(", \"message\": \"" + message + '\"');
        out.println("}");
    }

    public void BadRequest(String message){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.println("{");
        out.println("\"status\" : " + HttpServletResponse.SC_BAD_REQUEST);
        out.println(", \"message\": \"" + message + '\"');
        out.println("}");
    }

    public void Unauthorized(String message){
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        out.println("{");
        out.println("\"status\" : " + HttpServletResponse.SC_UNAUTHORIZED);
        out.println(", \"message\": \"" + message + '\"');
        out.println("}");
    }

    public void Forbidden(String message){
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        out.println("{");
        out.println("\"status\" : " + HttpServletResponse.SC_FORBIDDEN);
        out.println(", \"message\": \"" + message + '\"');
        out.println("}");
    }

    public void ExpectationFailed(String message){
        response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
        out.println("{");
        out.println("\"status\" : " + HttpServletResponse.SC_EXPECTATION_FAILED);
        out.println(", \"message\": \"" + message + '\"');
        out.println("}");
    }

    public void NotFound(String message){
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        out.println("{");
        out.println("\"status\" : " + HttpServletResponse.SC_NOT_FOUND);
        out.println(", \"message\": \"" + message + '\"');
        out.println("}");
    }

    public void InternalServerError(String message){
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        out.println("{");
        out.println("\"status\" : " + HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        out.println(", \"message\": \"" + message + '\"');
        out.println("}");
    }

    public void NotImplemented(String message){
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        out.println("{");
        out.println("\"status\" : " + HttpServletResponse.SC_NOT_IMPLEMENTED);
        out.println(", \"message\": \"" + message + '\"');
        out.println("}");
    }

    public void SendStatus(int statusCode, String message){
        switch(statusCode){
            case HttpServletResponse.SC_OK:
                Success(message);
                break;

            case HttpServletResponse.SC_UNAUTHORIZED:
                Unauthorized(message);
                break;

            case HttpServletResponse.SC_FORBIDDEN:
                Forbidden(message);
                break;

            case HttpServletResponse.SC_EXPECTATION_FAILED:
                ExpectationFailed(message);
                break;

            case HttpServletResponse.SC_NOT_FOUND:
                NotFound(message);
                break;

            case HttpServletResponse.SC_INTERNAL_SERVER_ERROR:
                InternalServerError(message);
                break;

            case HttpServletResponse.SC_NOT_IMPLEMENTED:
                NotImplemented(message);
                break;

            case HttpServletResponse.SC_BAD_REQUEST:
                BadRequest(message);
                break;

            default:
                response.setStatus(statusCode);
                out.println("{");
                out.println("\"status\" : " + statusCode);
                out.println(", \"message\": \"" + message + '\"');
                out.println("}");

        }
    }
}
