package org.myapplication.testing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.myapplication.servlets.DispatcherServlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamicAPITest extends BaseServiceTest {
    private DispatcherServlet servlet;

    @BeforeEach
    void init() {
        servlet = new DispatcherServlet();
        setUp();
    }

    static Stream<Arguments> loadTestCases() {
        // Combine test cases from multiple YAML files
        List<Map<String, Object>> combinedTestCases = new ArrayList<>();

        String[] yamlFiles = {
                "campTest.yaml",
                "tests.yaml"
        };

        for (String yamlFile : yamlFiles) {
            combinedTestCases.addAll(TestConfigLoader.loadTests(yamlFile));
        }

        // Map each test case to Arguments.of() with the name and the test case itself
        return combinedTestCases.stream()
                .map(testCase -> Arguments.of(testCase.get("name"), testCase));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loadTestCases")
    @SuppressWarnings("unchecked")
    void runTestCase(String testName, Map<String, Object> testCase) throws Exception {
        System.out.println("Running test: " + testName);

        // Extract fields from the testCase map
        String method = (String) testCase.get("method");
        String endpoint = (String) testCase.get("endpoint");
        Map<String, Object> payload = (Map<String, Object>) testCase.get("payload");
        Map<String, Object> sessionAttributes = (Map<String, Object>) testCase.get("session_attributes");
        Map<String, Object> expectedResponse = (Map<String, Object>) testCase.get("expected_response");

        // Set up request
        String payloadJson = this.payload.toJson(payload);
        setUpRequest(request, response, method, endpoint, payloadJson);
        setUpSession(sessionAttributes);

        // Execute servlet
        servlet.service(request, response);

        // Parse and validate response
        JsonObject jsonResponse = getResponseAsJson();
        int expectedStatus = (int) expectedResponse.get("status");
        assertEquals(expectedStatus, jsonResponse.get("status").getAsInt(),
                String.format("Test '%s' failed: Expected status %d but got %d", testName, expectedStatus, jsonResponse.get("status").getAsInt()));

        Map<String, Object> validations = (Map<String, Object>) expectedResponse.get("validate");
        if (validations != null) {
            JsonElement dataElement = jsonResponse.get("data");

            if (dataElement.isJsonArray()) {
                // If the response data is an array, validate each item in the array
                for (JsonElement item : dataElement.getAsJsonArray()) {
                    if (item.isJsonObject()) {
                        validateResponseFields(item.getAsJsonObject(), validations, testName);
                    } else {
                        throw new IllegalArgumentException("Invalid data type in array: expected JSON object but found " + item.getClass().getSimpleName());
                    }
                }
            } else if (dataElement.isJsonObject()) {
                // If the response data is a single object, validate the object
                validateResponseFields(dataElement.getAsJsonObject(), validations, testName);
            } else {
                throw new IllegalArgumentException("Invalid 'data' type: expected JSON object or array but found " + dataElement.getClass().getSimpleName());
            }
        }
    }
}