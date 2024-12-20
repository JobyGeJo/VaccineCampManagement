package org.myapplication.models;

import org.myapplication.enumerate.Role;
import org.myapplication.exceptions.InvalidRequestException;

import java.sql.Date;

public class UserModel {

    private int user_id;
    private String username;
    private String firstName;
    private String lastName;
    private String aadharNumber;
    private String phoneNumber;
    private Date dateOfBirth;
    private Role role;

    public void setUserId(int user_id) { this.user_id = user_id; }

    public void setUsername(String username) {
        if (!username.matches("^[A-Za-z0-9]{4,16}$")) {
            throw new InvalidRequestException("Invalid user name");
        }
        this.username = username.toLowerCase();
    }

    public void setFirstName(String firstName) {
        if (!firstName.matches("^[A-Za-z]{1,30}$")) {
            throw new InvalidRequestException("Invalid first name");
        }
        this.firstName = firstName.toLowerCase();
    }

    public void setLastName(String lastName) {
        if (!lastName.matches("^[A-Za-z]{1,30}$")) {
            throw new InvalidRequestException("Invalid last name");
        }
        this.lastName = lastName.toLowerCase();
    }

    public void setAadharNumber(String aadharNumber) {
        if (!aadharNumber.matches("^[0-9]{12}$")) {
            throw new InvalidRequestException("Invalid aadhar number");
        }
        this.aadharNumber = aadharNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (!phoneNumber.matches("^[1-9][0-9]{9}$")) {
            throw new InvalidRequestException("Invalid phone number");
        }
        this.phoneNumber = phoneNumber;
    }

    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public void setDateOfBirth(String dateOfBirth) {
        if (dateOfBirth == null) {
            return;
        } else if (!dateOfBirth.matches("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$")) {
            throw new InvalidRequestException("Invalid date of birth");
        }
        this.dateOfBirth = Date.valueOf(dateOfBirth);
    }
    public void setRole(org.myapplication.enumerate.Role Role) { this.role = Role; }

    public int getUserId() { return user_id; }
    public String getFullName() {
        if (firstName == null || lastName == null) {
            return null;
        }

        return ((char) (firstName.charAt(0) - 32) + firstName.substring(1) + " " +
                (char) (lastName.charAt(0) - 32) + lastName.substring(1));
    }
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getAadharNumber() { return aadharNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public Date getDateOfBirth() { return dateOfBirth; }
    public Role getRole() { return role; }

    @Override
    public String toString() {
        JsonModel userDetails = new JsonModel();

        userDetails.set("user_id", getUserId());
        userDetails.set("user_name", getUsername());
        userDetails.set("full_name", getFullName());
        userDetails.set("aadhar_number", getAadharNumber());
        userDetails.set("phone_number", getPhoneNumber());
        userDetails.set("date_of_birth", getDateOfBirth());
        userDetails.set("role", getRole());

        return userDetails.toString();
    }

}
