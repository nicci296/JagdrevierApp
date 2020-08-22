package com.example.jagdrevierapp.data.model;




import com.google.firebase.firestore.GeoPoint;

public class Hochsitz {
    /**
     * Nico - 22.08.20
     *
     *Damit eine Hochsitz-Objekt korrekt in der DB mit Standort gespeichert und hinter effizient wieder abgerufen
     * werden kann, habe ich die doubles lat und lng durch einen GeoPoint ersetzt.
     * Ausporbiert habe ich auch die Datentypen LatLng, Marker sowie MarkerOptions, weil man mit diesen weniger Code
     * zum Speichern und Abrufen gebraucht hätte.
     * Problem bei diesen ist aber, dass diese Android-Klassen keinen leeren Constructor definieren, welcher aber von
     * Firebase gefordert wird, sonst gibt es einen Crash.
     */
    private String hochsitzName;
    private GeoPoint gps;
    private boolean isIsBooked;
    private String bookedBy;
    private boolean isIsDamaged;
    private boolean isIsInsectious;

    public Hochsitz() {}

    public Hochsitz(String hochsitzName,GeoPoint gps, boolean isIsBooked, String bookedBy, boolean isIsDamaged, boolean isIsInsectious) {
        this.hochsitzName = hochsitzName;
        this.gps = gps;
        this.isIsBooked = isIsBooked;
        this.bookedBy = bookedBy;
        this.isIsDamaged = isIsDamaged;
        this.isIsInsectious = isIsInsectious;
    }


    public String getHochsitzName() {
        return hochsitzName;
    }

    public void setHochsitzName(String hochsitzName) {
        this.hochsitzName = hochsitzName;
    }

    public GeoPoint getGps() { return gps; }

    public void setGps(GeoPoint gps) { this.gps = gps;}

    public boolean isIsBooked() {
        return isIsBooked;
    }

    public void setBooked(boolean booked) {
        isIsBooked = booked;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }

    public boolean isIsDamaged() {return isIsDamaged;    }

    public void setDamaged(boolean damaged) {
        isIsDamaged = damaged;
    }

    public boolean isIsInsectious() {
        return isIsInsectious;
    }

    public void setIsInsectious(boolean insectious) {
        isIsInsectious = insectious;
    }





}

