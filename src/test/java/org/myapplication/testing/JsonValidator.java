package org.myapplication.testing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static org.junit.jupiter.api.Assertions.*;

public class JsonValidator {

    /**
     * Validates a field in a JSON object against the provided validator.
     *
     * @param json   The JSON object to validate.
     * @param field  The field name to validate.
     * @param validator The validator that defines the field's validation logic.
     * @throws IllegalArgumentException if the field is invalid or missing.
     */
    public static void validateField(JsonObject json, String field, Validator validator) {
        if (!json.has(field)) {
            throw new IllegalArgumentException("Missing field: " + field);
        }

        JsonElement element = json.get(field);

        // Validate field using the custom validator, asserting if invalid
        validator.assertValid(element);
    }

    /**
     * Validator interface for custom validation logic.
     */
    @FunctionalInterface
    public interface Validator {
        void assertValid(JsonElement element);
    }

    /**
     * Factory method for creating a string type validator.
     *
     * @return Validator that checks if the element is a string.
     */
    public static Validator isString() {
        return element -> {
            assertTrue( element.getAsJsonPrimitive().isString(),
                    "Expected a string, but got " + element
            );
        };
    }

    /**
     * Factory method for creating a number type validator.
     *
     * @return Validator that checks if the element is a number.
     */
    public static Validator isNumber() {
        return element -> {
            assertTrue( element.getAsJsonPrimitive().isNumber(),
                    "Expected a number, but got " + element
            );
        };
    }

    /**
     * Factory method for creating a date type validator.
     *
     * @return Validator that checks if the element is a valid date string (yyyy-MM-dd).
     */
    public static Validator isDate() {
        return element -> {
            assertTrue(element.getAsString().matches("\\d{4}-\\d{2}-\\d{2}"),
                    "Expected a date (yyyy-MM-dd), but got " + element
            );
        };
    }

    public static Validator isArray() {
        return element -> {
            assertTrue(element.isJsonArray(), "Expected an array");
        };
    }

    /**
     * Factory method for creating a custom value validator.
     *
     * @param expectedValue The value to compare against.
     * @return Validator that checks if the element matches the expected value.
     */
    public static Validator isEqualTo(String expectedValue) {
        return element -> {
            assertEquals(
                    expectedValue,
                    element.getAsString(),
                    "Expected value '" + expectedValue + "', but got '" + element.getAsString() + "'");
        };
    }

    /**
     * Factory method for creating a custom value validator.
     *
     * @param expectedValue The value to compare against.
     * @return Validator that checks if the element matches the expected value.
     */
    public static Validator isEqualTo(Integer expectedValue) {
        return element -> {
            assertEquals(
                    expectedValue,
                    element.getAsInt(),
                    "Expected value '" + expectedValue + "', but got '" + element.getAsInt() + "'");
        };
    }

    /**
     * Factory method for creating a custom value validator.
     *
     * @param expectedValue The value to compare against.
     * @return Validator that checks if the element matches the expected value.
     */
    public static Validator isEqualTo(Object expectedValue) {
        if (expectedValue instanceof String) {
            return isEqualTo((String) expectedValue);
        } else if (expectedValue instanceof Integer) {
            return isEqualTo((Integer) expectedValue);
        } else {
            throw new IllegalArgumentException("Unsupported value type: " + expectedValue.getClass().getSimpleName());
        }
    }

    public static Validator matchRegex(String regex) {
        return element -> {
            assertTrue(
                    element.isJsonPrimitive() && element.getAsString().matches(regex),
                    "Expected value matching regex '" + regex + "'"
            );
        };
    }

    /**
     * Factory method to create a general validator for any type.
     * This allows checking multiple types without needing a specific method.
     *
     * @param type The type to validate.
     * @return A validator for the specified type.
     */
    public static Validator forType(String type) {
        switch (type) {
            case "string":
                return isString();
            case "number":
                return isNumber();
            case "date":
                return isDate();
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
}