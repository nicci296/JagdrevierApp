package com.example.jagdrevierapp.data.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class Revier {

    private ArrayList<GeoPoint> tierPoints;
    private String revName;

    public Revier(){}

    public Revier(ArrayList<GeoPoint> tierPoints, String revName) {
        this.tierPoints = tierPoints;
        this.revName = revName;
    }

    public ArrayList<GeoPoint> getTierPoints() {
        return tierPoints;
    }

    public void setTierPoints(ArrayList<GeoPoint> tierPoints) {
        this.tierPoints = tierPoints;
    }

    public String getRevName() { return revName;
    }

    public void setRevName(String revName) { this.revName = revName;
    }
}
