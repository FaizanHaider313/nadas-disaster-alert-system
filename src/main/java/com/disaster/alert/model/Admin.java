package com.disaster.alert.model;

import java.time.LocalDateTime;

/**
 * Admin entity — system administrators who manage thresholds and monitor the system.
 * Corresponds to Admin class in the NADAS class diagram.
 */
public class Admin {
    private int adminId;
    private String name;
    private String email;
    private String role;
    private LocalDateTime lastLogIn;

    public Admin() {}

    public Admin(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // Getters & setters
    public int getAdminId()                    { return adminId; }
    public void setAdminId(int id)             { this.adminId = id; }
    public String getName()                    { return name; }
    public void setName(String n)              { this.name = n; }
    public String getEmail()                   { return email; }
    public void setEmail(String e)             { this.email = e; }
    public String getRole()                    { return role; }
    public void setRole(String r)              { this.role = r; }
    public LocalDateTime getLastLogIn()        { return lastLogIn; }
    public void setLastLogIn(LocalDateTime t)  { this.lastLogIn = t; }
}