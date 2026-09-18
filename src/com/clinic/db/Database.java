package com.clinic.db;

import java.sql.*;

/**
 * JDBC + SINGLETON (Modules 2 & 5).
 * Demonstrates DB connectivity: every save tries MySQL first,
 * and if the DB is not set up the app simply keeps running in memory.
 *
 * To activate: create database 'clinic_db' with a table
 *   CREATE TABLE patients(id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100));
 * and put your credentials below.
 */
public final class Database {

    private static final String URL = "jdbc:mysql://localhost:3306/clinic_db";
    private static final String USER = "root";
    private static final String PASS = "root";

    private Database() {}

    private static class Holder {
        private static final Database INSTANCE = new Database();
    }

    public static Database getInstance() { return Holder.INSTANCE; }

    public void savePatient(String name) {
        String sql = "INSERT INTO patients(name) VALUES (?)";
        try (Connection c = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
            System.out.println("[DB] patient saved to MySQL.");
        } catch (SQLException e) {
            System.out.println("[DB] not available - record kept in memory only.");
        }
    }
}
