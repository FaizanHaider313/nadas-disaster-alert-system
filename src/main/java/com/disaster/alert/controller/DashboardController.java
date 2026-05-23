package com.disaster.alert.controller;

import com.disaster.alert.model.Alert;
import com.disaster.alert.model.HazardReport;
import com.disaster.alert.service.AlertFactory;
import com.disaster.alert.service.AlertService;
import com.disaster.alert.service.HazardReportService;
import com.disaster.alert.util.SceneManager;
import com.disaster.alert.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    private static final String WEATHER_API_KEY = "f708268fca7a4f63a97163511262004";
    private static final String EARTHQUAKE_URL  =
            "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson";

    // GDACS — European Commission + UN disaster alerts RSS feed
    private static final String GDACS_URL =
            "https://www.gdacs.org/xml/rss.xml";

    // ReliefWeb fallback
    private static final String RELIEFWEB_URL =
            "https://api.reliefweb.int/v1/reports?appname=nadas&limit=3";

    // Top bar
    @FXML private HBox   authButtons;
    @FXML private HBox   userInfo;
    @FXML private HBox   loggedInNav;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button logoutButton;
    @FXML private Label  userNameLabel;

    // Location banner
    @FXML private Label locationLabel;
    @FXML private Label locationSubLabel;

    // Info cards
    @FXML private Label weatherTemp;
    @FXML private Label weatherDesc;
    @FXML private Label weatherCity;
    @FXML private Label earthquakeMag;
    @FXML private Label earthquakeLocation;
    @FXML private Label earthquakeTime;
    @FXML private Label newsHeadline;
    @FXML private Label newsSource;

    // Map
    @FXML private WebView mapWebView;
    @FXML private Button  toggleEqButton;
    @FXML private Button  toggleHeatButton;
    @FXML private Button  toggleReportsButton;

    private WebEngine webEngine;
    private boolean   eqVisible      = true;
    private boolean   heatVisible    = true;
    private boolean   reportsVisible = true;

    // Approved reports table
    @FXML private TableView<HazardReport>           approvedReportsTable;
    @FXML private TableColumn<HazardReport, String> arTypeColumn;
    @FXML private TableColumn<HazardReport, String> arLocationColumn;
    @FXML private TableColumn<HazardReport, String> arDescColumn;
    @FXML private TableColumn<HazardReport, String> arTimeColumn;

    // API alerts table
    @FXML private TableView<AlertRow>          alertsTable;
    @FXML private TableColumn<AlertRow,String> typeColumn;
    @FXML private TableColumn<AlertRow,String> locationColumn;
    @FXML private TableColumn<AlertRow,String> severityColumn;
    @FXML private TableColumn<AlertRow,String> timeColumn;

    // Bottom
    @FXML private Label  statusLabel;
    @FXML private Label  lastUpdateLabel;
    @FXML private Button refreshButton;

    private final ObservableList<AlertRow>     alertsList          = FXCollections.observableArrayList();
    private final ObservableList<HazardReport> approvedReportsList = FXCollections.observableArrayList();
    private final AlertService        alertService  = new AlertService();
    private final HazardReportService reportService = new HazardReportService();

    @Override
    public void initialize(URL loc, ResourceBundle res) {
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        severityColumn.setCellValueFactory(new PropertyValueFactory<>("severity"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        alertsTable.setItems(alertsList);

        arTypeColumn.setCellValueFactory(new PropertyValueFactory<>("hazardType"));
        arLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        arDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        arTimeColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getFormattedTime()));
        approvedReportsTable.setItems(approvedReportsList);

        initMap();
        updateNavBar();
        loadApprovedReports();
        loadAllData();
    }

    // ── MAP ───────────────────────────────────────────────────────────────

    private void initMap() {
        webEngine = mapWebView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        URL mapUrl = getClass().getResource("/map/disaster_map.html");
        if (mapUrl != null) {
            webEngine.load(mapUrl.toExternalForm());
            webEngine.getLoadWorker().stateProperty().addListener(
                    (obs, oldState, newState) -> {
                        if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                            injectCitizenReportPins();
                        }
                    });
        } else {
            System.err.println("[Map] disaster_map.html not found in resources/map/");
        }
    }

    private void injectCitizenReportPins() {
        List<HazardReport> approved = reportService.getAllReports()
                .stream()
                .filter(r -> "Approved".equals(r.getStatus()))
                .toList();
        for (HazardReport r : approved) {
            double[] coords = getCityCoords(r.getLocation());
            if (coords != null) {
                String js = String.format(
                        "addCitizenReport(%f, %f, '%s', '%s', '%s', '%s');",
                        coords[0], coords[1],
                        escapeJs(r.getHazardType()),
                        escapeJs(r.getLocation()),
                        escapeJs(r.getDescription()),
                        escapeJs(r.getFormattedTime()));
                Platform.runLater(() -> webEngine.executeScript(js));
            }
        }
    }

    private double[] getCityCoords(String location) {
        if (location == null || location.isEmpty()) return null;
        String loc = location.toLowerCase();
        if (loc.contains("islamabad"))  return new double[]{33.6844, 73.0479};
        if (loc.contains("rawalpindi")) return new double[]{33.5651, 73.0169};
        if (loc.contains("lahore"))     return new double[]{31.5497, 74.3436};
        if (loc.contains("karachi"))    return new double[]{24.8607, 67.0011};
        if (loc.contains("peshawar"))   return new double[]{34.0151, 71.5249};
        if (loc.contains("quetta"))     return new double[]{30.1798, 66.9750};
        if (loc.contains("multan"))     return new double[]{30.1575, 71.5249};
        if (loc.contains("faisalabad")) return new double[]{31.4504, 73.1350};
        if (loc.contains("bhakkar"))    return new double[]{31.6343, 71.0647};
        if (loc.contains("gilgit"))     return new double[]{35.9208, 74.3087};
        if (loc.contains("swat"))       return new double[]{35.2227, 72.4258};
        if (loc.contains("hunza"))      return new double[]{36.3167, 74.6500};
        if (loc.contains("skardu"))     return new double[]{35.2971, 75.6333};
        if (loc.contains("murree"))     return new double[]{33.9078, 73.3931};
        return new double[]{30.3753, 69.3451};
    }

    private String escapeJs(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", " ")
                .replace("\r", "");
    }

    // ── MAP TOGGLES ───────────────────────────────────────────────────────

    @FXML private void handleToggleEarthquakes() {
        eqVisible = !eqVisible;
        webEngine.executeScript("toggleEarthquakes(" + eqVisible + ");");
        toggleEqButton.setOpacity(eqVisible ? 1.0 : 0.45);
    }

    @FXML private void handleToggleHeatmap() {
        heatVisible = !heatVisible;
        webEngine.executeScript("toggleHeatmap(" + heatVisible + ");");
        toggleHeatButton.setOpacity(heatVisible ? 1.0 : 0.45);
    }

    @FXML private void handleToggleReports() {
        reportsVisible = !reportsVisible;
        webEngine.executeScript("toggleReports(" + reportsVisible + ");");
        toggleReportsButton.setOpacity(reportsVisible ? 1.0 : 0.45);
    }

    // ── APPROVED REPORTS ─────────────────────────────────────────────────

    private void loadApprovedReports() {
        List<HazardReport> approved = reportService.getAllReports()
                .stream()
                .filter(r -> "Approved".equals(r.getStatus()))
                .toList();
        approvedReportsList.setAll(approved);
    }

    // ── NAV BAR ───────────────────────────────────────────────────────────

    private void updateNavBar() {
        boolean loggedIn = SessionManager.getInstance().isLoggedIn();
        authButtons.setVisible(!loggedIn);
        authButtons.setManaged(!loggedIn);
        userInfo.setVisible(loggedIn);
        userInfo.setManaged(loggedIn);
        loggedInNav.setVisible(loggedIn);
        loggedInNav.setManaged(loggedIn);
        if (loggedIn) {
            String name = SessionManager.getInstance().getCurrentUser().getName();
            String city = SessionManager.getInstance().getCurrentUser().getCity();
            userNameLabel.setText("Welcome, " + name + "!");
            locationLabel.setText(city != null ? city : "Islamabad");
            locationSubLabel.setText("Showing personalised alerts for your city");
        } else {
            locationLabel.setText("Showing Global Data");
            locationSubLabel.setText("Login to see personalised alerts for your city");
        }
    }

    // ── DATA LOADING ──────────────────────────────────────────────────────

    private void loadAllData() {
        statusLabel.setText("Loading data...");
        String city = SessionManager.getInstance().isLoggedIn()
                ? SessionManager.getInstance().getCurrentUser().getCity()
                : "Islamabad";
        new Thread(() -> loadWeatherData(city)).start();
        new Thread(this::loadEarthquakeData).start();
        new Thread(this::loadDisasterNews).start();
        updateLastUpdateTime();
    }

    private void loadWeatherData(String city) {
        try {
            String url = "https://api.weatherapi.com/v1/current.json?key="
                    + WEATHER_API_KEY + "&q="
                    + URLEncoder.encode(city, StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(makeApiCall(url));
            double temp = json.getJSONObject("current").getDouble("temp_c");
            String desc = json.getJSONObject("current")
                    .getJSONObject("condition").getString("text");
            String name = json.getJSONObject("location").getString("name");
            Platform.runLater(() -> {
                weatherTemp.setText("Temperature: " + String.format("%.1f", temp) + "°C");
                weatherDesc.setText("Condition: " + cap(desc));
                weatherCity.setText("Location: " + name);
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                weatherTemp.setText("Temperature: Unavailable");
                weatherDesc.setText("Check API key or network");
                weatherCity.setText("Location: --");
            });
        }
    }

    private void loadEarthquakeData() {
        try {
            JSONArray features = new JSONObject(makeApiCall(EARTHQUAKE_URL))
                    .getJSONArray("features");
            if (features.length() > 0) {
                JSONObject props = features.getJSONObject(0).getJSONObject("properties");
                double mag   = props.getDouble("mag");
                String place = props.getString("place");
                String ago   = timeAgo(props.getLong("time"));
                Platform.runLater(() -> {
                    earthquakeMag.setText("Magnitude: " + String.format("%.1f", mag));
                    earthquakeLocation.setText("Location: " + place);
                    earthquakeTime.setText("Time: " + ago);
                });
                for (int i = 0; i < Math.min(8, features.length()); i++) {
                    JSONObject p = features.getJSONObject(i).getJSONObject("properties");
                    double m   = p.getDouble("mag");
                    String loc = p.getString("place");
                    String sev = m >= 6 ? "High" : m >= 4 ? "Medium" : "Low";
                    String t   = timeAgo(p.getLong("time"));
                    Alert a = AlertFactory.create(AlertFactory.AlertType.EARTHQUAKE, loc, m, "");
                    alertService.saveAlert(a);
                    final String fs = sev, ft = t;
                    Platform.runLater(() ->
                            alertsList.add(new AlertRow("Earthquake", loc, fs, ft)));
                }
            }
        } catch (Exception e) {
            Platform.runLater(() -> {
                earthquakeMag.setText("Magnitude: Error");
                earthquakeLocation.setText("Failed to load");
                earthquakeTime.setText("--");
            });
        }
    }

    /**
     * Tries 3 sources in order:
     * 1. GDACS RSS (European Commission + UN) — XML parsing
     * 2. ReliefWeb JSON (UN OCHA)
     * 3. USGS earthquake feed used as disaster news fallback
     */
    private void loadDisasterNews() {
        // Try 1: GDACS RSS
        try {
            URL gdacsUrl = URI.create(GDACS_URL).toURL();
            HttpURLConnection conn = (HttpURLConnection) gdacsUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "NADAS/1.0");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);

            InputStream is = conn.getInputStream();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            doc.getDocumentElement().normalize();

            NodeList titles = doc.getElementsByTagName("title");
            NodeList descs  = doc.getElementsByTagName("description");

            // index 0 is channel title, index 1 is first item
            if (titles.getLength() > 1) {
                String headline = titles.item(1).getTextContent().trim();
                String detail   = descs.getLength() > 1
                        ? descs.item(1).getTextContent().trim() : "";

                // Clean HTML tags if any
                headline = headline.replaceAll("<[^>]+>", "").trim();
                detail   = detail.replaceAll("<[^>]+>", "").trim();

                final String fh = headline.isEmpty() ? detail : headline;
                Platform.runLater(() -> {
                    newsHeadline.setText(fh);
                    newsSource.setText("Source: GDACS  (European Commission + UN)");
                });

                // Save top items as alerts
                for (int i = 1; i < Math.min(titles.getLength(), 6); i++) {
                    String t = titles.item(i).getTextContent()
                            .replaceAll("<[^>]+>", "").trim();
                    if (!t.isEmpty()) {
                        alertService.saveAlert(
                                AlertFactory.create(AlertFactory.AlertType.NEWS,
                                        "Global", 0, t));
                    }
                }

                System.out.println("[GDACS] Loaded disaster alerts OK.");
                Platform.runLater(() -> statusLabel.setText("Connected"));
                return; // success — stop here
            }
        } catch (Exception e) {
            System.err.println("[GDACS] Failed: " + e.getMessage());
        }

        // Try 2: ReliefWeb JSON
        try {
            String response = makeApiCall(RELIEFWEB_URL);
            JSONObject root = new JSONObject(response);
            JSONArray items = root.optJSONArray("data");
            if (items != null && items.length() > 0) {
                String title = items.getJSONObject(0)
                        .getJSONObject("fields")
                        .optString("title", "Disaster report available");
                Platform.runLater(() -> {
                    newsHeadline.setText(title);
                    newsSource.setText("Source: UN ReliefWeb");
                });
                System.out.println("[ReliefWeb] Loaded OK.");
                Platform.runLater(() -> statusLabel.setText("Connected"));
                return;
            }
        } catch (Exception e) {
            System.err.println("[ReliefWeb] Failed: " + e.getMessage());
        }

        // Try 3: USGS fallback — show most significant earthquake as news
        try {
            String significantUrl =
                    "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/significant_week.geojson";
            JSONArray features = new JSONObject(makeApiCall(significantUrl))
                    .getJSONArray("features");
            if (features.length() > 0) {
                JSONObject p = features.getJSONObject(0).getJSONObject("properties");
                double mag   = p.getDouble("mag");
                String place = p.getString("place");
                String ago   = timeAgo(p.getLong("time"));
                String headline = "M" + String.format("%.1f", mag)
                        + " Earthquake — " + place + " (" + ago + ")";
                Platform.runLater(() -> {
                    newsHeadline.setText(headline);
                    newsSource.setText("Source: USGS Significant Earthquakes");
                });
                System.out.println("[USGS fallback] Loaded significant earthquake.");
            } else {
                Platform.runLater(() -> {
                    newsHeadline.setText("No significant earthquakes this week.");
                    newsSource.setText("Source: USGS Earthquake Hazards Program");
                });
            }
        } catch (Exception e) {
            Platform.runLater(() -> {
                newsHeadline.setText("Disaster news unavailable — check connection.");
                newsSource.setText("Source: GDACS / UN ReliefWeb / USGS");
            });
            System.err.println("[USGS fallback] Also failed: " + e.getMessage());
        }

        Platform.runLater(() -> statusLabel.setText("Connected"));
    }

    // ── FXML HANDLERS ─────────────────────────────────────────────────────

    @FXML private void handleRefresh() {
        alertsList.clear();
        loadApprovedReports();
        loadAllData();
        if (webEngine != null) injectCitizenReportPins();
    }

    @FXML private void handleLogin()    { SceneManager.switchTo(loginButton,    "/fxml/Login.fxml");      }
    @FXML private void handleRegister() { SceneManager.switchTo(registerButton, "/fxml/Register.fxml");   }
    @FXML private void handleLogout()   {
        SessionManager.getInstance().logout();
        SceneManager.switchTo(logoutButton, "/fxml/Dashboard.fxml");
    }
    @FXML private void handleSettings()     { SceneManager.switchTo(logoutButton, "/fxml/Settings.fxml");     }
    @FXML private void handleAlertHistory() { SceneManager.switchTo(logoutButton, "/fxml/AlertHistory.fxml"); }
    @FXML private void handleWeather() { SceneManager.switchTo(logoutButton, "/fxml/Weather.fxml"); }
    @FXML private void handleAdmin()        { SceneManager.switchTo(logoutButton, "/fxml/AdminLogin.fxml");   }
    @FXML private void handleReportHazard() { SceneManager.switchTo(logoutButton, "/fxml/ReportHazard.fxml"); }

    // ── HELPERS ───────────────────────────────────────────────────────────

    private String makeApiCall(String urlString) throws Exception {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "NADAS/1.0");
        conn.setConnectTimeout(7000);
        conn.setReadTimeout(7000);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) sb.append(line);
        in.close();
        return sb.toString();
    }

    private String timeAgo(long ms) {
        long diff = System.currentTimeMillis() - ms;
        long min  = diff / 60_000;
        long hr   = diff / 3_600_000;
        long day  = diff / 86_400_000;
        if (min < 60) return min + " min ago";
        if (hr  < 24) return hr  + " hours ago";
        return day + " days ago";
    }

    private String cap(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private void updateLastUpdateTime() {
        String t = new SimpleDateFormat("HH:mm:ss").format(new Date());
        Platform.runLater(() -> lastUpdateLabel.setText("Last updated: " + t));
    }

    public static class AlertRow {
        private final String type, location, severity, time;
        public AlertRow(String t, String l, String s, String tm) {
            type = t; location = l; severity = s; time = tm;
        }
        public String getType()     { return type; }
        public String getLocation() { return location; }
        public String getSeverity() { return severity; }
        public String getTime()     { return time; }
    }
}