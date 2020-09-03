package com.example.jagdrevierapp.data.model;




import com.google.firebase.firestore.GeoPoint;

public class Hochsitz {
    /**
     * ****************22.08.20 Nico *******************************************************************************
     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     *
     *Damit eine Hochsitz-Objekt korrekt in der DB mit Standort gespeichert und hinter effizient wieder abgerufen
     * werden kann, habe ich die doubles lat und lng durch einen GeoPoint ersetzt.
     * Ausporbiert habe ich auch die Datentypen LatLng, Marker sowie MarkerOptions, weil man mit diesen weniger Code
     * zum Speichern und Abrufen gebraucht hätte.
     * Problem bei diesen ist aber, dass diese Android-Klassen keinen leeren Constructor definieren, welcher aber von
     * Firebase gefordert wird, sonst gibt es einen Crash.
     *
     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */
    private String hochsitzName;
    private GeoPoint gps;
    private boolean isBooked;
    private String bookedBy;
    private boolean isDamaged;
    private boolean isInsectious;
    private String jagdeinrichtungType;

    public Hochsitz() {}

    public Hochsitz(String hochsitzName, GeoPoint gps, boolean isBooked, String bookedBy, boolean isDamaged,
                    boolean isInsectious, String jagdeinrichtungType) {
        this.hochsitzName = hochsitzName;
        this.gps = gps;
        this.isBooked = isBooked;
        this.bookedBy = bookedBy;
        this.isDamaged = isDamaged;
        this.isInsectious = isInsectious;
        this.jagdeinrichtungType = jagdeinrichtungType;
    }


    public String getHochsitzName() {
        return hochsitzName;
    }

    public void setHochsitzName(String hochsitzName) {
        this.hochsitzName = hochsitzName;
    }

    public GeoPoint getGps() {
        return gps;
    }

    public void setGps(GeoPoint gps) {
        this.gps = gps;
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

    public String getJagdeinrichtungType() { return jagdeinrichtungType;  }

    public void setJagdeinrichtungType(String jagdeinrichtungType) { this.jagdeinrichtungType = jagdeinrichtungType;  }
}

