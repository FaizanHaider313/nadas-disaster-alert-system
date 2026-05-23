package com.disaster.alert.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Alert {
    private int id;
    private String type;       // Earthquake, Weather, Fire, Flood, News
    private String location;
    private String severity;   // Low, Medium, High, Critical
    private String message;
    private String source;     // USGS, OpenWeather, NewsAPI, User Report
    private LocalDateTime timestamp;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public Alert() { this.timestamp = LocalDateTime.now(); }

    public Alert(String type, String location, String severity,
                 String message, String source) {
        this.type = type;
        this.location = location;
        this.severity = severity;
        this.message = message;
        this.source = source;
        this.timestamp = LocalDateTime.now();
    }

    // Formatted time for TableView
    public String getFormattedTime() {
        return timestamp != null ? timestamp.format(FMT) : "--";
    }

    public String getSeverityEmoji() {
        return switch (severity != null ? severity : "") {
            case "High", "Critical" -> "🔴  " + severity;
            case "Medium"           -> "🟡  " + severity;
            default                 -> "🟢  " + severity;
        };
    }

    // Getters & setters
    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }
    public String getType()                  { return type; }
    public void setType(String type)         { this.type = type; }
    public String getLocation()              { return location; }
    public void setLocation(String l)        { this.location = l; }
    public String getSeverity()              { return severity; }
    public void setSeverity(String s)        { this.severity = s; }
    public String getMessage()               { return message; }
    public void setMessage(String m)         { this.message = m; }
    public String getSource()                { return source; }
    public void setSource(String s)          { this.source = s; }
    public LocalDateTime getTimestamp()      { return timestamp; }
    public void setTimestamp(LocalDateTime t){ this.timestamp = t; }
}