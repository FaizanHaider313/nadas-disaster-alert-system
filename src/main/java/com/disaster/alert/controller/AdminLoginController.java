package com.disaster.alert.controller;

import com.disaster.alert.db.DatabaseManager;
import com.disaster.alert.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Handles admin login screen.
 * Only allows access to Admin Dashboard if credentials match admins table.
 */
public class AdminLoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Label         errorLabel;

    @FXML
    private void handleAdminLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        // Basic empty check
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password.");
            return;
        }

        // Check credentials against admins table in DB
        if (isValidAdmin(email, password)) {
            // Credentials correct — go to Admin Dashboard
            SceneManager.switchTo(loginButton, "/fxml/Admin.fxml");
        } else {
            showError("Invalid admin credentials. Access denied.");
            passwordField.clear();
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo(loginButton, "/fxml/Dashboard.fxml");
    }

    private boolean isValidAdmin(String email, String password) {
        String sql = "SELECT id FROM admins WHERE email = ? AND password = ?";
        try (PreparedStatement ps =
                     DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // true if a matching admin row exists
        } catch (Exception e) {
            System.err.println("[AdminLogin] DB error: " + e.getMessage());
            return false;
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}