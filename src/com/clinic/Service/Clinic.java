package com.clinic.service;

import com.clinic.model.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Core service: patients, doctors, concurrent appointment booking,
 * billing (Payable) and NIO.2 report export.
 *
 * CONCURRENCY (Module 5): booking runs on an ExecutorService thread pool.
 * The synchronized block around the conflict check stops two threads from
 * double-booking the same doctor + slot at the same instant.
 */
public class Clinic {

    private final Map<Integer, Patient> patients = new HashMap<>();
    private final Map<Integer, Doctor> doctors = new HashMap<>();
    private final List<Appointment> appointments = new ArrayList<>();
    private final List<String> bills = new ArrayList<>();
    private final List<Double> revenue = new ArrayList<>();

    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    private final Object lock = new Object();          // booking guard

    private int nextPatientId = 1;
    private int nextAppointmentId = 1;

    public Clinic() {
        // a few seed doctors so the app is usable immediately
        addDoctor(new Doctor.Builder().id(101).name("Dr. Sharma")
                .specialization("Cardiology").consultationFee(800).contact("9876500001").build());
        addDoctor(new Doctor.Builder().id(102).name("Dr. Patel")
                .specialization("Dermatology").consultationFee(500).contact("9876500002").build());
        addDoctor(new Doctor.Builder().id(103).name("Dr. Iyer")
                .specialization("Pediatrics").consultationFee(600).contact("9876500003").build());
    }

    // ---------- PATIENT REGISTRATION (Module 1) ----------
    public synchronized int registerPatient(Patient p) {
        int id = nextPatientId++;
        Patient saved = new Patient.Builder().id(id).name(p.getName())
                .age(p.getAge()).contact(p.getContact())
                .bloodGroup(p.getBloodGroup()).build();
        patients.put(id, saved);
        return id;
    }

    public Collection<Patient> getPatients() { return patients.values(); }
    public Collection<Doctor> getDoctors()  { return doctors.values(); }
    public List<Appointment> getAppointments() { return appointments; }

    private void addDoctor(Doctor d) { doctors.put(d.getId(), d); }

    // ---------- APPOINTMENT SCHEDULING (Concurrency, Module 5) ----------
    public Future<Integer> bookAsync(int patientId, int doctorId, LocalDateTime slot) {
        return pool.submit(() -> book(patientId, doctorId, slot));
    }

    public int book(int patientId, int doctorId, LocalDateTime slot) throws Exception {
        if (!patients.containsKey(patientId)) throw new Exception("Patient not found.");
        if (!doctors.containsKey(doctorId))   throw new Exception("Doctor not found.");
        if (slot.isBefore(LocalDateTime.now())) throw new Exception("Slot is in the past.");

        synchronized (lock) {   // check-then-act must be atomic
            boolean taken = appointments.stream()
                    .anyMatch(a -> a.getDoctorId() == doctorId
                            && a.getSlot().equals(slot)
                            && a.getStatus() == Appointment.Status.BOOKED);
            if (taken) throw new Exception("Slot " + slot + " already booked for doctor " + doctorId);

            Appointment a = new Appointment(nextAppointmentId++, patientId, doctorId, slot);
            appointments.add(a);
            return a.getId();
        }
    }

    public void cancel(int appointmentId) {
        appointments.stream()
                .filter(a -> a.getId() == appointmentId)
                .findFirst()
                .ifPresent(a -> a.setStatus(Appointment.Status.CANCELLED));
    }

    // ---------- BILLING via Payable interface ----------
    public String generateBill(int appointmentId, double medicineCharges) {
        Appointment a = appointments.stream()
                .filter(x -> x.getId() == appointmentId).findFirst().orElse(null);
        if (a == null) return "Appointment not found.";
        Doctor d = doctors.get(a.getDoctorId());

        Payable bill = () -> {   // LAMBDA implementing Payable
            double sub = d.getConsultationFee() + medicineCharges;
            return sub + sub * 0.05;   // 5% tax
        };
        double total = bill.calculateAmount();
        String receipt = String.format(
                "BILL for Appt#%d | Doctor: %s | Fee: %.2f | Medicines: %.2f | %s",
                appointmentId, d.getName(), d.getConsultationFee(), medicineCharges,
                bill.describe());
        bills.add(receipt);
        revenue.add(total);
        a.setStatus(Appointment.Status.COMPLETED);
        return receipt;
    }

    public double totalRevenue() {
        // STREAMS: sum of all bill totals (Module 4)
        return revenue.stream().mapToDouble(Double::doubleValue).sum();
    }

    // ---------- REPORT EXPORT via NIO.2 (Module 3) ----------
    public Path exportReport() throws IOException {
        Files.createDirectories(Paths.get("reports"));
        Path file = Paths.get("reports", "report_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        StringBuilder sb = new StringBuilder("appointment_id,patient_id,doctor_id,slot,status\n");
        // STREAMS + lambda for CSV building (Module 4)
        appointments.forEach(a -> sb.append(String.format("%d,%d,%d,%s,%s\n",
                a.getId(), a.getPatientId(), a.getDoctorId(), a.getSlot(), a.getStatus())));
        bills.forEach(b -> sb.append("# ").append(b).append("\n"));

        Files.write(file, sb.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return file;
    }

    public void shutdown() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(3, TimeUnit.SECONDS)) pool.shutdownNow();
        } catch (InterruptedException e) {
            pool.shutdownNow();
        }
    }
}
