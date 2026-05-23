package com.disaster.alert.controller;

import com.disaster.alert.db.DatabaseManager;
import com.disaster.alert.model.User;
import com.disaster.alert.service.AuthService;
import com.disaster.alert.util.InputValidator;
import com.disaster.alert.util.SceneManager;
import com.disaster.alert.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Label         errorLabel;
    @FXML private Hyperlink     registerLink;

    private final AuthService    authService = new AuthService();
    private final DatabaseManager db         = DatabaseManager.getInstance();

    @FXML
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (!InputValidator.isNotEmpty(email, password)) {
            showError("Please fill in all fields.");
            return;
        }
        if (!InputValidator.isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        // ✅ Check admin credentials FIRST
        String adminName = db.verifyAdmin(email, password);
        if (adminName != null) {
            System.out.println("[Login] Admin access granted: " + adminName);
            SceneManager.switchTo(loginButton, "/fxml/Admin.fxml");
            return;
        }

        // Regular user login
        User user = authService.login(email, password);
        if (user == null) {
            showError("Incorrect email or password.");
            passwordField.clear();
            return;
        }

        // Store session and go to dashboard
        SessionManager.getInstance().login(user);
        SceneManager.switchTo(loginButton, "/fxml/Dashboard.fxml");
    }

    @FXML
    private void handleRegisterLink() {
        SceneManager.switchTo(registerLink, "/fxml/Register.fxml");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}