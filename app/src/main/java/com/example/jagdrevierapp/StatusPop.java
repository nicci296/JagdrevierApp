package com.example.jagdrevierapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class StatusPop extends Activity {

    @Override
    protected void onCreate(Bundle savedInstancesState) {
        super.onCreate(savedInstancesState);

        setContentView(R.layout.statuspop);

        TextView sitzname = findViewById(R.id.statusHochsitzName);
        sitzname.setText(getIntent().getExtras().getString("sitzname"));

        TextView popTitle = findViewById(R.id.titleStatusPop);
        popTitle.setText(getIntent().getExtras().getString("sitzname"));

        TextView txtGebucht = findViewById(R.id.statusHochsitzgebucht);
        boolean booked = getIntent().getExtras().getBoolean("booked");
        if (booked) {
            txtGebucht.setText("Hochsitz wurde von " + getIntent().getExtras().getString("booker") + " gebucht.");
        } else {
            txtGebucht.setText("Hochsitz ist noch frei.");
        }

        TextView txtGesperrt = findViewById(R.id.textHochsitzgesperrt);
        boolean damaged = getIntent().getExtras().getBoolean("damage");
        boolean instct = getIntent().getExtras().getBoolean("insect");

        if (damaged && instct) {
            txtGesperrt.setText(("ACHTUNG! Hochsitz weißt Beschädigung und Insektenbefall auf!"));
        } else if(instct){
            txtGesperrt.setText("ACHTUNG! Hochsitz ist von Insekten befallen!");
        } else if (damaged) {
            txtGesperrt.setText("ACHTUNG! Hochsitz hat eine Beschädigung!");
        } else {
            txtGesperrt.setText("Waidmannsheil wünschen Nico und Michi!");
        }




        Button btnBack = findViewById(R.id.statusBtnBack);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StatusPop.this, JagdeinrichtungenVerwalten.class));
            }
        });


    }
}
