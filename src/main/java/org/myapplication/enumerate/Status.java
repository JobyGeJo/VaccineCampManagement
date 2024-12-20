package org.myapplication.enumerate;

public enum Status {
    NULL(0, "Not Applied"),
    PENDING(1, "Pending"),
    SUCCESS(2, "Success"),
    CANCELLED(3, "Cancelled"),
    EXPIRED(4, "Expired");

    private final int code;
    private final String description;

    // Constructor for the enum
    Status(int code, String description) {
        this.code = code;
        this.description = description;
    }

    // Getter for code
    public int getCode() {
        return code;
    }

    // Getter for description
    @Override
    public String toString() {
        return description;
    }

    // Method to get enum from code
    public static Status fromCode(int code) {
        for (Status status : Status.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }

    // Helper method to parse a role from a string, handling spaces or lowercase
    public static Status fromString(String statusName) {
        if (statusName == null || statusName.equals("null")){
            return Status.NULL;
        }
        for (Status status : Status.values()) {
            if (status.name().equalsIgnoreCase(statusName.replace(" ", "_"))) {
                return status;
            }
        }
        throw new IllegalArgumentException("No enum constant for " + statusName);
    }
}
