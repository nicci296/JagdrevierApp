package com.example.jagdrevierapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jagdrevierapp.data.model.User;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.io.LittleEndianDataInputStream;
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
import java.util.Objects;


public class JagdeinrichtungenVerwalten extends AppCompatActivity {
    //##########################################################
    //###    Constant Variables
    //##########################################################
    private static final String TAG = "JagdeinrichtungenVer";
    private static final String COLLECTION_HS_KEY ="HochsitzeMichi";
    private static final String COLLECTION_US_KEY ="User";


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
    private final CollectionReference dbHochsitze = db.collection(COLLECTION_HS_KEY);
    private final CollectionReference dbUser = db.collection(COLLECTION_US_KEY);

    //Initialisierung eines neuen HochsitzAdapter-Objekts
    private HochsitzAdapter adapter;

    private User currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jagdeinrichtungen_verwalten);


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
        //###   FloatingBtn for Adding JagdEinr
        //##########################################################
        FloatingActionButton addJagdEinr = findViewById(R.id.btnAddHochsitz);
        addJagdEinr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(JagdeinrichtungenVerwalten.this, AddJagdEinrPop.class);
                startActivity(intent);
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
        //###   List of all Hochsitzen in RecylcerView
        //##########################################################

        //Get all Docs from Collection Hochsitze and sort by Name ascending (A-Z)
        Query hochsitzQuery = dbHochsitze.orderBy("hochsitzName", Query.Direction.ASCENDING);

        //set up connection between Query and class Hochsitz
        FirestoreRecyclerOptions<Hochsitz> options = new FirestoreRecyclerOptions.Builder<Hochsitz>()
                .setQuery(hochsitzQuery, Hochsitz.class)
                .build();

        //Instanciate Adapter
        adapter = new HochsitzAdapter(options);

        //Creating RecyclerView with List of Hochsitzen by registrating HochsitzAdapter-Objects
        RecyclerView hochsitzView = findViewById(R.id.jw_recycler_View);
        hochsitzView.setHasFixedSize(false);
        hochsitzView.setLayoutManager( new LinearLayoutManager(this));
        hochsitzView.setAdapter(adapter);

    }
    //If app forwarded: listener to adapter/ db is active
    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    //if app in background: listener to adapter/ db is inactive
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();

    }
}
