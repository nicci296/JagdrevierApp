package com.example.jagdrevierapp;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;

import com.example.jagdrevierapp.R;
import com.example.jagdrevierapp.data.model.Revier;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RevierSketch extends FragmentActivity implements OnMapReadyCallback {

    //Keys
    private static final String TAG = "RevierSketch";
    private final String COLLECTION_KEY = "Reviere";
    private static final String COLLECTION_HS_KEY ="Hochsitze";
    private static final String COLLECTION_US_KEY ="User";
    private static final String COLLECTION_REV_KEY="Reviere";
    private static final String COLLECTION_PA_KEY="Pachter";

    //Initialize Variables
    private GoogleMap mMap;
    private ImageButton drawBtn, clearBtn,saveBtn;
    private EditText revRef;

    Polygon polygon = null;
    List<LatLng> latLngList = new ArrayList<>();
    List<Marker> markerList = new ArrayList<>();
    ArrayList<GeoPoint> geoList = new ArrayList<>();
    LatLng start = new LatLng(48.854296, 11.239171);

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
    CollectionReference dbHochsitze;

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
            /*dbHochsitze = dbReviere.document(COLLECTION_REV_KEY).collection(COLLECTION_HS_KEY);*/
        }

        //Assign Variable

        drawBtn = findViewById(R.id.draw_button);
        clearBtn = findViewById(R.id.clear_button);
        saveBtn = findViewById(R.id.save_revier_button);
        revRef = findViewById(R.id.revier_ref);

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
                //no safe if no polygon drawn
                if(polygon == null)return;
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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Config der Karte - Typ Satellit, Zoom per Click&Touch, Compass auf Karte und Startposition
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 13));
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
}