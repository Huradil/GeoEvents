package com.example.geoevents.database;

import java.util.SplittableRandom;

public class Event {
    private String title;
    private String description;
    private double latitude;
    private double longitude;
    private String priority;
    private String date;
    private String time;

    public Event() {

    }

    public Event(String title, String description, double latitude, double longitude, String priority,
                 String date, String time) {
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.priority = priority;
        this.date = date;
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public String getTitle() {
        return title;
    }
    public String getDate() {return date;}
    public String getTime() {return time;}

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public void setDate(String date) {this.date = date;}
    public void setTime(String time) {this.time = time;}
}
