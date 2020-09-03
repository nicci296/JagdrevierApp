package com.example.jagdrevierapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.jagdrevierapp.data.model.Hochsitz;
import com.example.jagdrevierapp.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class AddJagdEinrPop extends AppCompatActivity {
    //##########################################################
    //###    Constant Variables
    //##########################################################
    private static final String TAG = "AddJagdEinriPop";
    private static final String COLLECTION_PA_KEY = "Pachter";
    private static final String COLLECTION_US_KEY ="User";
    private static final String COLLECTION_HS_KEY="Hochsitze";
    private static final String COLLECTION_REV_KEY="Reviere";
    public static final String SELECTED_REVIER = "selected revier";

    //##########################################################
    //###    Firebase - Authentication
    //##########################################################
    //Initialize Firebase Auth
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final FirebaseUser mFirebaseUser = mAuth.getCurrentUser();

    //##########################################################
    //###    Firebase - Firestore
    //##########################################################
    //Initialize FireStore and References
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference dbPachter = db.collection(COLLECTION_PA_KEY);
    private final CollectionReference dbUser = db.collection(COLLECTION_US_KEY);
    CollectionReference dbReviere = dbPachter.document(mFirebaseUser.getEmail()).collection(COLLECTION_REV_KEY);
    CollectionReference dbHochsitze;

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_jagd_einr_pop);

        //##########################################################
        //###   Nav-Header and Nav-Buttons
        //##########################################################
        //LogOut Button
        ImageButton logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(AddJagdEinrPop.this, LoginActivity.class));
            }
        });

        //zu Map Button
        ImageButton mapBtn = findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddJagdEinrPop.this, RevierKarte.class));
            }
        });

        //zu Schussjournal Button
        ImageButton schussBtn = findViewById(R.id.schussBtn);
        schussBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddJagdEinrPop.this, JagdeinrichtungenVerwalten.class));
            }
        });


        //##########################################################
        //###   User aus Datenbank extrahieren
        //##########################################################
        //get UserQuery per unique Mail from FirbaseUser
        Query userQuery = dbUser.whereEqualTo("mail", mFirebaseUser.getEmail());
        //get actual dataset from dbUser
        userQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // for each document in Collection "User" do
                    //          Log.d
                    //          set all getters for class User
                    //          if currentuser is not null print
                    //              "WMH currentUser" in TextView "HelloUser"
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        currentUser = document.toObject(User.class);
                        if(currentUser.getMail() != null) {
                            //##########################################################
                            //###   Welcome-Notice in Infobar
                            //##########################################################

                            //Initialize TextView for welcoming user
                            final TextView helloUser = findViewById(R.id.helloUser);
                            helloUser.setText("WaiHei, " + currentUser.getNick());
                        }
                    }
                } else {
                    // in case of error print error to log.d
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });


        //##########################################################
        //###  Adding Jagdeinrichtung into Database
        //##########################################################
        final ImageButton btnAddJagdEinr = findViewById(R.id.btnAddJagdEinrPop);
        btnAddJagdEinr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                String rev = getIntent().getStringExtra(SELECTED_REVIER);
                //##########################################################
                //###  Value Radiobuttons
                //##########################################################

                RadioGroup radiobtngrp = findViewById(R.id.radioGroup2);
                int radioButtonID = radiobtngrp.getCheckedRadioButtonId();
                View radioButton = radiobtngrp.findViewById(radioButtonID);
                int idx = radiobtngrp.indexOfChild(radioButton);
                RadioButton r = (RadioButton) radiobtngrp.getChildAt(idx);
                String jagdeinrichtungType = r.getText().toString();

                //##########################################################
                //###  Meet Firestore
                //##########################################################

                //Bekanntmachen der View zur Texteingabe und Abruf der Eingabe als String
                final EditText jgdeinName = findViewById(R.id.txtSitzName);
                final String inputText = jgdeinName.getText().toString();

                //Snackbar (weils schönes ist), falls kein Name im Feld, und return, damit keine Einrichtung namenlos gespeichert wird
                if (inputText.isEmpty()) {
                    Snackbar.make(view, R.string.nameReq, Snackbar.LENGTH_LONG).show();
                    return;
                }

                //Initialisierung eines LocationManagers zur aktuellen Standortbestimmung
                LocationManager locationManager = (LocationManager) AddJagdEinrPop.this.getSystemService(Context.LOCATION_SERVICE);
                String locationProvider = LocationManager.NETWORK_PROVIDER;
                if (ActivityCompat.checkSelfPermission(AddJagdEinrPop.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddJagdEinrPop.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
                assert lastKnownLocation != null;
                final GeoPoint current = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                 final Hochsitz kanzel = new Hochsitz
                        (inputText, current, false,"",false,false, jagdeinrichtungType);

                dbHochsitze = dbReviere.document(rev).collection(COLLECTION_HS_KEY);
                Query query = dbHochsitze.whereEqualTo("hochsitzName", inputText);
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Snackbar.make(view,"Jagdeinrichtung existiert bereits", Snackbar.LENGTH_LONG).show();
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                jgdeinName.getText().clear();
                                return;
                            }
                        }
                        dbHochsitze.document(inputText).set(kanzel)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Snackbar sbJagdEinrAdd = Snackbar.make(view, "Jagdeinrichtung hinzugefügt", Snackbar.LENGTH_LONG);
                                        sbJagdEinrAdd.setAction("zu Jagdeinrichtungen", new backToHome());
                                        sbJagdEinrAdd.show();

                                        jgdeinName.getText().clear();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(view, "Jagdeinrichtung konnte nicht hinzugefügt werden!", Snackbar.LENGTH_LONG).show();
                                    }
                                });
                    }
                });
                            }
        });

    }

    public class backToHome implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Intent yeah = new Intent(AddJagdEinrPop.this, JagdeinrichtungenVerwalten.class);
            startActivity(yeah);
        }
    }
}