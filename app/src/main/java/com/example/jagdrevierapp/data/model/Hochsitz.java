package com.example.jagdrevierapp.data.model;


import com.google.android.gms.maps.model.LatLng;

public class Hochsitz {

    private String hochsitzName;
    private double lat;
    private double lng;
    /*private LatLng gps;*/
    private boolean isBooked;
    private String bookedBy;
    private boolean isDamaged;
    private boolean isInsectious;

    public Hochsitz() {}

    public Hochsitz(String hochsitzName,double lat, double lng, boolean isBooked, String bookedBy, boolean isDamaged, boolean isInsectious) {
        this.hochsitzName = hochsitzName;
       /* this.gps = gps;*/
        this.lat = lat;
        this.lng = lng;
        this.isBooked = isBooked;
        this.bookedBy = bookedBy;
        this.isDamaged = isDamaged;
        this.isInsectious = isInsectious;
    }


    public String getHochsitzName() {
        return hochsitzName;
    }

    public void setHochsitzName(String hochsitzName) {
        this.hochsitzName = hochsitzName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lon) {
        this.lng = lng;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }

    public boolean isDamaged() {
        return isDamaged;
    }

    public void setDamaged(boolean damaged) {
        isDamaged = damaged;
    }

    public boolean isInsectious() {
        return isInsectious;
    }

    public void setInsectious(boolean insectious) {
        isInsectious = insectious;
    }

    /*public LatLng getGps() {return gps;}

    public void setGps(LatLng gps) { this.gps = gps; }*/



}

