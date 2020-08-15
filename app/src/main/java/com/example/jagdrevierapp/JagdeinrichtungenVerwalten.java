package com.example.jagdrevierapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class JagdeinrichtungenVerwalten extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "JagdeinrichtungenVerwalten";

    //Const variables
    public static final String ANONYMOUS = "anonymous";

    //general variables
    private String mUsername;
    private GoogleSignInClient mSignInClient;

    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jagdeinrichtungen_verwalten);

        //onClickListener für Buttons
        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(this);
        Button mapBtn = findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener(this);
        Button menuBtn = findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(this);


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
        if (i == R.id.menuBtn) {
            Toast.makeText(this, "Imagine: Menü erscheint", Toast.LENGTH_SHORT).show();
        }
    }
}
