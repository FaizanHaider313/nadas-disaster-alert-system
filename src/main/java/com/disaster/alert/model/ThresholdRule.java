package com.disaster.alert.model;

/**
 * ThresholdRule — defines when an automatic alert should be triggered.
 * e.g. Earthquake magnitude >= 5.0 => "High" alert.
 * Corresponds to ThresholdRule class in the NADAS class diagram.
 * Pattern: Strategy (values define detection strategy)
 */
public class ThresholdRule {
    private int ruleId;
    private String hazardType;      // Earthquake, Flood, Weather, Fire
    private double magnitudeLimit;  // e.g. 5.0 for earthquake
    private double rainfallLimit;   // mm/hr
    private double tempLimit;       // °C
    private LocalDateTimeHolder updatedAt;
    private boolean isActive;

    public ThresholdRule() { this.isActive = true; }

    public ThresholdRule(String hazardType, double magnitudeLimit,
                         double rainfallLimit, double tempLimit) {
        this.hazardType     = hazardType;
        this.magnitudeLimit = magnitudeLimit;
        this.rainfallLimit  = rainfallLimit;
        this.tempLimit      = tempLimit;
        this.isActive       = true;
    }

    /** Returns true if this rule is triggered by the given magnitude. */
    public boolean isTriggeredByMagnitude(double magnitude) {
        return isActive && magnitude >= magnitudeLimit;
    }

    // Getters & setters
    public int getRuleId()                      { return ruleId; }
    public void setRuleId(int id)               { this.ruleId = id; }
    public String getHazardType()               { return hazardType; }
    public void setHazardType(String h)         { this.hazardType = h; }
    public double getMagnitudeLimit()           { return magnitudeLimit; }
    public void setMagnitudeLimit(double m)     { this.magnitudeLimit = m; }
    public double getRainfallLimit()            { return rainfallLimit; }
    public void setRainfallLimit(double r)      { this.rainfallLimit = r; }
    public double getTempLimit()                { return tempLimit; }
    public void setTempLimit(double t)          { this.tempLimit = t; }
    public boolean isActive()                   { return isActive; }
    public void setActive(boolean a)            { this.isActive = a; }

    /** Inner helper — avoids import dependency on java.time in simple model. */
    public static class LocalDateTimeHolder {
        public String value;
        public LocalDateTimeHolder(String v) { this.value = v; }
    }
}