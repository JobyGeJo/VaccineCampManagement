package org.myapplication.models;

public class VaccineModel {

    private int vaccineId;
    private String vaccineName;
    private Integer stock;
    private int dosages;
    private AppointmentModel[] appointments;

    public void setVaccineId(int vaccineId) { this.vaccineId = vaccineId; }
    public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }
    public void setStock(int stock) { this.stock = stock; }
    public void setDosages(int dosages) { this.dosages = dosages; }
    public void setAppointments(AppointmentModel[] appointments) { this.appointments = appointments; }

    public int getVaccineId() { return vaccineId; }
    public String getVaccineName() { return vaccineName; }
    public Integer getStock() { return stock; }
    public int getDosages() { return dosages; }
    public AppointmentModel[] getAppointments() { return appointments; }

    @Override
    public String toString() {
        JsonModel vaccination = new JsonModel();

        vaccination.set("vaccine_id", getVaccineId());
        vaccination.set("vaccine_name", getVaccineName());
        vaccination.set("stock", getStock());
        vaccination.set("dosages", getDosages());
        vaccination.set("appointments", getAppointments());

        return vaccination.toString();
    }
}
