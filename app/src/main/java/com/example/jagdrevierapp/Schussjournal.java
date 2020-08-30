package com.example.jagdrevierapp;


import android.content.Context;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



import androidx.recyclerview.widget.ItemTouchHelper;
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
    private final String LATITUDE = "latitude";
    private final String LONGITUDE = "longitude";

    //##########################################################
    //###    Firebase - Authentication
    //##########################################################
    //Initialize Firebase Auth
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final FirebaseUser mFirebaseUser = mAuth.getCurrentUser();

    //Initialize FireStore and References
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference dbUser = db.collection(COLLECTION_KEY);
    private final CollectionReference dbJournal = dbUser.document(mFirebaseUser.getEmail()).collection(JOURNAL_COLLECTION_KEY);
    //Initialisierung eines neuen JournalAdapter-Objekts - Instanzierung weiter unten
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
        ImageButton logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(Schussjournal.this, LoginActivity.class));
            }
        });

        //zu Map Button
        ImageButton mapBtn = findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Schussjournal.this, RevierKarte.class));
            }
        });

        //zu Schussjournal Button
        ImageButton schussBtn = findViewById(R.id.schussBtn);
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
         * Abruf aller docs aus dem Schussjournal und Darstellung in RecyclerView
         * via JournalAdapter.
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         */
        //Query sortiert die Docs aus User-bezogenen Schussjournal nach Datum
        Query journalUser = dbJournal.orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Journal> options = new FirestoreRecyclerOptions.Builder<Journal>()
                .setQuery(journalUser,Journal.class)
                .build();
        //Instanzierung des Adapters
        adapter = new JournalAdapter(options);

        //RecyclerView gibt Journaleinträge aus, indem es das JournalAdapter-Objekt registriert
        RecyclerView journalView = findViewById(R.id.journal_View);
        journalView.setHasFixedSize(true);
        journalView.setLayoutManager(new LinearLayoutManager(this));
        journalView.setAdapter(adapter);

        /**
         * **************25.08.20 Nico *****************************************
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         * ItemTouchHelper legt fest was passiert, wenn bestimmte Elemente der
         * RecyclerView vom User berührt werden.
         * Legt in seinen Parametern die möglichen Bewegungsrichtungen für Drag
         * und Swipe fest.
         * Da Drag/Drop nicht benötigt, wird es 0 gesetzt und die Swipe-Richtungen
         * LEFT und RIGHT festgelegt.
         * Durch den Swipe werden documents an der Stelle aus der Firebase gelöscht.
         * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            //OnMove definiert Vorgehen bei Drag&Drop-Bewegungen (hoch,runter) - hier irrelevant
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) { return false; }

            //onSwipe definiert, was ein Swipe auslöst. In diesem Fall die deleteItem-Methode aus dem JournalAdapter
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    adapter.deleteItem(viewHolder.getAdapterPosition());
            }
            //abschließend wird der ItemTouchHelper an die RecyclerView gebunden.
        }).attachToRecyclerView(journalView);

        /**
         * **************27.08.20 Nico *****************************************
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         * Bei Klick auf ein Item der RecyclerView, soll auf die RevierKarte
         * gewechselt werden und ein Marker dort angezeigt werden, wo der Journal-
         * eintrag gemacht wurde.
         * Dazu ruft der adapter die Interface-Methode setOnItemClick aus dem
         * OnJournalClickListener-Interface (liegt im JournalAdapter) auf und
         * überschreibt deren innere onItemClick-Methode.
         * Das Interface wird über die Parameter-Angabe der setOnJournalClickListener-
         * Methode implementiert. Dadurch muss das Interface nicht über die
         * Klassen-Deklaration Schussjournal-Activity implementiert werden.
         * Ein DocumentSnapshot holt sich an der Stelle, wo geklickt wurde, das zugehörige
         * Document aus der Firebase und zieht den GeoPoint aus diesem Document.
         * Da ein GeoPoint nicht in einen Intent übergeben werden kann, wird dieser
         * auf zwei double aufgeteilt, welche als extra in den changeIntent übergeben
         * werden.
         * Durch die Key LATITUDE und LONGITUDE kann die Revierkarte-Activity diese
         * wieder auslesen.
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         */
        adapter.setOnJournalClickListener(new JournalAdapter.OnJournalClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Journal journal = documentSnapshot.toObject(Journal.class);
                String id = documentSnapshot.getId();
                Toast.makeText(Schussjournal.this,"Ich war hier am:"+id,Toast.LENGTH_LONG).show();
                GeoPoint entryLoc = journal.getLocation();
                double intentLat = entryLoc.getLatitude();
                double intentLng = entryLoc.getLongitude();

                Intent changeIntent = new Intent(Schussjournal.this,RevierKarte.class);
                changeIntent.putExtra(LATITUDE,intentLat);
                changeIntent.putExtra(LONGITUDE,intentLng);
                startActivity(changeIntent);
            }
        });
    }


    //onStart-Callback legt fest, dass der adapter bei Activity-Start die datenbank auf relevante Einträge überwacht
    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    //OnStop-callback legt fest, dass der adapter bei Activity-Stop die Datenbank-Überwachung beendet
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
