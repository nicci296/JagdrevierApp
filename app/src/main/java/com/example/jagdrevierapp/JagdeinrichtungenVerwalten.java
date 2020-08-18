package com.example.jagdrevierapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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


public class JagdeinrichtungenVerwalten extends AppCompatActivity implements View.OnClickListener {
    //Const variables

    private static final String TAG = "JagdeinrichtungenVer";
    private static final String GPS_POSITION = "GPS-Position";
    private static final String HOCHSITZ_NAME = "HochsitzName";
    private static final String PASS_KEY = "password";

    private final String COLLECTION_KEY ="HochsitzeMichi";

    public static final String ANONYMOUS = "anonymous";

    //general variables
    private String mUsername;
    private GoogleSignInClient mSignInClient;

        //View declaration
        private TextView textAuswahlHochsitze;
        private TextView textHochsitzName;


    //Initialize FireStore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference dbHochsitze = db.collection(COLLECTION_KEY);
    private DocumentReference docRefHochsitze = dbHochsitze.document();


    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jagdeinrichtungen_verwalten);

        //onClickListener für Buttons for Nav
        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(this);
        Button mapBtn = findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener(this);
        Button menuBtn = findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(this);
        Button schussBtn = findViewById(R.id.schussBtn);
        schussBtn.setOnClickListener(this);
        Button revierBtn = findViewById(R.id.revierBtn);
        revierBtn.setOnClickListener(this);


        //onClickListener für Buttons for Jagdeinrichtungen
        Button btnStatusHochsitz = findViewById(R.id.btnStatusHochsitz);
        btnStatusHochsitz.setOnClickListener(this);
        Button btnBook = findViewById(R.id.btnBook);
        btnBook.setOnClickListener(this);
        Button btnDamage = findViewById(R.id.btnDamage);
        btnDamage.setOnClickListener(this);
        Button btnInsect = findViewById(R.id.btnInsect);
        btnInsect.setOnClickListener(this);
        FloatingActionButton addHochsitz = findViewById(R.id.btnAddHochsitz);
        addHochsitz.setOnClickListener(this);

        // TextViews to create some texts
        textAuswahlHochsitze = findViewById(R.id.textAuswahlHochsitze);

        textHochsitzName = findViewById(R.id.textHochsitzName);
        textHochsitzName.setText("Hochsitz für den Jäger");
        textAuswahlHochsitze.setText("Alle Hochsitze");

        //Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            //Nicht eingeloggt, SignIn-Activity wird gestartet
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            mUsername = mFirebaseUser.getDisplayName();
        }

        // Read data from firestore

        docRefHochsitze.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    Hochsitz hochsitz = doc.toObject(Hochsitz.class);
                    textAuswahlHochsitze.setText(doc.get("HochsitzName").toString());
                }
            }
        });



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutBtn:
                mFirebaseAuth.signOut();
                mSignInClient.signOut();

                mUsername = ANONYMOUS;
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Navigation eingerichtet, Menü funktioniert noch nicht
     */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.logoutBtn) {
            mFirebaseAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
        }
        if (i == R.id.mapBtn) {
            startActivity(new Intent(this, RevierKarte.class));

        }
        // nur zu testzwecken, da Menübutton etwas mehr in anspruch nehmen wird
        if (i == R.id.revierBtn) {
            startActivity(new Intent(this, Revierverwaltung.class));
            Log.d(TAG, "Entfällt wenn Menü funktioniert");
        }
        if (i == R.id.schussBtn) {
            startActivity(new Intent(this, Schussjournal.class));
            Toast.makeText(this, "Entfällt wenn Menü ok", Toast.LENGTH_SHORT).show();
        }
        if (i == R.id.menuBtn) {
            Toast.makeText(this, "Imagine: Menü erscheint", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Entfällt wenn Menü funktioniert");
        }
    }
}
