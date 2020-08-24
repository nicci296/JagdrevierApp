package com.example.jagdrevierapp.data.model;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class Journal {

    /*private int index;*/
    private int shots;
    private int hits;
    private double caliber;
    private String mean;
    private String target;
    private String date;
    private GeoPoint location;

    public Journal() {
    }

    public Journal(int shots, int hits, double caliber, String mean, String target, String date, GeoPoint location) {
        /*this.index = index;*/
        this.shots = shots;
        this.hits = hits;
        this.caliber = caliber;
        this.mean = mean;
        this.target = target;
        this.date = date;
        this.location = location;
    }

   /* public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }*/

    public int getShots() {
        return shots;
    }

    public void setShots(int shots) {
        this.shots = shots;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public double getCaliber() {
        return caliber;
    }

    public void setCaliber(double caliber) {
        this.caliber = caliber;
    }

    public String getMean() {
        return mean;
    }

    public void setMean(String mean) {
        this.mean = mean;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }
}
