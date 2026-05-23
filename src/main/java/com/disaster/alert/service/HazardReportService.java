package com.disaster.alert.service;

import com.disaster.alert.db.DatabaseManager;
import com.disaster.alert.model.HazardReport;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages citizen hazard report persistence.
 * Pattern: Service Layer + Repository
 */
public class HazardReportService {

    private final DatabaseManager db = DatabaseManager.getInstance();

    /** Updates the status of a report (Pending → Reviewed → Resolved). */
    public boolean updateStatus(int reportId, String newStatus) {
        String sql = "UPDATE hazard_reports SET status=? WHERE id=?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, reportId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[HazardReportService] updateStatus error: " + e.getMessage());
            return false;
        }
    }

    /** Saves a new hazard report. Returns generated id, or -1 on failure. */
    public int saveReport(HazardReport report) {
        String sql = """
            INSERT INTO hazard_reports (user_id, hazard_type, location, description, photo_path)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, report.getUserId());
            ps.setString(2, report.getHazardType());
            ps.setString(3, report.getLocation());
            ps.setString(4, report.getDescription());
            ps.setString(5, report.getPhotoPath() != null ? report.getPhotoPath() : "");
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("[HazardReportService] saveReport error: " + e.getMessage());
        }
        return -1;
    }

    /** Returns all reports for a given user. */
    public List<HazardReport> getReportsByUser(int userId) {
        String sql = "SELECT * FROM hazard_reports WHERE user_id=? ORDER BY id DESC";
        List<HazardReport> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapReport(rs));
        } catch (SQLException e) {
            System.err.println("[HazardReportService] getByUser error: " + e.getMessage());
        }
        return list;
    }

    /** Returns all reports regardless of user (admin view). */
    public List<HazardReport> getAllReports() {
        List<HazardReport> list = new ArrayList<>();
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT * FROM hazard_reports ORDER BY id DESC")) {
            while (rs.next()) list.add(mapReport(rs));
        } catch (SQLException e) {
            System.err.println("[HazardReportService] getAllReports error: " + e.getMessage());
        }
        return list;
    }

    private HazardReport mapReport(ResultSet rs) throws SQLException {
        HazardReport r = new HazardReport();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setHazardType(rs.getString("hazard_type"));
        r.setLocation(rs.getString("location"));
        r.setDescription(rs.getString("description"));
        r.setPhotoPath(rs.getString("photo_path"));
        r.setStatus(rs.getString("status"));
        String ts = rs.getString("submitted_at");
        if (ts != null) {
            try { r.setSubmittedAt(LocalDateTime.parse(ts.replace(" ", "T"))); }
            catch (Exception ignored) {}
        }
        return r;
    }
}