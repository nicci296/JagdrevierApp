package com.example.jagdrevierapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.jagdrevierapp.data.model.Hochsitz;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StatusPop extends Activity {

    //##########################################################
    //###    Constant Variables
    //##########################################################
    private static final String TAG = "StatusPop";
    private static final String COLLECTION_HS_KEY ="Hochsitze";
    private static final String COLLECTION_US_KEY ="User";
    private static final String COLLECTION_REV_KEY="Reviere";
    private static final String COLLECTION_PA_KEY="Pachter";

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
    private final CollectionReference dbUser = db.collection(COLLECTION_US_KEY);
    private final CollectionReference dbPachter = db.collection(COLLECTION_PA_KEY);
    CollectionReference dbReviere = dbPachter.document(mFirebaseUser.getEmail()).collection(COLLECTION_REV_KEY);

    @Override
    protected void onCreate(Bundle savedInstancesState) {
        super.onCreate(savedInstancesState);
        String sitz = getIntent().getExtras().getString("sitzname");
        String revierName = getIntent().getExtras().getString("reviername");
        setContentView(R.layout.statuspop);

        //##########################################################
        //###    Auslesen Views von Status
        //##########################################################

        final TextView sitzname = findViewById(R.id.statusHochsitzName);
        final TextView popTitle = findViewById(R.id.titleStatusPop);
        final TextView txtGebucht = findViewById(R.id.statusHochsitzgebucht);
        final TextView txtGesperrt = findViewById(R.id.textHochsitzgesperrt);

        if(revierName != null) {
            CollectionReference dbHochsitze = dbReviere.document(revierName).collection(COLLECTION_HS_KEY);

            dbHochsitze.document(sitz)
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Hochsitz sitz = documentSnapshot.toObject(Hochsitz.class);
                    sitzname.setText(sitz.getHochsitzName());
                    popTitle.setText(sitz.getHochsitzName());


                    //##########################################################
                    //###    Damage/ Insect - Button handling
                    //##########################################################
                    boolean booked = sitz.isBooked();
                    if (booked) {
                        txtGebucht.setText("Hochsitz wurde von " + sitz.getBookedBy() + " gebucht.");
                    } else {
                        txtGebucht.setText("Hochsitz ist noch frei.");
                    }


                    //##########################################################
                    //###    Damage/ Insect - Button handling
                    //##########################################################
                    boolean damaged = sitz.isDamaged();
                    boolean instct = sitz.isInsectious();

                    if (damaged && instct) {
                        txtGesperrt.setText(("ACHTUNG! Hochsitz weißt Beschädigung und Insektenbefall auf!"));
                    } else if (instct) {
                        txtGesperrt.setText("ACHTUNG! Hochsitz ist von Insekten befallen!");
                    } else if (damaged) {
                        txtGesperrt.setText("ACHTUNG! Hochsitz hat eine Beschädigung!");
                    } else {
                        txtGesperrt.setText("Waidmannsheil wünschen Nico und Michi!");
                    }
                }
            });
        } else {
            Snackbar.make(findViewById(R.id.myConstraintStatLayout), "Die Hochsitzdaten konnten nicht geladen werden.", Snackbar.LENGTH_LONG).show();
        }

        Button btnBack = findViewById(R.id.statusBtnBack);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StatusPop.this, JagdeinrichtungenVerwalten.class));
            }
        });


    }
}