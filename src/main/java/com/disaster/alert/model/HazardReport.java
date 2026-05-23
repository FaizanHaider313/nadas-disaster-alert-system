package com.disaster.alert.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HazardReport {
    private int id;
    private int userId;
    private String hazardType;
    private String location;
    private String description;
    private String photoPath;
    private String status;     // Pending, Reviewed, Resolved
    private LocalDateTime submittedAt;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public HazardReport() {
        this.submittedAt = LocalDateTime.now();
        this.status = "Pending";
    }

    public HazardReport(int userId, String hazardType, String location,
                        String description, String photoPath) {
        this.userId = userId;
        this.hazardType = hazardType;
        this.location = location;
        this.description = description;
        this.photoPath = photoPath;
        this.submittedAt = LocalDateTime.now();
        this.status = "Pending";
    }

    public String getFormattedTime() {
        return submittedAt != null ? submittedAt.format(FMT) : "--";
    }

    // Getters & setters
    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }
    public int getUserId()                    { return userId; }
    public void setUserId(int uid)            { this.userId = uid; }
    public String getHazardType()             { return hazardType; }
    public void setHazardType(String h)       { this.hazardType = h; }
    public String getLocation()               { return location; }
    public void setLocation(String l)         { this.location = l; }
    public String getDescription()            { return description; }
    public void setDescription(String d)      { this.description = d; }
    public String getPhotoPath()              { return photoPath; }
    public void setPhotoPath(String p)        { this.photoPath = p; }
    public String getStatus()                 { return status; }
    public void setStatus(String s)           { this.status = s; }
    public LocalDateTime getSubmittedAt()     { return submittedAt; }
    public void setSubmittedAt(LocalDateTime t){ this.submittedAt = t; }
}