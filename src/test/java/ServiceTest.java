package org.myapplication.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceTest {

    private DispatcherServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        servlet = new DispatcherServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
    }

    @Test
    void testDoPost_ReturnsCorrectJsonResponse() throws Exception {
        // Mock JSON input
        String jsonInput = "{\"user_name\": \"joby\", \"password\": \"12345678\"}";

        // Mock request and response
        BufferedReader bufferedReader = new BufferedReader(new StringReader(jsonInput));
        when(request.getReader()).thenReturn(bufferedReader);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Simulate POST request
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/sessions");
        when(request.getSession()).thenReturn(session);

        // Call the service method
        servlet.service(request, response);

        // Verify response status
        verify(response).setStatus(HttpServletResponse.SC_OK);


        // Use a JSON library to compare objects instead of raw strings
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(stringWriter.toString(), JsonObject.class);

        // Validate top-level fields
        assertTrue(jsonObject.get("status").isJsonPrimitive() && jsonObject.get("status").getAsJsonPrimitive().isNumber(), "Status should be a number");
        assertTrue(jsonObject.get("message").isJsonPrimitive() && jsonObject.get("message").getAsJsonPrimitive().isString(), "Message should be a string");

        JsonObject data = jsonObject.getAsJsonObject("data");

        // Validate fields within "data"
        assertTrue(data.get("user_id").isJsonPrimitive() && data.get("user_id").getAsJsonPrimitive().isNumber(), "User ID should be a number");
        assertTrue(data.get("user_name").isJsonPrimitive() && data.get("user_name").getAsJsonPrimitive().isString(), "User Name should be a string");
        assertTrue(data.get("full_name").isJsonPrimitive() && data.get("full_name").getAsJsonPrimitive().isString(), "Full Name should be a string");

        // Test date field format
        String dob = data.get("date_of_birth").getAsString();
        assertTrue(data.get("aadhar_number").isJsonPrimitive() && data.get("aadhar_number").getAsJsonPrimitive().isString(), "Aadhar Number should be a string");
        assertTrue(data.get("phone_number").isJsonPrimitive() && data.get("phone_number").getAsJsonPrimitive().isString(), "Phone Number should be a string");
        assertTrue(dob.matches("\\d{4}-\\d{2}-\\d{2}"), "Date of Birth should match YYYY-MM-DD");

        assertTrue(data.get("role").isJsonPrimitive() && data.get("role").getAsJsonPrimitive().isString(), "Role should be a string");
    }
}
