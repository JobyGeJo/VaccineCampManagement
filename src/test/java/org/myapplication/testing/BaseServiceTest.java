package org.myapplication.testing;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static org.mockito.Mockito.*;

public abstract class BaseServiceTest {
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected Gson payload;
    protected StringWriter responseWriter;
    protected HttpSession session;

    protected void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        payload = new Gson();
        responseWriter = new StringWriter();
    }

    protected void setUpRequest(String method, String endpoint, String payload, Map<String, Object> queryParams) throws Exception {
        when(request.getMethod()).thenReturn(method);
        when(request.getPathInfo()).thenReturn(endpoint);
        if (payload != null) {
            when(request.getReader()).thenReturn(new java.io.BufferedReader(new java.io.StringReader(payload)));
        }

        PrintWriter writer = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(writer);

        if (queryParams == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            when(request.getParameter(entry.getKey())).thenReturn((String) entry.getValue());
        }
    }

    protected void setUpSession(Map<String, Object> sessionAttributes) {
        when(request.getSession()).thenReturn(session);

        // Set session attributes dynamically
        if (sessionAttributes != null) {
            sessionAttributes.forEach((key, value) -> when(session.getAttribute(key)).thenReturn(value));
        }
    }

    protected JsonObject getResponseAsJson() {
        return payload.fromJson(responseWriter.toString(), JsonObject.class);
    }

    protected void validateResponseFields(JsonObject jsonData, Map<String, Object> validations, String testName) {
        for (Map.Entry<String, Object> entry : validations.entrySet()) {
            String field = entry.getKey();
            Map<String, Object> fieldValidation = (Map<String, Object>) entry.getValue();

            JsonElement element = jsonData.get(field);

            assert element != null : String.format("Field '%s' is missing in the response for test '%s'", field, testName);

            // Validate the field type (number, string, date)
            String type = (String) fieldValidation.get("type");
            if (type != null) {
                validateFieldType(element, type);
            }

            // Check if there's a value to validate
            Object expectedValue = fieldValidation.get("value");
            if (expectedValue != null) {
                validateFieldValue(element, expectedValue);
            }

            // Check regex
            String regex = (String) fieldValidation.get("regex");
            if (regex != null) {
                validateFieldRegex(element, regex);
            }
        }
    }

    private void validateFieldType(JsonElement element, String type) {
        JsonValidator.forType(type).assertValid(element);
    }

    private void validateFieldValue(JsonElement element, Object expectedValue) {
        JsonValidator.isEqualTo(expectedValue).assertValid(element);
    }

    private void validateFieldRegex(JsonElement element, String regex) {
        JsonValidator.matchRegex(regex).assertValid(element);
    }
}