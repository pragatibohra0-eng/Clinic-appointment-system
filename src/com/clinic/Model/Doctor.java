package com.clinic.model;

public class Doctor extends Person {
    private final String specialization;
    private final double consultationFee;

    private Doctor(Builder b) {
        super(b.id, b.name, b.contact);
        this.specialization = b.specialization;
        this.consultationFee = b.consultationFee;
    }

    public String getSpecialization() { return specialization; }
    public double getConsultationFee() { return consultationFee; }

    @Override
    public String getRole() { return "Doctor"; }

    @Override
    public String toString() {
        return super.toString() + String.format(" | %s | Fee: Rs.%.2f", specialization, consultationFee);
    }

    public static class Builder {
        private int id; private String name; private String contact;
        private String specialization; private double consultationFee;
        public Builder id(int v) { id = v; return this; }
        public Builder name(String v) { name = v; return this; }
        public Builder contact(String v) { contact = v; return this; }
        public Builder specialization(String v) { specialization = v; return this; }
        public Builder consultationFee(double v) { consultationFee = v; return this; }
        public Doctor build() { return new Doctor(this); }
    }
}
