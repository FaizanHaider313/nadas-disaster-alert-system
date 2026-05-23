package com.disaster.alert.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton DatabaseManager.
 * Creates nadas.db in the project root on first run.
 * Pattern: Singleton (GoF)
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;

    private static final String DB_URL = "jdbc:sqlite:nadas.db";

    private DatabaseManager() {
        connect();
        createTables();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("[DB] Connected to nadas.db");
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
        }
    }

    private void createTables() {
        String users = """
            CREATE TABLE IF NOT EXISTS users (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                name          TEXT    NOT NULL,
                email         TEXT    NOT NULL UNIQUE,
                password      TEXT    NOT NULL,
                city          TEXT    DEFAULT 'Islamabad',
                alert_types   TEXT    DEFAULT 'Weather,Earthquake,Fire,Flood',
                severity_level TEXT   DEFAULT 'Low',
                email_alerts  INTEGER DEFAULT 1,
                emergency_only INTEGER DEFAULT 0
            );""";

        String admins = """
            CREATE TABLE IF NOT EXISTS admins (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                name     TEXT    NOT NULL,
                email    TEXT    NOT NULL UNIQUE,
                password TEXT    NOT NULL,
                role     TEXT    DEFAULT 'System Admin'
            );""";

        String alerts = """
            CREATE TABLE IF NOT EXISTS alerts (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                type       TEXT,
                location   TEXT,
                severity   TEXT,
                message    TEXT,
                source     TEXT,
                timestamp  TEXT DEFAULT (datetime('now','localtime'))
            );""";

        String hazard_reports = """
            CREATE TABLE IF NOT EXISTS hazard_reports (
                id           INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id      INTEGER,
                hazard_type  TEXT,
                location     TEXT,
                description  TEXT,
                photo_path   TEXT,
                status       TEXT DEFAULT 'Pending',
                submitted_at TEXT DEFAULT (datetime('now','localtime')),
                FOREIGN KEY (user_id) REFERENCES users(id)
            );""";

        String threshold_rules = """
            CREATE TABLE IF NOT EXISTS threshold_rules (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                hazard_type     TEXT    NOT NULL,
                magnitude_limit REAL    DEFAULT 5.0,
                rainfall_limit  REAL    DEFAULT 50.0,
                temp_limit      REAL    DEFAULT 45.0,
                is_active       INTEGER DEFAULT 1,
                updated_at      TEXT    DEFAULT (datetime('now','localtime'))
            );""";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(users);
            stmt.execute(admins);
            stmt.execute(alerts);
            stmt.execute(hazard_reports);
            stmt.execute(threshold_rules);
            seedDefaultData(stmt);
            System.out.println("[DB] Tables ready.");
        } catch (SQLException e) {
            System.err.println("[DB] Table creation error: " + e.getMessage());
        }
    }

    private void seedDefaultData(Statement stmt) {
        try {
            // ✅ Admin credentials: i240505@isb.nu.edu.pk / 12345
            stmt.execute("""
                INSERT OR IGNORE INTO admins (name, email, password, role)
                VALUES ('System Admin', 'nadas.alerts@gmail.com', '12345', 'System Admin');
            """);

            String[] hazards    = {"Earthquake", "Flood", "Weather", "Fire"};
            double[] magnitudes = {5.0, 0.0, 0.0, 0.0};
            double[] rainfalls  = {0.0, 50.0, 30.0, 0.0};
            double[] temps      = {0.0, 0.0, 42.0, 0.0};
            for (int i = 0; i < hazards.length; i++) {
                stmt.execute(String.format(
                        "INSERT OR IGNORE INTO threshold_rules " +
                                "(hazard_type, magnitude_limit, rainfall_limit, temp_limit) " +
                                "VALUES ('%s', %.1f, %.1f, %.1f);",
                        hazards[i], magnitudes[i], rainfalls[i], temps[i]));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Seed error: " + e.getMessage());
        }
    }

    /**
     * Verifies admin credentials against the admins table.
     * Returns the admin name if valid, null otherwise.
     */
    public String verifyAdmin(String email, String password) {
        String sql = "SELECT name FROM admins WHERE email = ? AND password = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("name");
        } catch (SQLException e) {
            System.err.println("[DB] verifyAdmin error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns emails of all users whose city is mentioned in the location string.
     * Only users with email_alerts=1 are included.
     */
    public List<String> getUserEmailsByCity(String location) {
        List<String> emails = new ArrayList<>();
        String sql = "SELECT email, city FROM users WHERE email_alerts = 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String city  = rs.getString("city");
                String email = rs.getString("email");
                String locLower  = location != null ? location.toLowerCase() : "";
                String cityLower = city     != null ? city.toLowerCase()     : "";
                if (!cityLower.isEmpty() && locLower.contains(cityLower)) {
                    emails.add(email);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] getUserEmailsByCity error: " + e.getMessage());
        }
        System.out.println("[DB] " + emails.size()
                + " user(s) matched for location: " + location);
        return emails;
    }

    /** Returns ALL user emails with email_alerts enabled. */
    public List<String> getAllUserEmails() {
        List<String> emails = new ArrayList<>();
        String sql = "SELECT email FROM users WHERE email_alerts = 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) emails.add(rs.getString("email"));
        } catch (SQLException e) {
            System.err.println("[DB] getAllUserEmails error: " + e.getMessage());
        }
        return emails;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) connect();
        } catch (SQLException e) {
            connect();
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            System.err.println("[DB] Close error: " + e.getMessage());
        }
    }
}