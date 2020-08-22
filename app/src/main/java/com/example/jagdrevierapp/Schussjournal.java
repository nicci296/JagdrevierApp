package com.example.jagdrevierapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    //FirebaseVarible von Michi hinzugefügt
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schussjournal);

        //##########################################################
        //###    Firebase - Authentication
        //##########################################################
        //Initialize Firebase Auth
        //Firebase instance variables
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();

        //Auskommentiert, damit Login-Funktion nicht stört --> Login funktioniert momentan nicht

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


    }




}
