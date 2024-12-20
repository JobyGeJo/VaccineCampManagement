package org.myapplication.exceptions;

import org.myapplication.utils.ColoredOutput;

import java.sql.SQLException;

// Custom Exception class to handle database-related errors
public class DataBaseException extends Exception {

    public DataBaseException(String message) {
        super(message);
    }

    public DataBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataBaseException(SQLException e) {
        super(getCustomMessage(e), e);
    }

    private static String getCustomMessage(SQLException e) {
        String message;

        switch (e.getSQLState()) {
            case "23505":  // Unique violation
                String columnName = getDuplicateColumnName(e.getMessage());
                message = "Duplicate value found: " + columnName;
                break;
            case "23514":  // Check violation
                message = "The data does not meet the specified constraints.";
                break;
            case "08001":  // Connection exception
                message = "Unable to connect to the database. Please check the connection settings.";
                break;
            case "22001":  // String data right truncation
                message = "The data provided is too large for the database field.";
                break;
            case "23503":  // Foreign key violation
                String foreignKey = getForeignKeyDetails(e.getMessage());
                message = foreignKey + " doesn't exist.";
                break;
            case "23502":  // Not null violation
                message = "A required field is missing.";
                break;
            default:
                message = "An unexpected database error occurred: " + e.getMessage();
                break;
        }
        return message;
    }

    private static String getDuplicateColumnName(String sqlMessage) {
        // Regular expression to capture the column or constraint name
        String pattern = "(?<=\\()\\w+(?=\\))";  // Matches words within parentheses
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(sqlMessage);

        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private static String getForeignKeyDetails(String sqlMessage) {
        // Regular expression to match foreign key constraint or column name inside parentheses
        String pattern = "(?<=\\()(\\w+)(?=\\))";  // Matches words within parentheses
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(sqlMessage);

        if (matcher.find()) {
            return matcher.group(); // Return the first matched group (either column or constraint name)
        }
        return null;
    }
}
