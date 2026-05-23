package com.disaster.alert.controller;

import com.disaster.alert.model.Alert;
import com.disaster.alert.service.AlertService;
import com.disaster.alert.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AlertHistoryController implements Initializable {

    @FXML private TableView<Alert>            alertsTable;
    @FXML private TableColumn<Alert, String>  typeColumn;
    @FXML private TableColumn<Alert, String>  locationColumn;
    @FXML private TableColumn<Alert, String>  severityColumn;
    @FXML private TableColumn<Alert, String>  sourceColumn;
    @FXML private TableColumn<Alert, String>  timeColumn;

    @FXML private TextField       searchField;
    @FXML private ComboBox<String> typeFilterCombo;
    @FXML private ComboBox<String> severityFilterCombo;
    @FXML private Label           countLabel;
    @FXML private Button          backButton;

    private final AlertService alertService = new AlertService();
    private final ObservableList<Alert> alertsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Wire table columns
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        severityColumn.setCellValueFactory(c -> {
            // Show coloured severity text
            javafx.beans.property.SimpleStringProperty prop =
                    new javafx.beans.property.SimpleStringProperty(
                            c.getValue().getSeverityEmoji());
            return prop;
        });
        sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        timeColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getFormattedTime()));

        alertsTable.setItems(alertsList);

        // Filters
        typeFilterCombo.setItems(FXCollections.observableArrayList(
                "All Types", "Earthquake", "Weather", "Flood", "Fire", "News", "User Report"));
        typeFilterCombo.setValue("All Types");

        severityFilterCombo.setItems(FXCollections.observableArrayList(
                "All Severities", "High", "Medium", "Low"));
        severityFilterCombo.setValue("All Severities");

        loadAll();
    }

    private void loadAll() {
        List<Alert> all = alertService.getAllAlerts();
        alertsList.setAll(all);
        updateCount();
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadAll();
            return;
        }
        List<Alert> results = alertService.searchAlerts(keyword);
        alertsList.setAll(results);
        updateCount();
    }

    @FXML
    private void handleFilter() {
        String type     = typeFilterCombo.getValue();
        String severity = severityFilterCombo.getValue();

        List<Alert> base;

        if (!"All Types".equals(type)) {
            base = alertService.getAlertsByType(type);
        } else if (!"All Severities".equals(severity)) {
            base = alertService.getAlertsBySeverity(severity);
        } else {
            base = alertService.getAllAlerts();
        }

        // Apply secondary filter in memory if both are set
        if (!"All Types".equals(type) && !"All Severities".equals(severity)) {
            String sev = severity;
            base = base.stream()
                    .filter(a -> sev.equals(a.getSeverity()))
                    .toList();
        }

        alertsList.setAll(base);
        updateCount();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        typeFilterCombo.setValue("All Types");
        severityFilterCombo.setValue("All Severities");
        loadAll();
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo(backButton, "/fxml/Dashboard.fxml");
    }

    private void updateCount() {
        countLabel.setText("Showing " + alertsList.size() + " alert(s)");
    }
}