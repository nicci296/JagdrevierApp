package com.example.jagdrevierapp;

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
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RevierSketch extends FragmentActivity implements OnMapReadyCallback {

    //Keys
    private static final String TAG = "RevierSketch";
    private final String COLLECTION_KEY = "Reviere";

    //Initialize Variables
    private GoogleMap mMap;
    private CheckBox checkBox;
    private ImageButton drawBtn, clearBtn,saveBtn;
    private EditText revRef;
    private Spinner polySpin;

    Polygon polygon = null;
    List<LatLng> latLngList = new ArrayList<>();
    List<Marker> markerList = new ArrayList<>();
    ArrayList<GeoPoint> geoList = new ArrayList<>();
    List<String> reviere = new ArrayList<>();
    LatLng start = new LatLng(48.854296, 11.239171);

    //Initialize FireStore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference dbReviere = db.collection(COLLECTION_KEY);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revier_sketch);


        //Assign Variable
        checkBox = findViewById(R.id.check_box);
        drawBtn = findViewById(R.id.draw_button);
        clearBtn = findViewById(R.id.clear_button);
        saveBtn = findViewById(R.id.save_revier_button);
        revRef = findViewById(R.id.revier_ref);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.sketchMap);
        mapFragment.getMapAsync(this);

        //CheckBox behaviour
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //Get State of CheckBox
                if(b){
                    if(polygon == null)return;;
                    //Fill Polygon with Color
                    polygon.setFillColor(Color.WHITE);
                }else{
                    //Unfill Polygon Color if unchecked
                    polygon.setFillColor(Color.TRANSPARENT);
                }
            }
        });

        drawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Draw Polyline on Map
                //Create PolygonOptions
                PolygonOptions polygonOptions = new PolygonOptions().addAll(latLngList).clickable(true);
                polygon = mMap.addPolygon(polygonOptions);
                //Polygon Stroke Color
                polygon.setStrokeColor(Color.WHITE);
                if(checkBox.isChecked())
                    //Fill Polygon with Color
                    polygon.setFillColor(Color.WHITE);
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
                checkBox.setChecked(false);
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
                        checkBox.setChecked(false);
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

        //populate spinner with docs from Revier-collection
        polySpin = findViewById(R.id.revier_spin);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, reviere);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        polySpin.setAdapter(adapter);

        dbReviere.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String subject = document.getString("revName");
                        reviere.add(subject);
                    }

                }adapter.notifyDataSetChanged();
            }
        });
        //Show polygon from Spinner-item on Map
        polySpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> polySpin, View view, final int position, long id) {
                final String selectedItem = polySpin.getItemAtPosition(position).toString();
                dbReviere.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(selectedItem.equals(document.getString("revName"))) {
                                    Revier revier = document.toObject(Revier.class);
                                    ArrayList<GeoPoint> points=(ArrayList<GeoPoint>) revier.getTierPoints();
                                    int length = points.size();
                                    if (length == 0) {
                                        return;
                                    }
                                    PolygonOptions poly = new PolygonOptions();
                                    poly.strokeColor(Color.WHITE);
                                    /*PolygonOptions poly = new PolygonOptions().clickable(true);*/
                                    for (int i = 0; i < length; i++) {
                                        GeoPoint polyGeo = (GeoPoint) points.get(i);
                                        double lat = polyGeo.getLatitude();
                                        double lng = polyGeo.getLongitude ();
                                        LatLng latLng = new LatLng(lat, lng);
                                        poly.add(latLng);
                                    }

                                    mMap.addPolygon(poly);
                                }
                            }

                        }
                    }
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> polySpin) {
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