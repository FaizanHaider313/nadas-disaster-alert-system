package com.disaster.alert.service;

import com.disaster.alert.db.DatabaseManager;
import com.disaster.alert.model.User;

import java.sql.*;

/**
 * Handles authentication and user account operations.
 * Pattern: Service Layer
 */
public class AuthService {

    private final DatabaseManager db = DatabaseManager.getInstance();

    /** Returns the User if credentials match, null otherwise. */
    public User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("[AuthService] login error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Registers a new user.
     * @return true on success, false if email already exists.
     */
    public boolean register(User user) {
        if (emailExists(user.getEmail())) return false;

        String sql = """
            INSERT INTO users (name, email, password, city, alert_types,
                               severity_level, email_alerts, emergency_only)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getName().trim());
            ps.setString(2, user.getEmail().trim().toLowerCase());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getCity() != null ? user.getCity() : "Islamabad");
            ps.setString(5, user.getAlertTypes() != null ? user.getAlertTypes() : "Weather,Earthquake");
            ps.setString(6, user.getSeverityLevel() != null ? user.getSeverityLevel() : "Low");
            ps.setInt(7, user.isEmailAlerts() ? 1 : 0);
            ps.setInt(8, user.isEmergencyOnly() ? 1 : 0);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[AuthService] register error: " + e.getMessage());
            return false;
        }
    }

    /** Updates profile and preferences for an existing user. */
    public boolean updateProfile(User user) {
        String sql = """
            UPDATE users SET name=?, city=?, alert_types=?,
            severity_level=?, email_alerts=?, emergency_only=?
            WHERE id=?
            """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getName().trim());
            ps.setString(2, user.getCity());
            ps.setString(3, user.getAlertTypes());
            ps.setString(4, user.getSeverityLevel());
            ps.setInt(5, user.isEmailAlerts() ? 1 : 0);
            ps.setInt(6, user.isEmergencyOnly() ? 1 : 0);
            ps.setInt(7, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AuthService] updateProfile error: " + e.getMessage());
            return false;
        }
    }

    /** Changes password after verifying the current one. */
    public boolean changePassword(int userId, String currentPwd, String newPwd) {
        String check = "SELECT id FROM users WHERE id=? AND password=?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(check)) {
            ps.setInt(1, userId);
            ps.setString(2, currentPwd);
            if (!ps.executeQuery().next()) return false;
        } catch (SQLException e) {
            return false;
        }
        String update = "UPDATE users SET password=? WHERE id=?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(update)) {
            ps.setString(1, newPwd);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email=?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setCity(rs.getString("city"));
        u.setAlertTypes(rs.getString("alert_types"));
        u.setSeverityLevel(rs.getString("severity_level"));
        u.setEmailAlerts(rs.getInt("email_alerts") == 1);
        u.setEmergencyOnly(rs.getInt("emergency_only") == 1);
        return u;
    }
}