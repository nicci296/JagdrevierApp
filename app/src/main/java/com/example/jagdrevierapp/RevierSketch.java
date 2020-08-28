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

public class RevierSketch extends FragmentActivity implements OnMapReadyCallback, SeekBar.OnSeekBarChangeListener {

    //Keys
    private static final String TAG = "RevierSketch";
    private final String COLLECTION_KEY = "Reviere";

    //Initialize Variables
    private GoogleMap mMap;
    private CheckBox checkBox;
    private SeekBar seekRed, seekGreen, seekBlue;
    private ImageButton drawBtn, clearBtn,saveBtn;
    private EditText revRef;

    Polygon polygon = null;
    List<LatLng> latLngList = new ArrayList<>();
    List<Marker> markerList = new ArrayList<>();
    List<GeoPoint> geoList = new ArrayList<>();

    int red=0,green=0,blue=0;

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
        seekRed = findViewById(R.id.seek_red);
        seekGreen = findViewById(R.id.seek_green);
        seekBlue = findViewById(R.id.seek_blue);
        drawBtn = findViewById(R.id.draw_button);
        clearBtn = findViewById(R.id.clear_button);
        saveBtn = findViewById(R.id.save_revier_button);
        revRef = findViewById(R.id.revier_ref);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
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
                    polygon.setFillColor(Color.rgb(red,green,blue));
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
                /*if(polygon == null)polygon.remove();*/
                //Create PolygonOptions
                PolygonOptions polygonOptions = new PolygonOptions().addAll(latLngList).clickable(true);
                polygon = mMap.addPolygon(polygonOptions);
                //Polygon Stroke Color
                polygon.setStrokeColor(Color.rgb(red,green,blue));
                if(checkBox.isChecked())
                    //Fill Polygon with Color
                    polygon.setFillColor(Color.rgb(red,green,blue));
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Clear All
                if(polygon != null)polygon.remove();
                for(Marker marker : markerList)marker.remove();
                checkBox.setChecked(false);
                seekRed.setProgress(0);
                seekGreen.setProgress(0);
                seekBlue.setProgress(0);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String revierRef = revRef.getText().toString();
                //no safe if no polygon drawn
                if(polygon == null)return;
                Revier newRevier = new Revier(polygon,geoList,revierRef);

                dbReviere.document(revierRef).set(newRevier).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Clear All
                        if(polygon != null)polygon.remove();
                        for(Marker marker : markerList)marker.remove();
                        checkBox.setChecked(false);
                        seekRed.setProgress(0);
                        seekGreen.setProgress(0);
                        seekBlue.setProgress(0);
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

        seekRed.setOnSeekBarChangeListener(this);
        seekGreen.setOnSeekBarChangeListener(this);
        seekBlue.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 13));
        //Config der Karte - Typ Satellit, Zoom per Click&Touch, Compass auf Karte
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

      /*  dbReviere.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    Revier revier = document.toObject(Revier.class);
                    List<GeoPoint> points = revier.getTierPoints();

                    for (GeoPoint geoPoint : points) {
                        LatLng newLatlng = new LatLng(geoPoint.getLatitude(),geoPoint.getLongitude());
                        List<LatLng> list = new ArrayList<>();
                        list.add(newLatlng);

                    }

                    PolygonOptions polygonOptions = new PolygonOptions().addAll(points).clickable(true);
                    Polygon polygon1 = mMap.addPolygon(polygonOptions);
                    polygon1.setStrokeColor(revier.getTier().getStrokeColor());
                    polygon1.setFillColor(revier.getTier().getFillColor());
                    }

                }
            }
        });
*/

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latlng) {
                //MarkerOptions
                MarkerOptions markerOptions = new MarkerOptions().position(latlng);
                //Marker
                Marker marker = mMap.addMarker(markerOptions);
                //Add Position and Marker to List
                latLngList.add(latlng);
                markerList.add(marker);
                GeoPoint geoPoint = new GeoPoint(latlng.latitude, latlng.longitude);
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (seekBar.getId()){
            case R.id.seek_red:
                red = i;
                break;
            case R.id.seek_green:
                green = i;
                break;
            case R.id.seek_blue:
                blue = i;
                break;
        }
        if(polygon != null) {
            //Polygon Stroke Color
            polygon.setStrokeColor(Color.rgb(red, green, blue));
            if (checkBox.isChecked())
                //Fill Polygon with Color
                polygon.setFillColor(Color.rgb(red, green, blue));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}