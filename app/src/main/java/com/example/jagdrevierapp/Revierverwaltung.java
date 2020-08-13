package com.example.jagdrevierapp;

import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.google.firebase.firestore.FirebaseFirestore;

public class Revierverwaltung extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revierverwaltung);
    }
    //Neue Instanz der Datenbank
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //Intent zum Wechseln der Activity
    public void onClickRevierkarte(View button){
        Intent changeIntent = new Intent(this, RevierKarte.class);
        startActivity(changeIntent);
    }

    //Lässt bei Click einen Jäger in die Datenbank hinzufügen
    public void onClickAddJaeger(View button){

    }
    //Lässt bei Click einen Jäger aus der Datenbank entfernen
    public void onClickDeleteJaeger(View button){

    }
}
