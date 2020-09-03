package com.example.jagdrevierapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import com.example.jagdrevierapp.data.model.Journal;
import com.example.jagdrevierapp.data.model.User;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * ****************23.08.20 Nico*************
 * ++++++++++++++++++++++++++++++++++++++++++
 *
 * Pop-Up für einen neuen Eintrag ins Schussjournal
 *
 * ++++++++++++++++++++++++++++++++++++++++++
 */
public class JournalPop extends AppCompatActivity {

    private final String TAG = "JournalPop";
    private final String COLLECTION_KEY = "User";

    //##########################################################
    //###    Firebase - Authentication
    //##########################################################
    //Initialize Firebase Auth
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final FirebaseUser mFirebaseUser = mAuth.getCurrentUser();

    //Initialize FireStore and References
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference dbUser = db.collection(COLLECTION_KEY);
    private final CollectionReference dbJournal;
    {
        assert mFirebaseUser != null;
        String JOURNAL_COLLECTION_KEY = "Schussjournal";
        dbJournal = dbUser.document(Objects.requireNonNull(mFirebaseUser.getEmail())).collection(JOURNAL_COLLECTION_KEY);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_pop);

        if (mFirebaseUser == null) {
            //Nicht eingeloggt, SignIn-Activity wird gestartet
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            //general variables
            String mUsername = mFirebaseUser.getDisplayName();
        }

        //##########################################################
        //###   Nav-Header and Nav-Buttons
        //##########################################################
        //LogOut Button
        ImageButton logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(JournalPop.this, LoginActivity.class));
            }
        });

        //zu Map Button
        ImageButton mapBtn = findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(JournalPop.this, RevierKarte.class));
            }
        });

        //zu Schussjournal Button
        ImageButton schussBtn = findViewById(R.id.schussBtn);
        schussBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(JournalPop.this, Schussjournal.class));
            }
        });


        /**
         * ******************23.08.20 Nico ***************************************************
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         *
         * TextView soll den aktuellen Nickname vom User in der Überschrift anzeigen.
         *
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         */

        final TextView userText = findViewById(R.id.user_Name_Jrnl);
        Query query = dbUser.whereEqualTo("mail", Objects.requireNonNull(mFirebaseUser).getEmail());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        User currentUser = document.toObject(User.class);
                        if (currentUser.getMail() != null) {

                            userText.setText(currentUser.getNick().toUpperCase() + "s");
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });


        ImageButton btnAddJrnl = findViewById(R.id.save_Line_Btn);
        btnAddJrnl.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                final EditText shots = findViewById(R.id.input_Shots);
                final EditText hits = findViewById(R.id.input_Hits);
                final EditText caliber = findViewById(R.id.input_Caliber);
                final EditText target = findViewById(R.id.input_Target);
                final EditText means = findViewById(R.id.input_Means);
                final int shotsTaken;


                if ((shots.getText().toString()).equals("")) {
                    shotsTaken =0;
                } else {
                    shotsTaken = Integer.parseInt(shots.getText().toString());
                }

                final int hitsLanded;
                if ((hits.getText().toString()).equals("")) {
                    hitsLanded = 0;
                } else {
                    hitsLanded = Integer.parseInt(hits.getText().toString());
                }

                final String caliberUsed = caliber.getText().toString();
                final String aimedTarget = target.getText().toString();
                final String meansForShot = means.getText().toString();
                //aktuelles Datum mit Uhrzeit
                final String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());


                //Toast, falls Felder leer, und return, damit keine unvollständigen Einträge angelegt werden.
                if (aimedTarget.isEmpty() | meansForShot.isEmpty() | caliberUsed.isEmpty()) {
                    if (shotsTaken == 0 | hitsLanded == 0) {
                       Toast.makeText(JournalPop.this, R.string.fields_Req, Toast.LENGTH_LONG).show();
                        return;
                    }

                } else {
                    //Toast, falls mehr Treffer als Schüsse, weil geht ja nicht...
                    if (hitsLanded > shotsTaken) {
                        Toast.makeText(JournalPop.this, R.string.hits_Over_Shots, Toast.LENGTH_LONG).show();
                        return;
                    }
                }


                //Initialisierung eines LocationManagers zur Standortbestimmung für den Geopoint im Journal-Constructor
                LocationManager locManager = (LocationManager) JournalPop.this.getSystemService(Context.LOCATION_SERVICE);
                String locationProvider = LocationManager.NETWORK_PROVIDER;
                if (ActivityCompat.checkSelfPermission(JournalPop.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                        .PERMISSION_GRANTED && ActivityCompat
                        .checkSelfPermission(JournalPop.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager
                        .PERMISSION_GRANTED) {
                    return;
                }
                Location lastKnownLocation = locManager.getLastKnownLocation(locationProvider);
                final GeoPoint current = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                //neues Journal Object zur Übergabe an die Firebase mit View-Eingaben, GeoPoint und Datum
                Journal journal = new Journal(shotsTaken, hitsLanded, caliberUsed, meansForShot,
                        aimedTarget, date, current);

        /*
            Speichern des Journal-Documents in der Subcollection "Schussjournal" des angemeldeten Users
            und Rückkehr zum Schussjournal
         */
                dbJournal.document(journal.getDate()).set(journal)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(JournalPop.this, R.string.line_Add_Success, Toast.LENGTH_LONG)
                                        .show();
                                shots.getText().clear();
                                hits.getText().clear();
                                caliber.getText().clear();
                                target.getText().clear();
                                means.getText().clear();
                                Intent changeIntent = new Intent(JournalPop.this, Schussjournal.class);
                                startActivity(changeIntent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(JournalPop.this, R.string.line_Add_Fail,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
/*
    //Speichern der View-Eingaben in der User-bezogenen Schussjournal-Collection
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onClickSaveLine(View v) {


    }
*/
    }
}