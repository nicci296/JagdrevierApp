package com.example.jagdrevierapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class JagdeinrichtungenVerwalten extends AppCompatActivity implements View.OnClickListener{

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

        //Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            //Nicht eingeloggt, SignIn-Activity wird gestartet
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            mUsername =mFirebaseUser.getDisplayName();
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

    @Override
    public void onClick(View v){
        if (v.getId() == R.id.logoutBtn) {
            mFirebaseAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}
