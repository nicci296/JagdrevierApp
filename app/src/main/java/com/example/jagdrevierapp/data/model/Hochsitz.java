package com.example.jagdrevierapp.data.model;



import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;

public class Hochsitz {

    private String hochsitzName;
    private double lat;
    private double lng;
    private MarkerOptions markerOpt;
    private boolean isBooked;
    private String bookedBy;
    private boolean isDamaged;
    private boolean isInsectious;
    private GeoPoint gps;



    public Hochsitz() {}

    public Hochsitz(String hochsitzName,GeoPoint gps, boolean isBooked, String bookedBy, boolean isDamaged, boolean isInsectious) {
        this.hochsitzName = hochsitzName;
       /* this.lat = lat;
        this.lng = lng;*/
        /*this.markerOpt = markerOpt;*/
        this.gps = gps;
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

   /* public double getLat() {
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
    }*/

    /*public MarkerOptions getMarkerOpt() {
        return markerOpt;
    }

    public void setMarkerOpt(MarkerOptions markerOpt) {
        this.markerOpt = markerOpt;
    }*/

    public GeoPoint getGps() { return gps; }

    public void setGps(GeoPoint gps) { this.gps = gps;}

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





}

