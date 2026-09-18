package com.clinic.model;

// BUILDER PATTERN for clean object creation (Module 2)
public class Patient extends Person {
    private final int age;
    private final String bloodGroup;

    private Patient(Builder b) {
        super(b.id, b.name, b.contact);
        this.age = b.age;
        this.bloodGroup = b.bloodGroup;
    }

    public int getAge() { return age; }
    public String getBloodGroup() { return bloodGroup; }

    @Override
    public String getRole() { return "Patient"; }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Age: %d | Blood: %s", age, bloodGroup);
    }

    public static class Builder {
        private int id; private String name; private String contact;
        private int age; private String bloodGroup;
        public Builder id(int v) { id = v; return this; }
        public Builder name(String v) { name = v; return this; }
        public Builder contact(String v) { contact = v; return this; }
        public Builder age(int v) { age = v; return this; }
        public Builder bloodGroup(String v) { bloodGroup = v; return this; }
        public Patient build() { return new Patient(this); }
    }
}
