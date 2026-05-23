package com.disaster.alert.util;

import com.disaster.alert.model.User;

/**
 * Singleton — holds the currently logged-in user across all screens.
 * Pattern: Singleton (GoF)
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user)  { this.currentUser = user; }
    public void logout()          { this.currentUser = null; }
    public boolean isLoggedIn()   { return currentUser != null; }
    public User getCurrentUser()  { return currentUser; }
}