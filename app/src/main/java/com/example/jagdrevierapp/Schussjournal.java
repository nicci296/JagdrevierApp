package com.example.jagdrevierapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class Schussjournal extends AppCompatActivity implements View.OnClickListener {

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

        //von Michi hinzugefügt: Buttons aus Navigation initialisieren
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

        mAuth = FirebaseAuth.getInstance();
    }


    /**
     * Navigation eingerichtet, Menü funktioniert noch nicht
     */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.logoutBtn) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
        }
        if (i == R.id.mapBtn) {
            startActivity(new Intent(this, RevierKarte.class));
        }
        // nur zu testzwecken, da Menübutton etwas mehr in anspruch nehmen wird
        if (i == R.id.revierBtn) {
            startActivity(new Intent(this, Revierverwaltung.class));
            Toast.makeText(this, "Entfällt wenn Menü ok", Toast.LENGTH_SHORT).show();
        }
        if (i == R.id.schussBtn) {
            startActivity(new Intent(this, Schussjournal.class));
            Toast.makeText(this, "Entfällt wenn Menü ok", Toast.LENGTH_SHORT).show();
        }
        if (i == R.id.menuBtn) {
            Toast.makeText(this, "Imagine: Menü erscheint", Toast.LENGTH_SHORT).show();
        }

    }

}
