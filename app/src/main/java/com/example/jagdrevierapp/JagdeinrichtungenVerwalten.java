package com.example.jagdrevierapp;


import android.graphics.Path;
import android.os.Parcelable;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.jagdrevierapp.data.model.Revier;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import com.example.jagdrevierapp.data.model.Hochsitz;
import com.example.jagdrevierapp.data.model.User;
import com.google.firebase.firestore.core.OrderBy;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class JagdeinrichtungenVerwalten extends AppCompatActivity {
    //##########################################################
    //###    Constant Variables
    //##########################################################
    private static final String TAG = "JagdeinrichtungenVer";
    private static final String COLLECTION_HS_KEY ="Hochsitze";
    private static final String COLLECTION_US_KEY ="User";
    private static final String COLLECTION_REV_KEY="Reviere";
    private static final String COLLECTION_PA_KEY="Pachter";
    public static final String DB_HOCHSITZE = "dbHochsitze";
    public static final String STATUS_INTENT = "Status_Intent";
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
    private final CollectionReference dbUser = db.collection(COLLECTION_US_KEY);
    private final CollectionReference dbPachter = db.collection(COLLECTION_PA_KEY);
    private CollectionReference dbHochsitze;
    private CollectionReference dbReviere;


    //##########################################################
    //###    General Declarations
    //##########################################################
    //Initialisierung eines neuen HochsitzAdapter-Objekts
    // Object-Declarations
    private HochsitzAdapter adapter;
    private User currentUser;

    //LiveData Declaration
    private MutableLiveData<HochsitzAdapter> myGigLiveData;

    // Variable-declaraations
    List<String> reviere = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jagdeinrichtungen_verwalten);


        myGigLiveData = new MutableLiveData<>();

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


        RecyclerView hochsitzView = findViewById(R.id.jw_recycler_View);

        //##########################################################
        //###   Nav-Header and Nav-Buttons
        //##########################################################
        //LogOut Button
        ImageButton logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(JagdeinrichtungenVerwalten.this, LoginActivity.class));
            }
        });

        //zu Map Button
        ImageButton mapBtn = findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(JagdeinrichtungenVerwalten.this, RevierKarte.class));
            }
        });

        //zu Schussjournal Button
        ImageButton schussBtn = findViewById(R.id.schussBtn);
        schussBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(JagdeinrichtungenVerwalten.this, Schussjournal.class));
            }
        });


        //##########################################################
        //###   User aus Datenbank extrahieren
        //##########################################################

        //get UserQuery per unique Mail from FirbaseUser
        Query userQuery = dbUser.whereEqualTo("mail", mFirebaseUser.getEmail());
        //get actual dataset from dbUser
        userQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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

                        }
                    }
                } else {
                    // in case of error print error to log.d
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        //##########################################################
        //###   Welcome-Notice in Infobar
        //##########################################################

        //Initialize TextView for welcoming user

        final TextView helloUser = findViewById(R.id.helloUser);
        //if(currentUser.getNick() != null) {
        //    helloUser.setText("WaiHei, " + currentUser.getNick());
        //} else {
            helloUser.setText("Hallo Jager!");
        //}


        //##########################################################
        //###   Spinner for Reviere in Infobar
        //##########################################################
        final Spinner revSpinner = findViewById(R.id.reviere_spinner);
        //populate spinner with docs from Revier-collection
        final ArrayAdapter<String> revAdapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, reviere);
        revAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        revSpinner.setAdapter(revAdapter);

        dbReviere.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String subject = document.getString("revName");
                        reviere.add(subject);
                    }
                }

                revAdapter.notifyDataSetChanged();
            }
        });

        revSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> polySpin, View view, final int position, final long id) {
                final RecyclerView hochsitzView = findViewById(R.id.jw_recycler_View);
                final String selectedItem = revSpinner.getItemAtPosition(position).toString();

                dbReviere.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(selectedItem.equals(document.getString("revName"))) {
                                    dbHochsitze = dbReviere.document(revSpinner.getItemAtPosition(position).toString())
                                            .collection(COLLECTION_HS_KEY);
                                    Revier revier = document.toObject(Revier.class);
                                    String revierName = revier.getRevName();
                                    Query hochsitzQuery = dbHochsitze;
                                    FirestoreRecyclerOptions<Hochsitz> options = new FirestoreRecyclerOptions.Builder<Hochsitz>()
                                            .setQuery(hochsitzQuery, Hochsitz.class)
                                            .build();
                                    adapter = new HochsitzAdapter(options);
                                    hochsitzView.setAdapter(adapter);
                                    hochsitzView.setHasFixedSize(true);
                                    hochsitzView.setLayoutManager( new LinearLayoutManager(hochsitzView.getContext()));
                                    adapter.notifyDataSetChanged();
                                    myGigLiveData.setValue(adapter);

                                    Intent revierIntent = new Intent(STATUS_INTENT);
                                    revierIntent.putExtra(DB_HOCHSITZE, revierName);
                                    LocalBroadcastManager.getInstance(JagdeinrichtungenVerwalten.this).sendBroadcast(revierIntent);
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


        //##########################################################
        //###   FloatingBtn for Adding JagdEinr
        //##########################################################
        FloatingActionButton addJagdEinr = findViewById(R.id.btnAddHochsitz);
        addJagdEinr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedItem = revSpinner.getSelectedItem().toString();
                        Intent intent = new Intent(JagdeinrichtungenVerwalten.this, AddJagdEinrPop.class);
                intent.putExtra(SELECTED_REVIER, selectedItem);
                startActivity(intent);
            }
        });




        //##########################################################
        //###   Deletable Hochsitze - Permission "Pächter" required
        //##########################################################

        // Abfrage, ob Pächter && Revierowner, wobei Pächterabfrage nicht zwingend erfolgen muss --> nur pächter können Reviere anlegen
        // if (Pächter && Revierowner)
        //      make Hochsitze deleteable by swiping

            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                //OnMove definiert Vorgehen bei Drag&Drop-Bewegungen (hoch,runter) - hier irrelevant
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                      @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                //onSwipe definiert, was ein Swipe auslöst. In diesem Fall die deleteItem-Methode aus dem JournalAdapter
                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();

                    View view = findViewById(R.id.theGrandView);
                    final int pos = viewHolder.getAdapterPosition();
                    Snackbar mySnackbar = Snackbar.make(view, "Soll wirklich gelöscht werden?" , Snackbar.LENGTH_LONG);
                    mySnackbar.setAction("Löschen", new deleteJagdEinr(pos));
                    mySnackbar.show();

                }
                //abschließend wird der ItemTouchHelper an die RecyclerView gebunden.
            }).attachToRecyclerView(hochsitzView);
        }


    //If app forwarded: listener to adapter/ db is active
    @Override
    protected void onStart() {
        super.onStart();
        myGigLiveData.observe(this, adapterList ->{
            adapter.startListening();
        });



    }

    //if app in background: listener to adapter/ db is inactive
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }


    //////////////////////
    // additional classes
    ////////////////////////
    public class deleteJagdEinr implements View.OnClickListener {
        int pos;

        deleteJagdEinr (int pos) {
            this.pos = pos;
        }

        @Override
        public  void onClick(View view) {

            adapter.deleteItem(pos);
        }
    }
}

