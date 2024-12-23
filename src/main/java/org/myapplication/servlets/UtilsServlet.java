package org.myapplication.servlets;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.modules.VaccineModule;
import org.myapplication.tools.ColoredOutput;
import org.myapplication.tools.ResponseGenerator;

import java.io.Console;
import java.io.IOException;

public class UtilsServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        ColoredOutput.println("black", "Here");

        try {
            HttpSession session = request.getSession(false);
            Integer userId = session != null ? (Integer) session.getAttribute("userId") : null;
            int vaccineId = Integer.parseInt(request.getParameter("vaccine_id"));

            if (userId == null) {
                throw new InvalidRequestException("Session has no userId.");
            }

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"viewed.pdf\"");
            VaccineModule.generateCertificate(
                    userId,
                    vaccineId,
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
