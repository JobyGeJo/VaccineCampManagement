package org.myapplication.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.myapplication.exceptions.InvalidRequestException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class JsonModel {

    private JsonObject jsonObject;

    public JsonModel(String json) {
        this.jsonObject = JsonParser.parseString(json).getAsJsonObject();
    }
    public JsonModel() { this.jsonObject = new JsonObject(); }

    public JsonModel(BufferedReader reader) throws IOException {
        StringBuilder jsonBuffer = new StringBuilder();
        String line;

        try (reader) {
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }

            this.jsonObject = JsonParser.parseString(jsonBuffer.toString()).getAsJsonObject();
        } catch (IllegalStateException e) {
            this.jsonObject = JsonParser.parseString("{}").getAsJsonObject();
        }


    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }

    /**
     * Retrieves the value associated with the specified key from the underlying JSON object.
     * The method supports multiple types (e.g., number, boolean, string) and returns
     * the corresponding Java object type based on the value in the JSON.
     *
     * <p>
     * If the value is {@code null} and an exception will be thrown.
     * </p>
     *
     * @param key the key to retrieve the value for.
     * @return the value as an {@code Object}, which could be an {@code Integer}, {@code Boolean},
     *         {@code String}, or a {@code JsonObject}.
     * @throws InvalidRequestException if the key is not found or the value is {@code null} and
     *         {@code rejectNull} is {@code true}.
     */
    public Object get(String key) {
        return this.get(key, true);
    }

    /**
     * Retrieves the value associated with the specified key from the underlying JSON object.
     * Allows control over whether {@code null} values are rejected.
     *
     * @param key        the key to retrieve the value for.
     * @param rejectNull if {@code true}, throws an exception if the value is {@code null}.
     * @return the value as an {@code Object}, which could be an {@code Integer}, {@code Boolean},
     *         {@code String}, or a {@code JsonObject}.
     * @throws InvalidRequestException if the key is not found or the value is {@code null} and
     *         {@code rejectNull} is {@code true}.
     */
    public Object get(String key, boolean rejectNull) {
        JsonElement jsonElement = jsonObject.get(key);

        if (jsonElement == null || jsonElement.isJsonNull()) {
            if (rejectNull)
                throw new InvalidRequestException(key + " cannot be null");
            return null;
        } else if (jsonElement.getAsJsonPrimitive().isNumber()) {
            return jsonElement.getAsInt();
        } else if (jsonElement.getAsJsonPrimitive().isBoolean()) {
            return jsonElement.getAsBoolean();
        } else if (jsonElement.getAsJsonPrimitive().isString()) {
            return jsonElement.getAsString();
        }

        return jsonElement.getAsJsonObject();
    }

    public String[] getKeys() {
        Set<String> keySet = jsonObject.keySet();
        keySet.remove("action");
        return keySet.toArray(new String[0]);
    }

    public void set(String key, Object value) { set(key, value, false); }

    public void set(String key, Object value, boolean setNull) {
        if (value == null) {
            if (Boolean.TRUE.equals(setNull)) {
                this.jsonObject.add(key, null);
            }
        } else if (value instanceof Number) {
            this.jsonObject.addProperty(key, (Number) value);
        } else if (value instanceof Boolean) {
            this.jsonObject.addProperty(key, (Boolean) value);
        } else if (value.getClass().isArray()) {
            JsonArray jsonArray = new JsonArray();
            for (Object item : (Object[]) value) {
                jsonArray.add(JsonParser.parseString(item.toString()));
            }
            this.jsonObject.add(key, jsonArray);
        } else {
            this.jsonObject.addProperty(key, value.toString());
        }
    }

    public UserModel getUser() {

        UserModel userModel = new UserModel();

        userModel.setUsername((String) this.get("user_name"));
        userModel.setFirstName((String) this.get("first_name"));
        userModel.setLastName((String) this.get("last_name"));
        userModel.setAadharNumber((String) this.get("aadhar_number"));
        userModel.setPhoneNumber((String) this.get("phone_number"));
        userModel.setDateOfBirth((String) this.get("date_of_birth", false));

        return userModel;
    }
}
