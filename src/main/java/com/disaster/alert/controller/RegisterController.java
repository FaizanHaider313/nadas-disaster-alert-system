package com.disaster.alert.controller;

import com.disaster.alert.model.User;
import com.disaster.alert.service.AuthService;
import com.disaster.alert.util.InputValidator;
import com.disaster.alert.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML private TextField     nameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> cityComboBox;
    @FXML private CheckBox      weatherCheckBox;
    @FXML private CheckBox      earthquakeCheckBox;
    @FXML private CheckBox      fireCheckBox;
    @FXML private CheckBox      floodCheckBox;
    @FXML private Button        createAccountButton;
    @FXML private Hyperlink     loginLink;
    @FXML private Label         errorLabel;
    @FXML private Label         successLabel;

    private final AuthService authService = new AuthService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cityComboBox.setItems(FXCollections.observableArrayList(
                "Islamabad", "Rawalpindi", "Karachi", "Lahore",
                "Faisalabad", "Multan", "Peshawar", "Quetta"));
        cityComboBox.setValue("Islamabad");
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }

    @FXML
    private void handleCreateAccount() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);

        String name     = nameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String city     = cityComboBox.getValue();

        // Validation
        if (!InputValidator.isNotEmpty(name, email, password)) {
            showError("Please fill in all required fields.");
            return;
        }
        if (!InputValidator.isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }
        if (!InputValidator.isValidPassword(password)) {
            showError("Password must be at least 4 characters.");
            return;
        }
        if (!weatherCheckBox.isSelected() && !earthquakeCheckBox.isSelected()
                && !fireCheckBox.isSelected() && !floodCheckBox.isSelected()) {
            showError("Please select at least one alert type.");
            return;
        }

        // Build alert types string
        StringBuilder alertTypes = new StringBuilder();
        if (weatherCheckBox.isSelected())    alertTypes.append("Weather,");
        if (earthquakeCheckBox.isSelected()) alertTypes.append("Earthquake,");
        if (fireCheckBox.isSelected())       alertTypes.append("Fire,");
        if (floodCheckBox.isSelected())      alertTypes.append("Flood,");
        String types = alertTypes.toString().replaceAll(",$", "");

        User user = new User(name, email, password, city, types, "Low", true, false);

        if (authService.register(user)) {
            successLabel.setText("Account created! Redirecting to login...");
            successLabel.setVisible(true);
            // Short delay then go to login
            new Thread(() -> {
                try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() ->
                        SceneManager.switchTo(createAccountButton, "/fxml/Login.fxml"));
            }).start();
        } else {
            showError("This email is already registered. Please login.");
        }
    }

    @FXML
    private void handleBackToLogin() {
        SceneManager.switchTo(loginLink, "/fxml/Login.fxml");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}