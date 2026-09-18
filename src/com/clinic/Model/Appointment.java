package com.clinic.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// DATE/TIME API: LocalDateTime + DateTimeFormatter (Module 3)
public class Appointment {
    public enum Status { BOOKED, CANCELLED, COMPLETED }

    private final int id;
    private final int patientId;
    private final int doctorId;
    private final LocalDateTime slot;
    private Status status;

    public Appointment(int id, int patientId, int doctorId, LocalDateTime slot) {
        this.id = id; this.patientId = patientId; this.doctorId = doctorId;
        this.slot = slot; this.status = Status.BOOKED;
    }

    public int getId() { return id; }
    public int getPatientId() { return patientId; }
    public int getDoctorId() { return doctorId; }
    public LocalDateTime getSlot() { return slot; }
    public Status getStatus() { return status; }
    public void setStatus(Status s) { status = s; }

    @Override
    public String toString() {
        return String.format("Appt#%d | Patient:%d -> Doctor:%d | %s | %s",
                id, patientId, doctorId,
                slot.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")), status);
    }
}
