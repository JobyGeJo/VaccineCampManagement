package org.myapplication.enumerate;

import org.myapplication.exceptions.InvalidRequestException;

public enum Role {
    NULL(null, 0),
    FACILITATOR("Facilitator", 1),
    ADMIN("Admin", 2),
    OWNER("Owner", 3);

    private final String displayName;
    private final int priorityCode;

    Role(String displayName, int priorityCode) {
        this.displayName = displayName;
        this.priorityCode = priorityCode;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public int getPriorityCode() {
        return priorityCode;
    }

    // Helper method to parse a role from a string, handling spaces or lowercase
    public static Role fromString(String roleName) {
        if (roleName == null || roleName.equals("null")){
            return Role.NULL;
        }
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(roleName.replace(" ", "_"))) {
                return role;
            }
        }
        throw new IllegalArgumentException("No enum constant for " + roleName);
    }

    public static Role getNextRole(Role currentRole) {
        switch (currentRole) {
            case NULL:
                return Role.FACILITATOR;
            case FACILITATOR:
                return Role.ADMIN;
            case ADMIN:
                return Role.OWNER;
            case OWNER:
                throw new InvalidRequestException("No upgrade available for role: " + OWNER);
            default:
                throw new InvalidRequestException("No enum constant for " + currentRole);
        }
    }

    public static Role getPrevoiusRole(Role currentRole) {
        switch (currentRole) {
            case OWNER:
                throw new InvalidRequestException("can't Update Owner");
            case ADMIN:
                return Role.FACILITATOR;
            case FACILITATOR:
                return Role.NULL;
            case NULL:
                throw new InvalidRequestException("User is not a Admin to set Role");
            default:
                throw new InvalidRequestException("No enum constant for " + currentRole);
        }
    }
}