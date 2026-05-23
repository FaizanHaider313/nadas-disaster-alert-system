package com.disaster.alert.service;

import com.disaster.alert.db.DatabaseManager;
import com.disaster.alert.model.Alert;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages alert persistence (save incoming API alerts to DB, retrieve history).
 * Pattern: Service Layer + Repository
 */
public class AlertService {

    private final DatabaseManager db = DatabaseManager.getInstance();

    /** Saves an alert to the database. */
    public void saveAlert(Alert alert) {
        String sql = """
            INSERT INTO alerts (type, location, severity, message, source)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, alert.getType());
            ps.setString(2, alert.getLocation());
            ps.setString(3, alert.getSeverity());
            ps.setString(4, alert.getMessage());
            ps.setString(5, alert.getSource());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[AlertService] saveAlert error: " + e.getMessage());
        }
    }

    /** Returns all alerts, newest first. */
    public List<Alert> getAllAlerts() {
        return query("SELECT * FROM alerts ORDER BY id DESC");
    }

    /** Returns alerts filtered by type (case-insensitive). */
    public List<Alert> getAlertsByType(String type) {
        String sql = "SELECT * FROM alerts WHERE LOWER(type) LIKE ? ORDER BY id DESC";
        List<Alert> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, "%" + type.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapAlert(rs));
        } catch (SQLException e) {
            System.err.println("[AlertService] getByType error: " + e.getMessage());
        }
        return list;
    }

    /** Returns alerts filtered by severity. */
    public List<Alert> getAlertsBySeverity(String severity) {
        String sql = "SELECT * FROM alerts WHERE severity=? ORDER BY id DESC";
        List<Alert> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, severity);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapAlert(rs));
        } catch (SQLException e) {
            System.err.println("[AlertService] getBySeverity error: " + e.getMessage());
        }
        return list;
    }

    /** Searches alerts by location keyword. */
    public List<Alert> searchAlerts(String keyword) {
        String sql = """
            SELECT * FROM alerts
            WHERE LOWER(location) LIKE ? OR LOWER(message) LIKE ? OR LOWER(type) LIKE ?
            ORDER BY id DESC
            """;
        List<Alert> list = new ArrayList<>();
        String kw = "%" + keyword.toLowerCase() + "%";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapAlert(rs));
        } catch (SQLException e) {
            System.err.println("[AlertService] searchAlerts error: " + e.getMessage());
        }
        return list;
    }

    private List<Alert> query(String sql) {
        List<Alert> list = new ArrayList<>();
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapAlert(rs));
        } catch (SQLException e) {
            System.err.println("[AlertService] query error: " + e.getMessage());
        }
        return list;
    }

    private Alert mapAlert(ResultSet rs) throws SQLException {
        Alert a = new Alert();
        a.setId(rs.getInt("id"));
        a.setType(rs.getString("type"));
        a.setLocation(rs.getString("location"));
        a.setSeverity(rs.getString("severity"));
        a.setMessage(rs.getString("message"));
        a.setSource(rs.getString("source"));
        String ts = rs.getString("timestamp");
        if (ts != null) {
            try { a.setTimestamp(LocalDateTime.parse(ts.replace(" ", "T"))); }
            catch (Exception ignored) {}
        }
        return a;
    }
}