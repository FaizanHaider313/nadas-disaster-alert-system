package com.disaster.alert.service;

import com.disaster.alert.model.Alert;

/**
 * Factory that creates Alert objects from raw API data.
 * Pattern: Factory Method (GoF)
 */
public class AlertFactory {

    public enum AlertType { EARTHQUAKE, WEATHER, NEWS, FLOOD, FIRE, USER_REPORT }

    /** Creates a typed alert from raw values. */
    public static Alert create(AlertType type, String location,
                               double magnitude, String detail) {
        return switch (type) {
            case EARTHQUAKE -> createEarthquake(location, magnitude, detail);
            case WEATHER    -> createWeather(location, detail);
            case NEWS       -> createNews(location, detail);
            case FLOOD      -> createFlood(location, detail);
            case FIRE       -> createFire(location, detail);
            case USER_REPORT -> createUserReport(location, detail);
        };
    }

    private static Alert createEarthquake(String location, double mag, String detail) {
        String severity = mag >= 6.0 ? "High" : mag >= 4.0 ? "Medium" : "Low";
        String msg = String.format("Magnitude %.1f earthquake detected. %s", mag, detail);
        return new Alert("Earthquake", location, severity, msg, "USGS");
    }

    private static Alert createWeather(String location, String detail) {
        String severity = detail.toLowerCase().contains("storm") ||
                detail.toLowerCase().contains("cyclone") ? "High" : "Low";
        return new Alert("Weather", location, severity, "Weather condition: " + detail, "OpenWeatherMap");
    }

    private static Alert createNews(String location, String detail) {
        return new Alert("News", location, "Medium", detail, "NewsAPI");
    }

    private static Alert createFlood(String location, String detail) {
        return new Alert("Flood", location, "High", "Flood warning: " + detail, "System");
    }

    private static Alert createFire(String location, String detail) {
        return new Alert("Fire", location, "High", "Fire alert: " + detail, "NewsAPI");
    }

    private static Alert createUserReport(String location, String detail) {
        return new Alert("User Report", location, "Medium", detail, "Citizen Report");
    }
}