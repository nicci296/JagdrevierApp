package com.example.jagdrevierapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;


import com.example.jagdrevierapp.data.PermissionUtilsSketch;
import com.example.jagdrevierapp.data.model.Revier;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class RevierSketch extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    //Keys
    private static final String TAG = "RevierSketch";
    private static final String COLLECTION_REV_KEY="Reviere";
    private static final String COLLECTION_PA_KEY="Pachter";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;

    //Initialize Variables
    private GoogleMap mMap;
    private ImageButton drawBtn, clearBtn,saveBtn,bckToMap;
    private EditText revRef;

    Polygon polygon = null;
    List<LatLng> latLngList = new ArrayList<>();
    List<Marker> markerList = new ArrayList<>();
    ArrayList<GeoPoint> geoList = new ArrayList<>();

    //##########################################################
    //###    Firebase - Authentication
    //##########################################################
    //Initialize Firebase Auth
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
    //##########################################################
    //###    Firebase - Firestore
    //##########################################################
    //Initialize FireStore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference dbPachter = db.collection(COLLECTION_PA_KEY);
    CollectionReference dbReviere;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revier_sketch);

        //##########################################################
        //###    User-Validation
        //##########################################################
        if (mFirebaseUser == null) {
            //Nicht eingeloggt, SignIn-Activity wird gestartet
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            //general variables
            String mUsername = mFirebaseUser.getDisplayName();
            dbReviere = dbPachter.document(mFirebaseUser.getEmail()).collection(COLLECTION_REV_KEY);
        }

        //Assign Variable

        drawBtn = findViewById(R.id.draw_button);
        clearBtn = findViewById(R.id.clear_button);
        saveBtn = findViewById(R.id.save_revier_button);
        revRef = findViewById(R.id.revier_ref);
        bckToMap = findViewById(R.id.backTo_map_button);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.sketchMap);
        mapFragment.getMapAsync(this);


        drawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Draw Polyline on Map
                //Create PolygonOptions
                PolygonOptions polygonOptions = new PolygonOptions().addAll(latLngList).clickable(true);
                polygon = mMap.addPolygon(polygonOptions);
                //Polygon Stroke Color
                polygon.setStrokeColor(Color.WHITE);

            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Clear All
                if(polygon != null)polygon.remove();
                for(Marker marker : markerList)marker.remove();
                latLngList.clear();
                geoList.clear();
                mMap.clear();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String revierRef = revRef.getText().toString();
                if(revierRef.isEmpty()){
                    Toast.makeText(RevierSketch.this, R.string.rev_name_req, Toast.LENGTH_SHORT).show();
                    return;
                }
                //no safe if no polygon drawn
                if(polygon == null){
                    Toast.makeText(RevierSketch.this, R.string.poly_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                Revier newRevier = new Revier(geoList,revierRef);

                dbReviere.document(revierRef).set(newRevier).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Clear All
                        if(polygon != null)polygon.remove();
                        for(Marker marker : markerList)marker.remove();
                        latLngList.clear();
                        geoList.clear();
                        mMap.clear();
                        Toast.makeText(RevierSketch.this, "Revier gespeichert", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        });

        bckToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backIntent = new Intent(RevierSketch.this,RevierKarte.class);
                startActivity(backIntent);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        /*
         * Initialisierung eines LocationManagers zur aktuellen Standortbestimmung.
         * In einem GeoPoint wird der aktuelle Standort gespeichert, welcher in der Firebase gespeichert werden kann.
         * Damit hinterher ein Marker mit diesen Koordinaten gesetzt werden kann, wird zusätzlich ein LatLng-Objekt
         * definiert, welches die Koordinaten aus dem GeoPoint bezieht. Es sowohl ein GeoPoint als auch ein LatLng
         * Objekt geben, da Firestore nur GeoPoint, aber nicht LatLng wieder auslesen kann und umgekehrt die Google-
         * Maps Logik nur mit LatLng-Koordinaten arbeiten kann.
         * Marker werden also über LatLng lokalisiert, in der Datenbank liegen die Koordinaten aber als GeoPoint.
         */
        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        final String locationProvider = LocationManager.NETWORK_PROVIDER;
        //Koordinaten der aktuellen GPS-Position erhalten
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        final LatLng current = new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());

        //Config der Karte - Typ Satellit, Zoom per Click&Touch, Compass auf Karte und Startposition
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 13));
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);



        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latlng) {
                /*if(polygon != null)polygon.remove();*/
                //MarkerOptions
                MarkerOptions markerOptions = new MarkerOptions().position(latlng);
                //Marker
                Marker marker = mMap.addMarker(markerOptions);
                //Add Position and Marker to List
                latLngList.add(latlng);
                markerList.add(marker);
                GeoPoint geoPoint = new GeoPoint(latlng.latitude, latlng.longitude);
                geoList.add(geoPoint);
            }
        });
    }
//---------------------copy pasta der Permissions aus der Google API-----------------------------------------------

    /**
     * *******************************18.08.20 Nico****************************************************************
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * Damit die folgenden Methoden genutzt werden können, wird die PermissionUtils-Class benötigt.
     * Diese kann selbst implementiert werden.
     * Für den Projektumfang wurde der Klassen-Code aus der Google API von GitHub übernommen
     * https://github.com/googlemaps/android-samples/blob/master/ApiDemos/java/app/src/gms/java/com/example/mapdemo/PermissionUtils.java
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    /**
     * Enables die My Location Funktion bzw. das Berechtigungslevel zur Bestimmung des aktuellen Standorts, wenn im
     * AndroidManifest die fine-permission erteilt wurde.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtilsSketch.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
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

        if (PermissionUtilsSketch.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
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
        PermissionUtilsSketch.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
}
