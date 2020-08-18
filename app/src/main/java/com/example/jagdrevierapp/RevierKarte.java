package com.example.jagdrevierapp;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;




import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

import java.util.List;


/**
 * In dieser Google-Map-Activity wird das aktuelle Jagrevier angezeigt.
 * In der aktuellen Version wird das Revier vom Entwickler vorgegeben.
 * Eine spätere Version sieht vor, dass der Pächter das Revier selber abstecken kann.
 * Damit die Map genutzt und angezeigt werden kann, muss ein API-Key generiert und in die XML unter
 * release/res/values/google_maps_api.xml eingefügt werden. Die XLM wurde beim Erstellen der Google-Map-Activity
 * automatisch angelegt und mit einer Anleitung zum Erstellen des API-Keys versehen.
 */

public class RevierKarte extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback{

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean permissionDenied = false;

    private GoogleMap jagdrevierMap;
    LatLng revierMitte = new LatLng(48.849444, 11.241417);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Aufrufen der Mapview.
        setContentView(R.layout.activity_revier_karte);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.revier);
        mapFragment.getMapAsync(this);
    }

    //Polygon Styling
    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_GREEN_ARGB = 0xff388E3C;
    private static final int POLYGON_STROKE_WIDTH_PX = 8;

    private void stylePolygon(Polygon revierGrenze){
        String type = "";
        if(revierGrenze.getTag() != null){
            type = revierGrenze.getTag().toString();
        }
        int strokeColor = COLOR_GREEN_ARGB;
        /*int fillColor = COLOR_WHITE_ARGB;*/

        revierGrenze.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
        revierGrenze.setStrokeColor(strokeColor);
        /*revierGrenze.setFillColor(fillColor);*/

    };


    /**
     * WICHTIG: Das Endgerät muss Google Play Services installiert haben, damit die Activity genutzt werden kann.
     * Dieser Callback wird also ausgelöst, wenn die Map bereit zur Nutzung ist..
     *
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        jagdrevierMap = googleMap;
        jagdrevierMap.setOnMyLocationButtonClickListener(this);
        jagdrevierMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        //Config der Karte - Typ, Zoom swipe&click, Compass
        jagdrevierMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        jagdrevierMap.getUiSettings().setZoomControlsEnabled(true);
        jagdrevierMap.getUiSettings().setZoomGesturesEnabled(true);
        jagdrevierMap.getUiSettings().setCompassEnabled(true);

        //Polygon zum Abstecken des Reviers
        Polygon revierGrenze = googleMap.addPolygon(new PolygonOptions()
                .clickable(true)
                .add(
                        new LatLng(48.854938, 11.241890),
                        new LatLng(48.849355, 11.255418),
                        new LatLng(48.844572, 11.240600),
                        new LatLng(48.850002, 11.225756)));
        revierGrenze.setTag("Revier");
        stylePolygon(revierGrenze);


        //Kamera zur RevierMitte bewegen und mit Faktor 12 reinzoomen
        /*jagdrevierMap.animateCamera(CameraUpdateFactory.zoomTo(12),2000,null);*/
        jagdrevierMap.moveCamera(CameraUpdateFactory.newLatLngZoom(revierMitte, 14));

        // Marker in Reviermitte setzen
        jagdrevierMap.addMarker(new MarkerOptions().position(revierMitte).title("Revier-Mittelpunkt"));
        jagdrevierMap.moveCamera(CameraUpdateFactory.newLatLng(revierMitte));
    }





    //---------------------copy pasta der Permissions aus der Google API-----------------------------------------------

    /**
     * Damit die folgenden Methoden genutzt werden können, wird die PermissionUtils-Class benötigt.
     * Diese kann selbst implementiert werden.
     * Für den Projektumfang wurde der Klassen-Code aus der Google API von GitHub übernommen
     * https://github.com/googlemaps/android-samples/blob/master/ApiDemos/java/app/src/gms/java/com/example/mapdemo/PermissionUtils.java
     */

    /**
     * Enables die My Location Funktion bzw. das Berechtigungslevel zur Bestimmung des aktuellen Standorts, wenn im
     * AndroidManifest die fine-permission erteilt wurde.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (jagdrevierMap != null) {
                jagdrevierMap.setMyLocationEnabled(true);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    /**
     * Auf der Karte kann der "Mein Standort anzeigen"-Button genutzt werden
     */
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    /**
     *Beim Klick auf die eigene Location wird ein Toast mit den aktuellen Koordinaten ausgegeben.
     */
    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
      PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
}
