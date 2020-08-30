package com.example.jagdrevierapp;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.example.jagdrevierapp.data.model.Hochsitz;


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


/**
 * *******************20.08.20 Nico*****************************************************************************
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * In dieser Google-Map-Activity wird das aktuelle Jagrevier und alle Hochsitze angezeigt.
 *
 * Damit die Map genutzt und angezeigt werden kann, muss ein API-Key generiert und in die XML unter
 * release/res/values/google_maps_api.xml eingefügt werden. Die XLM wurde beim Erstellen der Google-Map-Activity
 * automatisch angelegt und mit einer Anleitung zum Erstellen des API-Keys versehen.
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

public class RevierKarte extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    //##########################################################
    //###    Constant Variables
    //##########################################################
    private static final String TAG = "Revierkarte";
    private static final String COLLECTION_HS_KEY ="Hochsitze";
    private static final String COLLECTION_US_KEY ="User";
    private static final String COLLECTION_REV_KEY="Reviere";
    private static final String COLLECTION_PA_KEY="Pachter";
    public final String LATITUDE = "latitude";
    public final String LONGITUDE = "longitude";
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;


    //Initialize Variables
    private ImageButton jgdEinAdd, jgdEinDel, refresh, addRevier;
    private Spinner polySpin;
    private EditText jgdeinName, jgdeinOut;
    TextView spinnerItem;
    List<String> reviere = new ArrayList<>();
    private GoogleMap jagdrevierMap;
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
    private final CollectionReference dbUser = db.collection(COLLECTION_US_KEY);
    private final CollectionReference dbPachter = db.collection(COLLECTION_PA_KEY);
    CollectionReference dbReviere = dbPachter.document(mFirebaseUser.getEmail()).collection(COLLECTION_REV_KEY);
    CollectionReference dbHochsitze = dbReviere.document(COLLECTION_HS_KEY).collection(COLLECTION_HS_KEY);

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean permissionDenied = false;

    /**
     * ******************20.08.20 Nico*****************************************************************************
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     *
     * onCreate-Callback der diverse Funktionen zum Start der activity festlegt.
     *
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Festlegen der Mapview.
        setContentView(R.layout.activity_revier_karte);
        //Bekanntmachen der Views
        polySpin = findViewById(R.id.revier_spin);
        spinnerItem =findViewById(R.id.spinner_item);
        jgdeinName = findViewById(R.id.jgdeinNameInput);
        jgdEinAdd = findViewById(R.id.jgdein_Add_Btn);
        jgdEinDel = findViewById(R.id.jgdein_Del_Btn);
        refresh = findViewById(R.id.refresh_Btn);
        addRevier = findViewById(R.id.add_Revier);
        jgdeinOut = findViewById(R.id.jgdeinNameInputOut);

        //##########################################################
        //###    Firebase - Authentication
        //##########################################################
        //Initialize Firebase Auth
        //Firebase instance variables
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            //Nicht eingeloggt, SignIn-Activity wird gestartet
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            //general variables
            String mUsername = mFirebaseUser.getDisplayName();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.revier);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
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

        /**

         * ******************30.08.20 Nico **************************************
         * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         *
         * Die SpinnerView polySpin wird mit Strings gefüllt. Diese Strings kommen
         * aus der List reviere und werden über einen ArrayAdapter an die Spinner-
         * View übergeben.
         * Die Strings, welche in der der List reviere zu speichern sind, werden
         * durch einen Datenbankabfrage bestimmt, sodass der Spinner alle Documents
         * aus der Reviere-Collection beinhaltet. Der ArrayAdapter prüft über
         * notifyDataSetChangec(), ob es Änderungen an den Listeninhalten gab.
         *
         * Über einen onItemSelectedListener wird bestimmt, was die Wahl eines
         * Items aus dem Spinner auslöst.
         * Zuerst wird die Map per .clear() geleert, damit es keine Überlappung von
         * Revieren gibt.
         * Ein String selectedItem holt das aktuelle Spinner-Item.
         * In der Datanbankabfrage wird geprüft, ob es ein document in der Revier-Collection gibt,
         * welches in seinem field revName den selben String definiert.
         * Dieses document wird geholt und in ein Objekt der Modelklasse Revier umgewandelt.
         * Damit das Revier gezeichnet werden kann, werden Koordinaten benötigt, welche im Document
         * gespeichert sind.
         * Diese werden in einer ArrayListe<GeoPoint> gespeichert.
         * Da ein Polygon nur mit LatLng Koordinaten gezeichnet werden kann, wird über eine for-Loop
         * für jeden GeoPoint der Arraylist ein LatLng erzeugt und ebenfalls in einer List gespeichert.
         * Diese Liste wird dann genutzt, um das Polygon, welches das Revier symbolisiert, zu zeichnen.
         * Da zu Beginn der Methode die karte geleert wurde, folgt noche eine Abfrage der Hochsitz-
         * Collection, um alle Hochsitzmarker wieder zu setzen.
         *
         */
        //populate spinner with docs from Revier-collection
        polySpin = findViewById(R.id.revier_spin);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.spinner_item, reviere);
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
        //Create polygon from Spinner-item and show on Map
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
                                    ArrayList<GeoPoint> points= revier.getTierPoints();
                                    int length = points.size();
                                    if (length == 0) {
                                        return;
                                    }
                                    PolygonOptions poly = new PolygonOptions();
                                    poly.strokeColor(Color.WHITE);
                                    for (GeoPoint point : points) {
                                        double lat = point.getLatitude();
                                        double lng = point.getLongitude();
                                        LatLng latLng = new LatLng(lat, lng);
                                        poly.add(latLng);
                                    }
                                    jagdrevierMap.addPolygon(poly);
                                    dbHochsitze.get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot document : Objects
                                                                .requireNonNull(task.getResult())) {
                                                            Log.d(TAG, document.getId() + " => " + document
                                                                    .getData());
                                                            Hochsitz kanzel = document.toObject(Hochsitz.class);
                                                            if(kanzel.getGps() != null ){
                                                                double lat = kanzel.getGps().getLatitude();
                                                                double lng = kanzel.getGps().getLongitude();
                                                                LatLng latLng = new LatLng(lat,lng);
                                                                jagdrevierMap.addMarker(new MarkerOptions()
                                                                        .position(latLng)
                                                                        .icon(BitmapDescriptorFactory
                                                                                .defaultMarker(BitmapDescriptorFactory
                                                                                        .HUE_CYAN))
                                                                        .title(kanzel.getHochsitzName()));
                                                            }
                                                        }
                                                    } else {
                                                        Toast.makeText
                                                                (RevierKarte.this,R.string.data_Get_Fail,
                                                                        Toast.LENGTH_LONG).show();
                                                        Log.d(TAG, "Error getting documents: ",
                                                                task.getException());
                                                    }
                                                }

                                            });
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

        /**
         * ****************20.08.20 Nico********************************************************************
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         *
         * Methode zum Speichern eines Hochsitzes an der aktuellen GPS-Position im Firestore.
         * Speichert ein Object der Modelklasse Hochsitz in der Collection dbHochsitz.
         * Über .document(inputText).set(kanzel) wird die ID in der DB mit dem Text aus dem EditText gefüllt.
         * Soll Firebase eine eigene ID generieren, müsste auf dbHochsitz.add(kanzel) geändert werden.
         * OnSuccessListener: Wenn das Objekt erfolgreich gespeichert werden konnte, wird ein Marker an der
         * aktuellen Position (currentLoc) auf der Karte gesetzt und die Kamera schwenkt zum neuen Marker rüber.
         * Abschließend wird die EditText-View wieder geleert.
         * OnFailureListener: Toast weist auf fehlgeschlagenes Speichern hin
         *
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         * ***************UPDATE 22.08.20 Nico**************************************************************
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         *
         * Bevor der Datensatz gespeichert wird, wird über einen Datenabruf (.get()) mittels einer Query getestest,
         * ob bereits ein Revier mit dem eingegebenen Namen in der Firebase existiert.
         * Ist ein solches Document bereits vorhanden, wird ein Toast ausgegeben und return ausgelöst.
         * Im Anschluss erfolgt das oben beschriebene Speichern.
         *
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         */
        jgdEinAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String inputText = jgdeinName.getText().toString();
                //Toast, falls kein Name im Feld, und return, damit keine Einrichtung namenlos gespeichert wird
                if (inputText.isEmpty()) {
                    Toast.makeText(RevierKarte.this, R.string.nameReq, Toast.LENGTH_LONG).show();
                    return;
                }

                /*LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                String locationProvider = LocationManager.NETWORK_PROVIDER;*/
                Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
                final GeoPoint current = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                final LatLng latLng = new LatLng(current.getLatitude(),current.getLongitude());
                /*
                 * Festlegung der Eigenschaften des anzuzeigenden Markers.
                 * .position zieht die Koordinaten aus dem LatLng-Objekt latLng.
                 * .icon legt das Farbschema des Markers fest und greift dazu auf ein vordefiniertes Farbschema aus der
                 * BitMapDescriptionFactory zurück.
                 */
                final MarkerOptions currentLoc = new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                        .title(inputText);
                /*
                 *Initialisierung des in der Firebase zu speichernden Objekts vom Typ Hochsitz mit denen im Klassen-Constructor
                 *festgelegten Attributen.
                 */
                final Hochsitz kanzel = new Hochsitz
                        (currentLoc.getTitle(), current, false,"TBA",false,
                                false);
                //Start der Datenbank-Operation
                Query query = dbHochsitze.whereEqualTo("hochsitzName", inputText);
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Toast.makeText(RevierKarte.this, "Jagdeinrichtung existiert bereits",
                                        Toast.LENGTH_LONG).show();
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                jgdeinName.getText().clear();
                                return;
                            }
                        }
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
                });

            }
        });

        /**
         * *******************22.08.20 Nico************************************************************
         * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         *
         * OnClickListener zum Löschen von Hochsitzen.
         * Wie beim Hinzufügen wird im onComplete-Callback zuerst über eine Query ein
         * document aus der firebase geholt, welches im field hochsitzName den Wert aus dem EditText hat.
         * Liegt ein solches Document in der Firebase, wird es per .delete() gelöscht und ein return
         * ausgelöst.
         * Wird der Task aus dem Callback nicht erfolgreich beendet, wird ein Toast augerufen, welcher auf ein
         * nicht existentes Document hinweist.
         *
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         */
        jgdEinDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String inputTextOut = jgdeinOut.getText().toString();
                //Toast, falls kein Name im Feld, und return, damit keine Einrichtung ohne Namen gelöscht wird
                if (inputTextOut.isEmpty()) {
                    Toast.makeText(RevierKarte.this, R.string.nameReq, Toast.LENGTH_LONG).show();
                    return;
                }
                Query query = dbHochsitze.whereEqualTo("hochsitzName", inputTextOut);
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                dbHochsitze.document(inputTextOut)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                                Toast.makeText
                                                        (RevierKarte.this, R.string.data_Del_Success,
                                                                Toast.LENGTH_LONG).show();
                                                jgdeinOut.getText().clear();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error deleting document", e);
                                            }
                                        });
                                return;
                            }
                        }
                        Toast.makeText
                                (RevierKarte.this, R.string.data_Del_Fail,
                                        Toast.LENGTH_LONG).show();
                        jgdeinOut.getText().clear();
                    }
                });
            }
        });

        /**
         * **************************22.08.20 Nico*****************************************************************
         * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         *
         * In der OnClickShowAll-Methode ruft die Datenbankabfrage dbHochsitze.get() die gesamte Collection
         * an Hohsitzen auf.
         * der onCompleteListener löst eine for-Schleife aus, welche jedes hinterlegte document der collection wieder
         * in ein Objekt vom Typ Hochsitz umwandelt.
         * Enthalten die Objekte einen nicht-leeren GeoPoint (getGPS) wird der Geopoint in ein LatLng Objekt übergeben,
         * um einen Marker an dieser Position zu setzen.
         * So lassen sich alle gespeichterten Hochsitze auf Knopfdruck anzeigen.
         *
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         * *************************UPDATE 23.08.20 Nico ***********************************************************
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         *
         * Aus Platz- und Nutzungsgründen wird Inhalt der onClickShowAll-Methode im onMapReady-Callback automatisch
         * ausgelöst, damit von Beginn der Activity an alle Hochsitze sichtbar sind.
         * Alternaiv wird ein jetzt Refresh-Button mit onClickListener in OnCreate registriert.
         *
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         * *************************UPDATE 30.08.20 Nico ***********************************************************
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         *
         * Da nun Reviere über einen Spinner gewählt und angezeigt werden können, wird die Abfrage der aktuellen
         * documents der Hochsitze-Collection im onItemSelectedListener des polySpin Spinners ausgeführt.
         * Würde dies nicht geschehen, würden jedes mal, wenn ein neues Revier aus dem Spinner selektiert wird,
         * alle Marker von der Karte gelöscht. Nach jedem Wechsel müsste also der Refresh-button betätigt werden,
         * was nicht nutzerfreundlich ist.
         * Siehe dazu die Spinner Funktionalität weiter unten..
         *
         * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         */
        //Erneuter Abruf der Documents aus der Hochsitze-Collection, um eventuelle Änderungen am Revier zu aktualisieren
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                                            jagdrevierMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 13));
                                        }
                                    }
                                    Toast.makeText
                                            (RevierKarte.this,R.string.refresh_Success,Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText
                                            (RevierKarte.this,R.string.refresh_Fail,Toast.LENGTH_LONG).show();
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });

            }
        });

        /**
         * *************************30.08.20 Nico*********************************************************
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         *
         * Da nun ein Spinner in der View existiert, welcher zwischen gespeicherten Revieren wählen lässt,
         * muss natürlich auch eine Activity zum Anlegen dieser Reviere existieren.
         * Der nachfolgende Intent leitet zu dieser neuen RevierSketch-Activity weiter.
         *
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         */
        addRevier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent changeIntent = new Intent(RevierKarte.this, RevierSketch.class);
                startActivity(changeIntent);
            }
        });
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
        jagdrevierMap.setMyLocationEnabled(true);
        jagdrevierMap.setOnMyLocationButtonClickListener(this);
        jagdrevierMap.setOnMyLocationClickListener(this);
        enableMyLocation();
        //Config der Karte - Typ Satellit, Zoom per Click&Touch, Compass auf Karte
        jagdrevierMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 13));
        jagdrevierMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        jagdrevierMap.getUiSettings().setZoomControlsEnabled(true);
        jagdrevierMap.getUiSettings().setZoomGesturesEnabled(true);
        jagdrevierMap.getUiSettings().setCompassEnabled(true);

        /**
         * ************* UPDATE 30.08.20 Nico ********************
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++
         *
         * Das ursprünglich statische Revier kann jetzt aus einem
         * SpinnerView gewählt und angezeigt werden. Siehe
         * dazu im OnCreate-Callback.
         * Deshalb ist das u.s. auskommentierte Styling eines
         * Polygons überflüssig.
         * Die geschieht in der neunen RevierSketch-Activity, in
         * welcher sich neue Reviere anlegen lassen.
         *
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++
         */
        /*//Polygon zum Abstecken des Reviers mit vier Eckpunkten(kann erweitert werden)
        Polygon revierGrenze = googleMap.addPolygon(new PolygonOptions()
                .clickable(true)
                .add(
                        new LatLng(48.859032, 11.226044),
                        new LatLng(48.858333, 11.224939),
                        new LatLng(48.854138, 11.209016),
                        new LatLng(48.851124, 11.213823),

                ));
        revierGrenze.setTag("Revier");
        stylePolygon(revierGrenze);*/
        /**
         * ************27.08.20 Nico ******************************
         * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         *
         * Empfangen des Intents aus Schussjournal-Activity.
         * Die aus den Extras gewonnenen double-Werte werden in einem
         * LatLng Objekt gespeichert und als Marker auf der Map angezeigt.
         * Farblich von den Hochsitz-Markern abgehoben.
         *
         * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         */
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            double intentLat = extras.getDouble(LATITUDE);
            double intentLng = extras.getDouble(LONGITUDE);
            LatLng intentLoc = new LatLng(intentLat,intentLng);

            jagdrevierMap.addMarker(new MarkerOptions().position(intentLoc)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
            jagdrevierMap.moveCamera(CameraUpdateFactory.newLatLngZoom(intentLoc,17));
        }

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
