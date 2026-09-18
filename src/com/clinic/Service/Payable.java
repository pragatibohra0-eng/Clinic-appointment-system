package com.clinic.service;

// INTERFACE: anything that produces a bill (Module 2)
@FunctionalInterface
public interface Payable {
    double calculateAmount();

    default String describe() {
        return String.format("Total payable: Rs. %.2f", calculateAmount());
    }
}
