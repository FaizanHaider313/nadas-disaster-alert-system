package com.disaster.alert.controller;

import com.disaster.alert.model.User;
import com.disaster.alert.service.AuthService;
import com.disaster.alert.util.InputValidator;
import com.disaster.alert.util.SceneManager;
import com.disaster.alert.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    // Profile fields
    @FXML private TextField     nameField;
    @FXML private Label         emailDisplay;
    @FXML private ComboBox<String> cityComboBox;

    // Alert preferences
    @FXML private CheckBox weatherCheckBox;
    @FXML private CheckBox earthquakeCheckBox;
    @FXML private CheckBox fireCheckBox;
    @FXML private CheckBox floodCheckBox;

    // Severity
    @FXML private RadioButton   severityLow;
    @FXML private RadioButton   severityMedium;
    @FXML private RadioButton   severityHigh;

    // Notifications
    @FXML private CheckBox      emailAlertsCheckBox;
    @FXML private CheckBox      emergencyOnlyCheckBox;

    // Password change
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    // Feedback
    @FXML private Label profileStatusLabel;
    @FXML private Label passwordStatusLabel;

    private final AuthService authService = new AuthService();
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cityComboBox.setItems(FXCollections.observableArrayList(
                "Islamabad", "Rawalpindi", "Karachi", "Lahore",
                "Faisalabad", "Multan", "Peshawar", "Quetta"));

        // Group severity radio buttons
        ToggleGroup severityGroup = new ToggleGroup();
        severityLow.setToggleGroup(severityGroup);
        severityMedium.setToggleGroup(severityGroup);
        severityHigh.setToggleGroup(severityGroup);

        profileStatusLabel.setVisible(false);
        passwordStatusLabel.setVisible(false);

        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) populateFields();
    }

    private void populateFields() {
        nameField.setText(currentUser.getName());
        emailDisplay.setText(currentUser.getEmail());
        cityComboBox.setValue(currentUser.getCity() != null ? currentUser.getCity() : "Islamabad");

        // Alert type checkboxes
        String types = currentUser.getAlertTypes() != null ? currentUser.getAlertTypes() : "";
        weatherCheckBox.setSelected(types.contains("Weather"));
        earthquakeCheckBox.setSelected(types.contains("Earthquake"));
        fireCheckBox.setSelected(types.contains("Fire"));
        floodCheckBox.setSelected(types.contains("Flood"));

        // Severity
        String sev = currentUser.getSeverityLevel();
        if ("Medium".equals(sev))      severityMedium.setSelected(true);
        else if ("High".equals(sev))   severityHigh.setSelected(true);
        else                           severityLow.setSelected(true);

        // Notification options
        emailAlertsCheckBox.setSelected(currentUser.isEmailAlerts());
        emergencyOnlyCheckBox.setSelected(currentUser.isEmergencyOnly());
    }

    @FXML
    private void handleSaveProfile() {
        profileStatusLabel.setVisible(false);

        if (!InputValidator.isNotEmpty(nameField.getText())) {
            showProfileStatus("Name cannot be empty.", false);
            return;
        }

        // Build alert types
        StringBuilder types = new StringBuilder();
        if (weatherCheckBox.isSelected())    types.append("Weather,");
        if (earthquakeCheckBox.isSelected()) types.append("Earthquake,");
        if (fireCheckBox.isSelected())       types.append("Fire,");
        if (floodCheckBox.isSelected())      types.append("Flood,");

        // Severity
        String severity = "Low";
        if (severityMedium.isSelected()) severity = "Medium";
        if (severityHigh.isSelected())   severity = "High";

        currentUser.setName(nameField.getText().trim());
        currentUser.setCity(cityComboBox.getValue());
        currentUser.setAlertTypes(types.toString().replaceAll(",$", ""));
        currentUser.setSeverityLevel(severity);
        currentUser.setEmailAlerts(emailAlertsCheckBox.isSelected());
        currentUser.setEmergencyOnly(emergencyOnlyCheckBox.isSelected());

        if (authService.updateProfile(currentUser)) {
            SessionManager.getInstance().login(currentUser); // refresh session
            showProfileStatus("Settings saved successfully!", true);
        } else {
            showProfileStatus("Failed to save settings. Please try again.", false);
        }
    }

    @FXML
    private void handleChangePassword() {
        passwordStatusLabel.setVisible(false);

        String current  = currentPasswordField.getText();
        String newPwd   = newPasswordField.getText();
        String confirm  = confirmPasswordField.getText();

        if (!InputValidator.isNotEmpty(current, newPwd, confirm)) {
            showPasswordStatus("Please fill in all password fields.", false);
            return;
        }
        if (!newPwd.equals(confirm)) {
            showPasswordStatus("New passwords do not match.", false);
            return;
        }
        if (!InputValidator.isValidPassword(newPwd)) {
            showPasswordStatus("Password must be at least 4 characters.", false);
            return;
        }

        if (authService.changePassword(currentUser.getId(), current, newPwd)) {
            currentUser.setPassword(newPwd);
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            showPasswordStatus("Password changed successfully!", true);
        } else {
            showPasswordStatus("Current password is incorrect.", false);
        }
    }

    @FXML
    private void handleResetDefaults() {
        weatherCheckBox.setSelected(true);
        earthquakeCheckBox.setSelected(true);
        fireCheckBox.setSelected(true);
        floodCheckBox.setSelected(true);
        severityLow.setSelected(true);
        emailAlertsCheckBox.setSelected(true);
        emergencyOnlyCheckBox.setSelected(false);
        showProfileStatus("Preferences reset to defaults. Click Save to apply.", true);
    }

    @FXML
    private void handleBackToDashboard() {
        SceneManager.switchTo(nameField, "/fxml/Dashboard.fxml");
    }

    private void showProfileStatus(String msg, boolean success) {
        profileStatusLabel.setText(msg);
        profileStatusLabel.setStyle(success
                ? "-fx-text-fill: #276749; -fx-font-weight: bold;"
                : "-fx-text-fill: #c53030; -fx-font-weight: bold;");
        profileStatusLabel.setVisible(true);
    }

    private void showPasswordStatus(String msg, boolean success) {
        passwordStatusLabel.setText(msg);
        passwordStatusLabel.setStyle(success
                ? "-fx-text-fill: #276749; -fx-font-weight: bold;"
                : "-fx-text-fill: #c53030; -fx-font-weight: bold;");
        passwordStatusLabel.setVisible(true);
    }
}