package com.example.jagdrevierapp;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

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
        ActivityCompat.OnRequestPermissionsResultCallback {

    //Keys
    private static final String TAG = "Revierkarte";
    private final String COLLECTION_KEY = "HochsitzeMichi";
    /*private static final int COLOR_WHITE_ARGB = 0xffffffff;*/
    private static final int COLOR_GREEN_ARGB = 0xff388E3C;
    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    //Initialize FireStore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference dbHochsitze = db.collection(COLLECTION_KEY);

    //Map-Object declaration
    private GoogleMap jagdrevierMap;

    //Attributes
    LatLng revierMitte = new LatLng(48.849444, 11.241417);
    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean permissionDenied = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //##########################################################
        //###    Firebase - Authentication
        //##########################################################
        //Initialize Firebase Auth
        //Firebase instance variables
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();

        //Auskommentiert, damit Login-Funktion nicht stört --> Login funktioniert momentan nicht

        /*if (mFirebaseUser == null) {
            //Nicht eingeloggt, SignIn-Activity wird gestartet
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            //general variables
            String mUsername = mFirebaseUser.getDisplayName();
        }*/



        // Festlegen der Mapview.
        setContentView(R.layout.activity_revier_karte);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.revier);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    //Polygon Styling zur Erstellung eine Polygon im der onMapReady-Callback
    private void stylePolygon(Polygon revierGrenze) {
        /*String type = "";*/
        /*if(revierGrenze.getTag() != null){
            type = revierGrenze.getTag().toString();
        }*/
        /*int fillColor = COLOR_WHITE_ARGB;*/

        revierGrenze.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
        revierGrenze.setStrokeColor(COLOR_GREEN_ARGB);
        /*revierGrenze.setFillColor(COLOR_WHITE_ARGB);*/
    }

    /**
     * WICHTIG: Das Endgerät muss Google Play Services installiert haben, damit die Activity genutzt werden kann.
     * Dieser Callback wird nur ausgelöst, wenn die Map bereit zur Nutzung ist..
     *
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Initialisierung der Map und Standortbestimmung
        jagdrevierMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        jagdrevierMap.setMyLocationEnabled(true);
        jagdrevierMap.setOnMyLocationButtonClickListener(this);
        jagdrevierMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        //Config der Karte - Typ Satellit, Zoom per Click&Touch, Compass auf Karte
        jagdrevierMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        jagdrevierMap.getUiSettings().setZoomControlsEnabled(true);
        jagdrevierMap.getUiSettings().setZoomGesturesEnabled(true);
        jagdrevierMap.getUiSettings().setCompassEnabled(true);

        //Polygon zum Abstecken des Reviers mit vier Eckpunkten(kann erweitert werden)
        Polygon revierGrenze = googleMap.addPolygon(new PolygonOptions()
                .clickable(true)
                .add(
                        new LatLng(48.854938, 11.241890),
                        new LatLng(48.849355, 11.255418),
                        new LatLng(48.844572, 11.240600),
                        new LatLng(48.850002, 11.225756)));
        revierGrenze.setTag("Revier");
        stylePolygon(revierGrenze);

        //Kamera zur RevierMitte bewegen und mit Faktor 14 reinzoomen
        /*jagdrevierMap.animateCamera(CameraUpdateFactory.zoomTo(12),2000,null);*/
        jagdrevierMap.moveCamera(CameraUpdateFactory.newLatLngZoom(revierMitte, 14));

        // Marker in Reviermitte zur Orientierung setzen
        jagdrevierMap.addMarker(new MarkerOptions().position(revierMitte).title("Revier-Mittelpunkt"));
        jagdrevierMap.moveCamera(CameraUpdateFactory.newLatLng(revierMitte));

    }

    //Fügt der Firebase eine Jagdeinrichtung hinzu und zeigt diese auf der Map
    public void onClickAddJgdEin(View v) {
        //Bekanntmachen der View zur Texteingabe und Abruf der Eingabe als String
        final EditText jgdeinName = findViewById(R.id.jgdeinNameInput);
        final String inputText = jgdeinName.getText().toString();

        //Toast, falls kein Name im Feld, und return, damit keine Einrichtung namenlos gespeichert wird
        if (inputText.isEmpty()) {
            Toast.makeText(RevierKarte.this, R.string.nameReq, Toast.LENGTH_LONG).show();
            return;
        }

        //Initialisierung eines LocationManagers zur aktuellen Standortbestimmung
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        /**
         * In einem GeoPoint wird der aktuelle Standort gespeichert, welcher in der Firebase gespeichert werden kann.
         * Damit hinterher ein Marker mit diesen Koordinaten gesetzt werden kann, wird zusätzlich ein LatLng-Objekt
         * definiert, welches die Koordinaten aus dem GeoPoint bezieht.
         */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        final GeoPoint current = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        final LatLng latLng = new LatLng(current.getLatitude(),current.getLongitude());

        /**
         * Festlegung der Eigenschaften des anzuzeigenden Markers.
         * .position zieht die Koordinaten aus dem LatLng-Objekt latLng.
         * .icon legt das Farbschema des Markers fest und greift dazu auf ein vordefiniertes Farbschema aus der
         * BitMapDescriptionFactory zurück.
         */
        final MarkerOptions currentLoc = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                .title(inputText);

        /**
         *Initialisierung des in der Firebase zu speichernden Objekts vom Typ Hochsitz mit denen im Klassen-Constructor
         *festgelegten Attributen.
         */
        Hochsitz kanzel = new Hochsitz
                (currentLoc.getTitle(),current, false,"TBA",false,
                        false);

        /**
         * Speichern des Hochsitzobjects in der Collection dbHochsitz.
         * Über .document(inputText).set(kanzel) wird die ID in der DB mit dem Text aus dem EditText gefüllt.
         * Soll Firebase eine eigene ID generieren, müsste auf dbHochsitz.add(kanzel) geändert werden.
         * Wenn das Objekt erfolgreich gespeichert werden konnte, wird ein Marker an der aktuellen Position
         * (currentLoc) auf der Karte gesetzt und die Kamera schwenkt zum neuen Marker rüber.
         * Abschließend wird die EditText-View wieder geleert.
         */
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

    //Zeigt alle in der Firebase gespeicherten Objekte vom Typ Hochsitz auf der Karte als Marker
    public void onClickShowAll(View v){

        /**
         * dbHochsitze.get() ruft die gesamte Collection auf.
         * der onCompleteListener löst eine for-Schleife aus, welche jedes hinterlegte document der collection wieder
         * in ein Objekt vom Typ Hochsitz umwandelt.
         * Enthalten die Objekte einen nicht-leeren GeoPoint (getGPS) wird der Geopoint in ein LatLng Objekt übergeben,
         * um einen Marker an dieser Position zu setzen.
         */
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
