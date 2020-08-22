package com.example.jagdrevierapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.jagdrevierapp.data.model.Hochsitz;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;
import java.util.Map;


public class JagdeinrichtungenVerwalten extends AppCompatActivity {
    //Const variables
    private static final String TAG = "JagdeinrichtungenVer";
    private static final String COLLECTION_KEY ="HochsitzeMichi";

    private String docname;


        //Object declaration
        private Hochsitz obj;

    Map<String, Object> data = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jagdeinrichtungen_verwalten);

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



        //##########################################################
        //###    Buttons from Nav-Header
        //##########################################################
        //LogOut Button
        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(JagdeinrichtungenVerwalten.this, LoginActivity.class));
                }
        });

        //zu Map Button
        Button mapBtn = findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(JagdeinrichtungenVerwalten.this, RevierKarte.class));
            }
        });

        //zu Schussjournal Button
        Button schussBtn = findViewById(R.id.schussBtn);
        schussBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(JagdeinrichtungenVerwalten.this, Schussjournal.class));
            }
        });


        //##########################################################
        //###    Firebase - Firestore
        //##########################################################
        //Initialize FireStore - Collection Hochsitze
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference dbHochsitze = db.collection(COLLECTION_KEY);

        // TextViews to create some texts
        final TextView textAuswahlHochsitze = findViewById(R.id.textAuswahlHochsitze);
        textAuswahlHochsitze.setText("Alle Hochsitze");


        // Read data from firestore
        dbHochsitze
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                final LinearLayout orderLayout = findViewById(R.id.orderLayoutHochsitze);
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        obj = document.toObject(Hochsitz.class);

                        //Anlegen jedes Layouts pro Schleifendurchlauf
                        View viewHochsitze = getLayoutInflater().inflate(R.layout.hochsitz_layout, orderLayout, false );
                        orderLayout.addView(viewHochsitze);

                        // Vergeben des Hochsitznamens
                        TextView txtHochsitzName = viewHochsitze.findViewById(R.id.textHochsitzName);
                        txtHochsitzName.setText(obj.getHochsitzName());
                        txtHochsitzName.setTextSize(20);

                        // Status Button initialisieren
                        Button btnStatHochsitz = viewHochsitze.findViewById(R.id.btnStatusHochsitz);
                        btnStatHochsitz.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(JagdeinrichtungenVerwalten.this, StatusPop.class);
                                Bundle extras = new Bundle();
                                extras.putString("sitzname", obj.getHochsitzName());
                                extras.putString("booker", obj.getBookedBy());
                                extras.putBoolean("booked", obj.isIsBooked());
                                extras.putBoolean("damage", obj.isIsDamaged());
                                extras.putBoolean("insect", obj.isIsInsectious());
                                intent.putExtras(extras);
                                startActivity(intent);
                            }
                        });

                        docname = document.getId();
                        textAuswahlHochsitze.setText(docname);

                        //Button to book hochsitz
                        Button btnBook = findViewById(R.id.btnBook);
                        btnBook.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(obj.isIsBooked()) {
                                    data.put("IsBooked", false);
                                    // Nutzer auslesen --> Name bekommen
                                    // Daten an firestore --> value "Name" für variable "booked by"
                                    // Toast Nachricht: "WMH" + name + "der Ansitz ist für Dich gebucht!"
                                } else {
                                    data.put("IsBooked", true);
                                    // Nutzer auslesen --> Name bekommen
                                    // Daten von firestore --> value "name" von variable "bookedBy"
                                    // Toast Nachricht: "Sorry " + name + "hier sitzt heute Nacht schon " + "bookedBy"
                                }
                                dbHochsitze.document(docname)
                                        .set(data, SetOptions.merge());
                            }
                        });


                        // Button to announce a damaged hochsitz
                        Button btnDamage = findViewById(R.id.btnDamage);
                        btnDamage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(obj.isIsDamaged()) {
                                    data.put("isDamaged", false);
                                } else {
                                    data.put("isDamaged", true);
                                }
                                db.collection(COLLECTION_KEY).document(docname)
                                        .set(data, SetOptions.merge());
                            }
                        });

                        // Button to announce insects inside hochsitz
                        Button btnInsect = findViewById(R.id.btnInsect);
                        btnInsect.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(obj.isIsInsectious()) {
                                    data.put("isInsectious", false);
                                } else {
                                    data.put("isInsectious", true);
                                }
                                db.collection(COLLECTION_KEY).document(docname)
                                        .set(data, SetOptions.merge());
                            }
                        });
                    }
                } else {
                    Log.w(TAG, "Error getting docs: ", task.getException());
                }
            }

                } );




        // Button to add new Hochsitz
        FloatingActionButton addHochsitz = findViewById(R.id.btnAddHochsitz);
        addHochsitz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textAuswahlHochsitze.setText("Hi there I am using WhatsApp!");
            }
        });

    }
}
