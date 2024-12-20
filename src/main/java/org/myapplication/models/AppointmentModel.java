package org.myapplication.models;

import org.myapplication.enumerate.Slot;
import org.myapplication.enumerate.Status;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AppointmentModel {

    public AppointmentModel() {}

    public AppointmentModel(ResultSet rs) throws SQLException {
        setAppointmentId(rs.getInt("appointment_id"));
        setCampName(rs.getString("location"));
        setSlot(rs.getString("slot"));
        setDate(rs.getDate("date_of_vaccination"));
        setStatus(rs.getString("status"));
    }

    private int appointmentId;
    private String vaccineName;
    private String campName;
    private Slot slot;
    private Date date;
    private Status status;
    private UserModel user;

    public void setAppointmentId(int appointmentId) {this.appointmentId = appointmentId;}
    public void setCampName(String campName) { this.campName = campName; }
    public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }
    public void setSlot(Slot slot) { this.slot = slot; }
    public void setSlot(String slot) { this.slot = Slot.fromString(slot); }
    public void setDate(Date date) { this.date = date; }
    public void setDate(String date) { this.date = Date.valueOf(date); }
    public void setStatus(Status status) { this.status = status; }
    public void setStatus(String status) { this.status = Status.fromString(status); }

    public int getAppointmentId() { return appointmentId; }
    public String getVaccineName() { return vaccineName; }
    public String getCampName() { return campName; }
    public Date getDate() { return date; }
    public String getSlot() { return slot.toString(); }
    public String getStatus() { return status.toString(); }

    public void setUser(UserModel user) { this.user = user; }
    public UserModel getUser() { return user; }

    @Override
    public String toString() {
        JsonModel appointment = new JsonModel();

        appointment.set("appointment_id", getAppointmentId());
        appointment.set("location", getCampName());
        appointment.set("vaccine_name", getVaccineName());
        appointment.set("slot", getSlot());
        appointment.set("date", getDate());
        appointment.set("status", getStatus());

        if (user != null) {
            appointment.set("full_name", getUser().getFullName());
            appointment.set("aadhar_number", getUser().getAadharNumber());
        }

        return appointment.toString();
    }
}
