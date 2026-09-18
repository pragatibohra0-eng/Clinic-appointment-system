package com.clinic.model;

// ABSTRACT CLASS: common fields for Patient and Doctor (Module 2)
public abstract class Person {
    protected int id;
    protected String name;
    protected String contact;

    public Person(int id, String name, String contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
    }

    public abstract String getRole();   // each subclass defines its role

    public int getId() { return id; }
    public String getName() { return name; }
    public String getContact() { return contact; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - %s", id, name, getRole(), contact);
    }
}
