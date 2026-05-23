package com.disaster.alert.controller;

import com.disaster.alert.model.HazardReport;
import com.disaster.alert.service.HazardReportService;
import com.disaster.alert.util.InputValidator;
import com.disaster.alert.util.SceneManager;
import com.disaster.alert.util.SessionManager;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class ReportHazardController implements Initializable {

    @FXML private ComboBox<String> hazardTypeCombo;
    @FXML private ComboBox<String> locationCombo;       // ← now a ComboBox
    @FXML private TextArea         descriptionArea;
    @FXML private Label            photoPathLabel;
    @FXML private Button           submitButton;
    @FXML private Button           backButton;
    @FXML private Label            statusLabel;

    private final HazardReportService reportService = new HazardReportService();
    private String selectedPhotoPath = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Hazard types
        hazardTypeCombo.setItems(FXCollections.observableArrayList(
                "Earthquake", "Flood", "Fire", "Storm", "Landslide",
                "Industrial Accident", "Infrastructure Damage", "Other"));
        hazardTypeCombo.setValue("Flood");

        // Pakistan cities — including Bhakkar and Gilgit as requested
        locationCombo.setItems(FXCollections.observableArrayList(
                // Major cities
                "Islamabad",
                "Rawalpindi",
                "Lahore",
                "Karachi",
                "Peshawar",
                "Quetta",
                "Multan",
                "Faisalabad",
                "Hyderabad",
                "Sialkot",
                "Gujranwala",
                "Abbottabad",
                "Murree",
                // Requested additions
                "Bhakkar",
                "Gilgit",
                // More cities
                "Bahawalpur",
                "Sargodha",
                "Sukkur",
                "Larkana",
                "Dera Ghazi Khan",
                "Gujrat",
                "Sahiwal",
                "Mirpur",
                "Muzaffarabad",
                "Mardan",
                "Swat",
                "Chitral",
                "Hunza",
                "Skardu",
                "Zhob",
                "Turbat",
                "Khuzdar",
                "Other"
        ));

        statusLabel.setVisible(false);
        photoPathLabel.setText("No photo selected");

        // Pre-fill city from logged-in user's profile
        SessionManager sm = SessionManager.getInstance();
        if (sm.isLoggedIn() && sm.getCurrentUser().getCity() != null) {
            String userCity = sm.getCurrentUser().getCity();
            // Set value whether or not it's in the list (editable ComboBox)
            locationCombo.setValue(userCity);
        }
    }

    @FXML
    private void handleChoosePhoto() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Select Photo Evidence");
        fc.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter(
                        "Images", "*.jpg", "*.jpeg", "*.png", "*.bmp"));
        java.io.File file = fc.showOpenDialog(submitButton.getScene().getWindow());
        if (file != null) {
            selectedPhotoPath = file.getAbsolutePath();
            photoPathLabel.setText(file.getName());
        }
    }

    @FXML
    private void handleSubmit() {
        statusLabel.setVisible(false);

        String hazardType  = hazardTypeCombo.getValue();
        // For editable ComboBox, getValue() returns typed text too
        String location    = locationCombo.getEditor() != null
                ? locationCombo.getEditor().getText().trim()
                : (locationCombo.getValue() != null
                ? locationCombo.getValue().trim() : "");
        String description = descriptionArea.getText().trim();

        if (!InputValidator.isNotEmpty(location, description)) {
            showError("Please fill in Location and Description.");
            return;
        }
        if (description.length() < 10) {
            showError("Description must be at least 10 characters.");
            return;
        }

        int userId = SessionManager.getInstance().isLoggedIn()
                ? SessionManager.getInstance().getCurrentUser().getId()
                : 0;

        HazardReport report = new HazardReport(
                userId, hazardType, location, description, selectedPhotoPath);
        int newId = reportService.saveReport(report);

        if (newId > 0) {
            showSuccessAnimation("Report submitted! ID #" + newId
                    + "  —  Pending admin review.");
            clearForm();
        } else {
            showError("Submission failed. Please try again.");
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
        statusLabel.setVisible(false);
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo(backButton, "/fxml/Dashboard.fxml");
    }

    // ── SUCCESS ANIMATION ────────────────────────────────────────────────

    private void showSuccessAnimation(String message) {
        statusLabel.setText("  \u2714   " + message);
        statusLabel.setStyle(
                "-fx-text-fill: #276749;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 13px;" +
                        "-fx-background-color: #f0fff4;" +
                        "-fx-border-color: #48bb78;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-padding: 12px 18px;"
        );

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#48bb78"));
        glow.setRadius(18);
        glow.setSpread(0.3);
        statusLabel.setEffect(glow);
        statusLabel.setVisible(true);
        statusLabel.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(350), statusLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition pop = new ScaleTransition(Duration.millis(250), statusLabel);
        pop.setFromX(0.92); pop.setFromY(0.92);
        pop.setToX(1.0);    pop.setToY(1.0);

        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(glow.radiusProperty(), 18)),
                new KeyFrame(Duration.millis(600),
                        new KeyValue(glow.radiusProperty(), 28)),
                new KeyFrame(Duration.millis(1200),
                        new KeyValue(glow.radiusProperty(), 18))
        );
        pulse.setCycleCount(3);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(600), statusLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            statusLabel.setVisible(false);
            statusLabel.setEffect(null);
        });

        fadeIn.play();
        pop.play();
        pulse.play();

        PauseTransition delay = new PauseTransition(Duration.millis(1500));
        delay.setOnFinished(e -> {
            SequentialTransition exit =
                    new SequentialTransition(new PauseTransition(Duration.seconds(3)), fadeOut);
            exit.play();
        });
        delay.play();
    }

    // ── ERROR DISPLAY ────────────────────────────────────────────────────

    private void showError(String msg) {
        statusLabel.setText("  \u2716   " + msg);
        statusLabel.setStyle(
                "-fx-text-fill: #c53030;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-color: #fff5f5;" +
                        "-fx-border-color: #fc8181;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-padding: 10px 16px;"
        );
        statusLabel.setEffect(null);
        statusLabel.setVisible(true);
        statusLabel.setOpacity(1);

        Timeline shake = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(statusLabel.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(60),
                        new KeyValue(statusLabel.translateXProperty(), -8)),
                new KeyFrame(Duration.millis(120),
                        new KeyValue(statusLabel.translateXProperty(), 8)),
                new KeyFrame(Duration.millis(180),
                        new KeyValue(statusLabel.translateXProperty(), -6)),
                new KeyFrame(Duration.millis(240),
                        new KeyValue(statusLabel.translateXProperty(), 6)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(statusLabel.translateXProperty(), 0))
        );
        shake.play();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────

    private void clearForm() {
        hazardTypeCombo.setValue("Flood");
        locationCombo.setValue(null);
        locationCombo.getEditor().clear();
        descriptionArea.clear();
        photoPathLabel.setText("No photo selected");
        selectedPhotoPath = "";

        // Re-fill city from user profile
        if (SessionManager.getInstance().isLoggedIn()) {
            String city = SessionManager.getInstance().getCurrentUser().getCity();
            if (city != null) locationCombo.setValue(city);
        }
    }
}