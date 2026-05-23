package com.disaster.alert.controller;

import com.disaster.alert.db.DatabaseManager;
import com.disaster.alert.model.Alert;
import com.disaster.alert.model.HazardReport;
import com.disaster.alert.service.AlertService;
import com.disaster.alert.service.EmailService;
import com.disaster.alert.service.HazardReportService;
import com.disaster.alert.util.SceneManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import javafx.scene.image.ImageView;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML private Label totalAlertsLabel;
    @FXML private Label totalReportsLabel;
    @FXML private Label pendingReportsLabel;

    @FXML private TableView<HazardReport>            reportsTable;
    @FXML private TableColumn<HazardReport, Integer> idColumn;
    @FXML private TableColumn<HazardReport, String>  typeColumn;
    @FXML private TableColumn<HazardReport, String>  locationColumn;
    @FXML private TableColumn<HazardReport, String>  statusColumn;
    @FXML private TableColumn<HazardReport, String>  timeColumn;
    @FXML private TableColumn<HazardReport, String>  descColumn;

    @FXML private Label     statusLabel;
    @FXML private ImageView reportImageView;

    private final HazardReportService reportService = new HazardReportService();
    private final AlertService        alertService  = new AlertService();
    private final EmailService        emailService  = new EmailService();
    private final DatabaseManager     db            = DatabaseManager.getInstance();

    private final ObservableList<HazardReport> reportsList =
            FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("hazardType"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        timeColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getFormattedTime()));
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        reportsTable.setItems(reportsList);
        reportsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> handleReportSelected()
        );

        // Color-code rows by status — dark theme compatible
        reportsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(HazardReport r, boolean empty) {
                super.updateItem(r, empty);
                if (r == null || empty) { setStyle(""); return; }
                switch (r.getStatus()) {
                    case "Approved" ->
                            setStyle("-fx-background-color: rgba(72,187,120,0.10);");
                    case "Rejected" ->
                            setStyle("-fx-background-color: rgba(252,129,129,0.10);");
                    case "Pending"  ->
                            setStyle("-fx-background-color: rgba(236,201,75,0.10);");
                    default -> setStyle("");
                }
            }
        });

        loadStats();
        loadReports();
    }

    private void loadStats() {
        int alertCount = alertService.getAllAlerts().size();
        List<HazardReport> all = reportService.getAllReports();
        long pending = all.stream()
                .filter(r -> "Pending".equals(r.getStatus())).count();
        totalAlertsLabel.setText(String.valueOf(alertCount));
        totalReportsLabel.setText(String.valueOf(all.size()));
        pendingReportsLabel.setText(String.valueOf(pending));
    }

    private void loadReports() {
        reportsList.setAll(reportService.getAllReports());
    }

    // ── APPROVE ───────────────────────────────────────────────────
    @FXML
    private void handleApprove() {
        HazardReport selected = reportsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Please select a report first.", false);
            return;
        }
        if ("Approved".equals(selected.getStatus())) {
            showStatus("Report #" + selected.getId()
                    + " is already approved.", false);
            return;
        }

        boolean updated = reportService.updateStatus(selected.getId(), "Approved");
        if (!updated) {
            showStatus("Failed to update report status in database.", false);
            return;
        }

        Alert officialAlert = new Alert();
        officialAlert.setType("🚨 " + selected.getHazardType());
        officialAlert.setLocation(selected.getLocation());
        officialAlert.setSeverity("High");
        officialAlert.setMessage("Citizen Report: " + selected.getDescription());
        officialAlert.setSource("Approved Citizen Report #" + selected.getId());
        alertService.saveAlert(officialAlert);

        selected.setStatus("Approved");
        reportsTable.refresh();
        loadStats();

        showStatus("✅ Approved! Sending email alerts to users in "
                + selected.getLocation() + "...", true);

        final String hazardType  = selected.getHazardType();
        final String location    = selected.getLocation();
        final String description = selected.getDescription();
        final String photoPath   = selected.getPhotoPath();

        new Thread(() -> {
            List<String> emails = db.getUserEmailsByCity(location);
            if (emails.isEmpty()) {
                System.out.println("[Admin] No city match — sending to all users.");
                emails = db.getAllUserEmails();
            }
            emailService.sendAlertToUsers(emails, hazardType,
                    location, description, photoPath);
            final int count = emails.size();
            Platform.runLater(() ->
                    showStatus("✅ Report #" + selected.getId()
                            + " approved! Email sent to " + count
                            + " user(s) in " + location
                            + ". Alert added to Dashboard feed.", true));
        }).start();
    }

    // ── REJECT ────────────────────────────────────────────────────
    @FXML
    private void handleReject() {
        HazardReport selected = reportsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Please select a report first.", false);
            return;
        }
        if ("Rejected".equals(selected.getStatus())) {
            showStatus("Report #" + selected.getId()
                    + " is already rejected.", false);
            return;
        }

        boolean ok = reportService.updateStatus(selected.getId(), "Rejected");
        if (ok) {
            selected.setStatus("Rejected");
            reportsTable.refresh();
            loadStats();
            showStatus("❌ Report #" + selected.getId()
                    + " rejected. No alert issued.", false);
        } else {
            showStatus("Failed to reject report.", false);
        }
    }

    // ── REFRESH ───────────────────────────────────────────────────
    @FXML
    private void handleRefresh() {
        loadStats();
        loadReports();
        showStatus("Data refreshed.", true);
    }

    @FXML
    private void handleBackToDashboard() {
        SceneManager.switchTo(totalAlertsLabel, "/fxml/Dashboard.fxml");
    }

    private void showStatus(String msg, boolean success) {
        statusLabel.setText(msg);
        statusLabel.setStyle(success
                ? "-fx-text-fill: #6bffb8; -fx-font-weight: bold;"
                : "-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
        statusLabel.setVisible(true);
    }

    @FXML
    private void handleReportSelected() {
        HazardReport selected = reportsTable.getSelectionModel().getSelectedItem();
        if (selected == null || reportImageView == null) return;
        String path = selected.getPhotoPath();
        if (path != null && !path.isEmpty()) {
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                reportImageView.setImage(
                        new javafx.scene.image.Image(f.toURI().toString()));
                reportImageView.setVisible(true);
                return;
            }
        }
        reportImageView.setVisible(false);
    }
}