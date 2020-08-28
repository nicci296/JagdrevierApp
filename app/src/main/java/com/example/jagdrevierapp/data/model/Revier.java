package com.example.jagdrevierapp.data.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class Revier {

    private Polygon tier;
    private List<GeoPoint> tierPoints;
    private String reference;

    public Revier(){}

    public Revier(Polygon tier, List<GeoPoint> tierPoints, String reference) {
        this.tier = tier;
        this.tierPoints = tierPoints;
    }

    public Polygon getTier() {
        return tier;
    }

    public void setTier(Polygon tier) {
        this.tier = tier;
    }

    public List<GeoPoint> getTierPoints() {
        return tierPoints;
    }

    public void setTierPoints(List<GeoPoint> tierPoints) {
        this.tierPoints = tierPoints;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
