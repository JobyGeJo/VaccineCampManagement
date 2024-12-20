package org.myapplication.models;

import java.sql.Date;
import java.util.Arrays;

public class CampModel {

    private int campId;
    private String location;
    private Date startDate;
    private Date endDate;
    private int totolStock;
    private int appointmentCount;
    private AppointmentModel[] appointments;

    public void setCampId(int camp_id) { this.campId = camp_id; }
    public void setLocation(String location) { this.location = location.toLowerCase(); }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public void setStartDate(String startDate) {
        if (startDate == null) {
            return;
        } else if (!startDate.matches("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$")) {
            throw new IllegalArgumentException("Invalid start date");
        }
        this.startDate = Date.valueOf(startDate);
    }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public void setEndDate(String endDate) {
        if (endDate == null) {
            return;
        } else if (!endDate.matches("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$")) {
            throw new IllegalArgumentException("Invalid end date");
        }
        this.endDate = Date.valueOf(endDate);
    }
    public void setAppointments(AppointmentModel[] appointments) { this.appointments = appointments; }

    public int getCampId() { return campId; }
    public String getLocation() { return location; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public String getStatus() {
        if (startDate == null) {
            return null;
        } else if (endDate != null && endDate.before(new Date(System.currentTimeMillis()))) {
            return "Inactive";
        }
        return "Active";
    }
    public AppointmentModel[] getAppointments() { return appointments; }

    public void setTotolStock(int totolStock) { this.totolStock = totolStock; }
    public void setAppointmentCount(int appointmentCount) { this.appointmentCount = appointmentCount; }
    public int getAppointmentCount() { return appointmentCount; }
    public int getTotolStock() { return totolStock; }

    @Override
    public String toString() {
        JsonModel campDetails = new JsonModel();

        campDetails.set("camp_id", getCampId());
        campDetails.set("location", getLocation());
        campDetails.set("start_date", getStartDate());
        campDetails.set("end_date", getEndDate(), true);
        campDetails.set("status", getStatus());
        campDetails.set("appointments", getAppointments());

        campDetails.set("total_stock", getTotolStock());
        campDetails.set("appointment_count", getAppointmentCount());

        return campDetails.toString();
    }
}
