package com.example.jagdrevierapp;

import android.app.Activity;
import android.os.Bundle;
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

        if (getIntent().getExtras().getBoolean("booked")) {
            txtGebucht.setText("Hochsitz wurde von " + getIntent().getExtras().getString("booker") + " gebucht.");
        } else {
            txtGebucht.setText("Hochsitz ist noch frei.");
        }

    }
}
