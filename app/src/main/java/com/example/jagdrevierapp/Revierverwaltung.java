package com.example.jagdrevierapp;

import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class Revierverwaltung extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revierverwaltung);
    }

    public void onClickRevierkarte(View button){
        Intent changeIntent = new Intent(this, RevierKarte.class);
        startActivity(changeIntent);
    }
}
