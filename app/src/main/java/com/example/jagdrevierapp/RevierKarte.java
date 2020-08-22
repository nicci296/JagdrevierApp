package com.example.jagdrevierapp;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;

import com.example.jagdrevierapp.data.model.Hochsitz;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

import java.util.List;
import java.util.Objects;


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

    //Keys
    private static final String TAG = "Revierkarte";
    private final String COLLECTION_KEY ="HochsitzeMichi";
    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_GREEN_ARGB = 0xff388E3C;
    private static final int POLYGON_STROKE_WIDTH_PX = 8;

    //Initialize FireStore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference dbHochsitze = db.collection(COLLECTION_KEY);

    //Object declaration
    private GoogleMap jagdrevierMap;

    //Attributes
    LatLng revierMitte = new LatLng(48.849444, 11.241417);
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Aufrufen der Mapview.
        setContentView(R.layout.activity_revier_karte);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.revier);
        mapFragment.getMapAsync(this);

        Button jgdeinAddBtn = findViewById(R.id.jgdeinAddBtn);
        Button jgdeinDelBtn = findViewById(R.id.jgdeinDelBtn);
        Button jgdeinDmgBtn = findViewById(R.id.jgdeinDmgBtn);
        Button showAll = (Button) findViewById(R.id.jgdeinShow);
    }

    //Polygon Styling
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
    }

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
        jagdrevierMap.setMyLocationEnabled(true);
        jagdrevierMap.setOnMyLocationButtonClickListener(this);
        jagdrevierMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        //Config der Karte - Typ, Zoom swipe&click, Compass
        jagdrevierMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
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

    public void onClickAddJgdEin (View v){

        final EditText jgdeinName = (EditText) findViewById(R.id.jgdeinNameInput);
        final String inputText = jgdeinName.getText().toString();

        //Toast, falls kein Name im Feld
        if(inputText.isEmpty()){
            Toast.makeText(RevierKarte.this, R.string.nameReq,Toast.LENGTH_LONG).show();
            return;
        }

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;

        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        final GeoPoint current = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        final double lat = current.getLatitude();
        final double lng = current.getLongitude();
        final LatLng latLng = new LatLng(lat,lng);

        /*final MarkerOptions currentLoc = new MarkerOptions()
                .position(current).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                .title(inputText);*/

        final MarkerOptions currentLoc = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                .title(inputText);


        Hochsitz kanzel = new Hochsitz
                (currentLoc.getTitle(),current, false,"TBA",false,
                        false);


        /*dbHochsitze.add(kanzel)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(RevierKarte.this, "Jagdeinrichtung hinzugefügt",
                                Toast.LENGTH_LONG).show();
                        jagdrevierMap.addMarker(currentLoc);
                        jagdrevierMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        jgdeinName.getText().clear();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                      Toast.makeText(RevierKarte.this, "Jagdeinrichtung konnte nicht hinzugefügt werden",
                              Toast.LENGTH_LONG).show();
                    }
                });*/

        dbHochsitze.document(inputText).set(kanzel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RevierKarte.this, "Jagdeinrichtung hinzugefügt",
                                Toast.LENGTH_LONG).show();
                        jagdrevierMap.addMarker(currentLoc);
                        jagdrevierMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        jgdeinName.getText().clear();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RevierKarte.this, "Jagdeinrichtung konnte nicht hinzugefügt werden",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void onClickShowAll(View v){


        dbHochsitze.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Hochsitz kanzel = document.toObject(Hochsitz.class);
                                if(kanzel.getGps() != null ){
                                    double lat = kanzel.getGps().getLatitude();
                                    double lng = kanzel.getGps().getLongitude();
                                    LatLng latLng = new LatLng(lat,lng);
                                    jagdrevierMap.addMarker(new MarkerOptions()
                                            .position(latLng)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                                            .title(kanzel.getHochsitzName()));
                                }
                            }
                            Toast.makeText
                                    (RevierKarte.this,R.string.data_Get_Success,Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText
                                    (RevierKarte.this,R.string.data_Get_Fail,Toast.LENGTH_LONG).show();
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }

                });

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
