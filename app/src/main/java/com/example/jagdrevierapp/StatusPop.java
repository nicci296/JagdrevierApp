package com.example.jagdrevierapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.jagdrevierapp.data.model.Hochsitz;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

public class StatusPop extends Activity {



    //##########################################################
    //###    Constant Variables
    //##########################################################
    private static final String TAG = "StatusPop";
    private static final String COLLECTION_HS_KEY ="Hochsitze";
    private static final String COLLECTION_US_KEY ="User";
    private static final String COLLECTION_REV_KEY="Reviere";
    private static final String COLLECTION_PA_KEY="Pachter";
    public static final String DB_HOCHSITZE = "dbHochsitze";
    public static final String SITZNAME = "sitzname";
    public static final String STATUS_INTENT = "Status_Intent";

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
    CollectionReference dbReviere;
    CollectionReference dbHochsitze;




    @Override
    protected void onCreate(Bundle savedInstancesState) {
        super.onCreate(savedInstancesState);
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

        setContentView(R.layout.statuspop);

        //##########################################################
        //###    Auslesen Views von Status
        //##########################################################

        final TextView sitzname = findViewById(R.id.statusHochsitzName);
        final TextView popTitle = findViewById(R.id.titleStatusPop);
        final TextView txtGebucht = findViewById(R.id.statusHochsitzgebucht);
        final TextView txtGesperrt = findViewById(R.id.textHochsitzgesperrt);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(STATUS_INTENT));
        String sitz = getIntent().getExtras().getString(SITZNAME);

        dbHochsitze = dbReviere.document(mReceiver.toString()).collection(COLLECTION_HS_KEY);
        Query query = dbHochsitze.whereEqualTo("hochsitzname", sitz);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Hochsitz hochsitz = document.toObject(Hochsitz.class);
                        sitzname.setText(hochsitz.getHochsitzName());
                        popTitle.setText(hochsitz.getHochsitzName());


                        //##########################################################
                        //###    Damage/ Insect - Button handling
                        //##########################################################
                        boolean booked = hochsitz.isBooked();
                        if (booked) {
                            txtGebucht.setText("Hochsitz wurde von " + hochsitz.getBookedBy() + " gebucht.");
                        } else {
                            txtGebucht.setText("Hochsitz ist noch frei.");
                        }


                        //##########################################################
                        //###    Damage/ Insect - Button handling
                        //##########################################################
                        boolean damaged = hochsitz.isDamaged();
                        boolean instct = hochsitz.isInsectious();

                        if (damaged && instct) {
                            txtGesperrt.setText(("ACHTUNG! Hochsitz weißt Beschädigung und Insektenbefall auf!"));
                        } else if(instct){
                            txtGesperrt.setText("ACHTUNG! Hochsitz ist von Insekten befallen!");
                        } else if (damaged) {
                            txtGesperrt.setText("ACHTUNG! Hochsitz hat eine Beschädigung!");
                        } else {
                            txtGesperrt.setText("Waidmannsheil wünschen Nico und Michi!");
                        }

                    }
                }
            }
        });

        /*dbHochsitze.document(sitz)
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
                } else if(instct){
                    txtGesperrt.setText("ACHTUNG! Hochsitz ist von Insekten befallen!");
                } else if (damaged) {
                    txtGesperrt.setText("ACHTUNG! Hochsitz hat eine Beschädigung!");
                } else {
                    txtGesperrt.setText("Waidmannsheil wünschen Nico und Michi!");
                }
            }
        });*/


        Button btnBack = findViewById(R.id.statusBtnBack);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StatusPop.this, JagdeinrichtungenVerwalten.class));
            }
        });


    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String revier = intent.getStringExtra(DB_HOCHSITZE);
        }
    };
}
