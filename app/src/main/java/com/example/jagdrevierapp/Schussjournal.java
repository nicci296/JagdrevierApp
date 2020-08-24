package com.example.jagdrevierapp;

import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jagdrevierapp.data.model.Journal;
import com.example.jagdrevierapp.data.model.User;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.Objects;


public class Schussjournal extends AppCompatActivity  {

    /**
     * Wilderei durch Michi am 16.08.2020 - 19:22 Uhr
     *
     *Ich habe die "Navigation" als ein Constraint-Layout in der Activity_revierverwaltung.xml hinzugefügt
     * Damit die Navigation klappt, implementiere ich hier die Klasse "View.onClickListener
     * In den weiteren Steps muss ich die Buttons per findViewByID definieren (passiert in Methode OnCreate)
     * Nachder Definition der Buttons lege ich auf jeden Button ein .setOnClickListener(this) --> Erkennung, wenn der Button gedrückt wird
     *
     * in Der Method onClick(View v) implementiere ich dann die startActivity und rufe per expliziten Intent die jeweilige View auf.
     */


    private final String TAG = "Schussjournal";
    private final String COLLECTION_KEY = "User";
    private final String JOURNAL_COLLECTION_KEY = "Schussjournal";

    //##########################################################
    //###    Firebase - Authentication
    //##########################################################
    //Initialize Firebase Auth
    //Firebase instance variables
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final FirebaseUser mFirebaseUser = mAuth.getCurrentUser();

    //Initialize FireStore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference dbUser = db.collection(COLLECTION_KEY);
    private final CollectionReference dbJournal = dbUser.document(mFirebaseUser.getEmail()).collection(JOURNAL_COLLECTION_KEY);
    private JournalAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schussjournal);


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
                startActivity(new Intent(Schussjournal.this, LoginActivity.class));
            }
        });

        //zu Map Button
        Button mapBtn = findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Schussjournal.this, RevierKarte.class));
            }
        });

        //zu Schussjournal Button
        Button schussBtn = findViewById(R.id.schussBtn);
        schussBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Schussjournal.this, JagdeinrichtungenVerwalten.class));
            }
        });

        /**
         * ******************23.08.20 Nico ***************************************************
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         * TextView soll den aktuellen Nick vom User in Überschrift anzeigen.
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         */

        final TextView userText = findViewById(R.id.user_Name_Jrnl);

        Query userQuery = dbUser.whereEqualTo("mail",mFirebaseUser.getEmail());
        userQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        User currentUser = document.toObject(User.class);
                        if(currentUser.getMail() != null ){
                            userText.setText(currentUser.getNick().toUpperCase()+"s'");

                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        /**
         * **************24.08.20. Nico*****************************************
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         * Abruf aller docs aus dem Schussjournal und Darstellung in RecyclerView.
         * Implementierung der Methode weiter unten.
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         */
        //Query sortiert die Docs aus User-bezogenen Schussjournal nach Datum
        Query journalUser = dbJournal.orderBy("date", Query.Direction.DESCENDING);
        //RecyclerView gibt Journal aus
        RecyclerView journalView = findViewById(R.id.journal_View);
        journalView.setHasFixedSize(true);
        journalView.setLayoutManager(new LinearLayoutManager(this));
        journalView.setAdapter(adapter);

        FirestoreRecyclerOptions<Journal> options = new FirestoreRecyclerOptions.Builder<Journal>()
                .setQuery(journalUser,Journal.class)
                .build();

        //Instanzierung des Adapters
        adapter = new JournalAdapter(options);

        journalView.setAdapter(adapter);


    }

    /*private void setUpRecyclerView(){
        //Query sortiert die Docs aus User-bezogenen Schussjournal nach Datum
        Query journalUser = dbJournal.orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Journal> options = new FirestoreRecyclerOptions.Builder<Journal>()
                .setQuery(journalUser,Journal.class)
                .build();

        adapter = new JournalAdapter(options);
        //RecyclerView gibt Journal aus
        RecyclerView journalView = findViewById(R.id.journal_View);
        journalView.setHasFixedSize(true);
        journalView.setLayoutManager(new LinearLayoutManager(this));
        journalView.setAdapter(adapter);
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    //Weiterleitung zur JournalPop, um einen neuen Eintrag anzulegen.
    public void onClickAddLine(View v){

        Intent changeIntent = new Intent(this,JournalPop.class);
        startActivity(changeIntent);

    }


}
