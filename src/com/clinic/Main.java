package com.clinic;

import com.clinic.db.Database;
import com.clinic.model.Patient;
import com.clinic.service.Clinic;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final Clinic clinic = new Clinic();

    public static void main(String[] args) {
        System.out.println("===== HOSPITAL MANAGEMENT SYSTEM =====");
        boolean running = true;
        while (running) {
            System.out.println("\n1. Register Patient\n2. List Patients\n3. List Doctors"
                    + "\n4. Book Appointment\n5. View Appointments\n6. Cancel Appointment"
                    + "\n7. Generate Bill\n8. Export Report (CSV)\n9. Total Revenue\n0. Exit");
            System.out.print("Choice: ");
            switch (sc.nextLine().trim()) {
                case "1": register();   break;
                case "2": clinic.getPatients().forEach(System.out::println); break;
                case "3": clinic.getDoctors().forEach(System.out::println);  break;
                case "4": book();       break;
                case "5": view();       break;
                case "6": System.out.print("Appointment ID: ");
                        clinic.cancel(Integer.parseInt(sc.nextLine()));
                        System.out.println("Cancelled."); break;
                case "7": bill();       break;
                case "8": export();     break;
                case "9": System.out.printf("Total revenue: Rs. %.2f%n", clinic.totalRevenue()); break;
                case "0": clinic.shutdown(); running = false; break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void register() {
        System.out.print("Name: ");        String name = sc.nextLine();
        System.out.print("Age: ");         int age = Integer.parseInt(sc.nextLine());
        System.out.print("Contact: ");     String contact = sc.nextLine();
        System.out.print("Blood group: "); String bg = sc.nextLine();

        Patient p = new Patient.Builder().name(name).age(age)
                .contact(contact).bloodGroup(bg).build();
        int id = clinic.registerPatient(p);
        Database.getInstance().savePatient(name);   // JDBC (optional)
        System.out.println("Patient registered with ID: " + id);
    }

    private static void book() {
        clinic.getDoctors().forEach(System.out::println);
        System.out.print("Doctor ID: ");  int doc = Integer.parseInt(sc.nextLine());
        System.out.print("Patient ID: "); int pat = Integer.parseInt(sc.nextLine());
        System.out.print("Slot (dd-MM-yyyy HH:mm): ");
        LocalDateTime slot = LocalDateTime.parse(sc.nextLine(),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));

        // ExecutorService: booking happens on a worker thread
        Future<Integer> f = clinic.bookAsync(pat, doc, slot);
        try {
            System.out.println("Appointment confirmed! ID = " + f.get(5, TimeUnit.SECONDS));
        } catch (ExecutionException e) {
            System.out.println("Booking failed: " + e.getCause().getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void view() {
        // STREAMS: sort by slot then print (Module 4)
        clinic.getAppointments().stream()
                .sorted(Comparator.comparing(a -> a.getSlot()))
                .forEach(System.out::println);
    }

    private static void bill() {
        System.out.print("Appointment ID: ");
        int id = Integer.parseInt(sc.nextLine());
        System.out.print("Medicine charges: ");
        double med = Double.parseDouble(sc.nextLine());
        System.out.println(clinic.generateBill(id, med));
    }

    private static void export() {
        try {
            System.out.println("Report written to: " + clinic.exportReport());
        } catch (Exception e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }
}
