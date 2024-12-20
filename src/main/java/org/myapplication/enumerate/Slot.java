package org.myapplication.enumerate;

public enum Slot {
    MORNING(1, "Morning", 9),
    AFTERNOON(2, "Afternoon", 12),
    EVENING(3, "Evening", 15);

    private final int code;
    private final String description;
    private final int time; //Slot starting time

    // Constructor for the enum
    Slot(int code, String description, int time) {
        this.code = code;
        this.description = description;
        this.time = time;
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
    public static Slot fromCode(int code) {
        for (Slot slot : Slot.values()) {
            if (slot.getCode() == code) {
                return slot;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }

    // Helper method to parse a role from a string, handling spaces or lowercase
    public static Slot fromString(String slotName) {
        for (Slot slot : Slot.values()) {
            if (slot.name().equalsIgnoreCase(slotName.replace(" ", "_"))) {
                return slot;
            }
        }
        throw new IllegalArgumentException("No enum constant for " + slotName);
    }

    public int getTime() {
        return time;
    }
}
