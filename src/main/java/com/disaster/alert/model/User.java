package com.disaster.alert.model;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private String city;
    private String alertTypes;   // comma-separated: Weather,Earthquake,Fire,Flood
    private String severityLevel; // Low / Medium / High
    private boolean emailAlerts;
    private boolean emergencyOnly;

    public User() {}

    public User(String name, String email, String password, String city,
                String alertTypes, String severityLevel,
                boolean emailAlerts, boolean emergencyOnly) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.city = city;
        this.alertTypes = alertTypes;
        this.severityLevel = severityLevel;
        this.emailAlerts = emailAlerts;
        this.emergencyOnly = emergencyOnly;
    }

    // Getters & setters
    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }
    public String getName()                 { return name; }
    public void setName(String name)        { this.name = name; }
    public String getEmail()                { return email; }
    public void setEmail(String email)      { this.email = email; }
    public String getPassword()             { return password; }
    public void setPassword(String p)       { this.password = p; }
    public String getCity()                 { return city; }
    public void setCity(String city)        { this.city = city; }
    public String getAlertTypes()           { return alertTypes; }
    public void setAlertTypes(String a)     { this.alertTypes = a; }
    public String getSeverityLevel()        { return severityLevel; }
    public void setSeverityLevel(String s)  { this.severityLevel = s; }
    public boolean isEmailAlerts()          { return emailAlerts; }
    public void setEmailAlerts(boolean e)   { this.emailAlerts = e; }
    public boolean isEmergencyOnly()        { return emergencyOnly; }
    public void setEmergencyOnly(boolean e) { this.emergencyOnly = e; }
}