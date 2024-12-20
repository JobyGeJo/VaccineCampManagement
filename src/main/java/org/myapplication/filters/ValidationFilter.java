package org.myapplication.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.myapplication.enumerate.Role;
import org.myapplication.utils.ResponseGenerator;

import java.io.IOException;
import java.util.logging.Logger;

public class ValidationFilter implements Filter {

    public static final Logger logger = Logger.getLogger(ValidationFilter.class.getName());

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        // Construct the requested URL string for pattern matching
        String requestURL = httpRequest.getMethod() + httpRequest.getPathInfo();
        logger.info("Request URL: " + requestURL);

        // Check session attributes for user authentication and admin rights
        HttpSession session = httpRequest.getSession(false);  // Use `false` to avoid creating a new session if one doesn't exist
        Integer userId = session != null ? (Integer) session.getAttribute("userId") : null;
        Role isAdmin = session != null ? (Role) session.getAttribute("role") : Role.NULL;

        // Match URL patterns for authentication and authorization checks
        if (requestURL.matches("^POST/(sessions|users)$")) {
            if (userId != null) {
                new ResponseGenerator(httpResponse).ExpectationFailed("Active session found");
                return;
            }
        } else if (requestURL.matches(
                "^(GET/(sessions|users/\\d+/(vaccines/\\d+|appointments)|camps)|" +
                        "POST/users/\\d+/appointments|" +
                        "DELETE/(sessions|users/\\d+/appointments/\\d+)|" +
                        "PUT/users/\\d+)$")
        ) {
            if (userId == null) {
                new ResponseGenerator(httpResponse).ExpectationFailed("Session not found");
                return;
            }
        } else if (requestURL.matches(
                "^(GET/(users|camps(/\\d+/appointments)?)|" +
                        "POST/(camps(/\\d+/vaccines/\\d+)?|vaccines)|" +
                        "PUT/(appointments/\\d+|camps/\\d+))$")) {
            if (Role.NULL.equals(isAdmin)) {
                new ResponseGenerator(httpResponse).ExpectationFailed("Permission denied");
                return;
            }
        } else {
            // Respond with 404 if the URL pattern doesn't match any expected endpoints
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Page not found");
            return;
        }

        // Proceed with the filter chain if no conditions are met
        filterChain.doFilter(servletRequest, servletResponse);
    }
}