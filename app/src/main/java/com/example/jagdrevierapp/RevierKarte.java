package com.example.jagdrevierapp;

import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * In dieser Google-Map-Activity wird das aktuelle Jagrevier angezeigt.
 * In der aktuellen Version wird das Revier vom Entwickler vorgegeben.
 * Eine spätere Version sieht vor, dass der Pächter das Revier selber abstecken kann.
 * Damit die Map genutzt und angezeigt werden kann, muss ein API-Key generiert und in die XML unter
 * release/res/values/google_maps_api.xml eingefügt werden. Die XLM wurde beim Erstellen der Google-Map-Activity
 * automatisch angelegt und mit einer Anleitung zum Erstellen des API-Keys versehen.
 */

public class RevierKarte extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap jagdrevierMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revier_karte);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * WICHTIG: Das Endgerät muss Google Play Services installiert haben, damit die Activity genutzt werden kann.
     * Hier kann die Map manipuliert werden, sobald der API-Key registriert wurde..
     * Dieser Callback wird also ausgelöst, wenn die Map bereit zur Nutzung ist..
     * Das Jagdrevier wird fest über Koordinaten der Klasse LatLng verortet.
     * In diesem Fall wird der Mittelpunkt des Reviers abgesteckt.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        jagdrevierMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng revierMitte = new LatLng(48.849444, 11.241417);
        jagdrevierMap.addMarker(new MarkerOptions().position(revierMitte).title("Revier-Mittelpunkt"));
        jagdrevierMap.moveCamera(CameraUpdateFactory.newLatLng(revierMitte));
    }
}
