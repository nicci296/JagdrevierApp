package com.example.jagdrevierapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final String COLLECTION_KEY = "User";


    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;

    //Check if user is already signed in
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //##########################################################
        //###    Firebase - Firestore
        //##########################################################
        //Initialize FireStore - Collection Hochsitze
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference colUser = db.collection(COLLECTION_KEY);

        //##########################################################
        //###    Buttons - Handling Main Functions
        //##########################################################
        //Button to complete registration
        Button registBtn = findViewById(R.id.registBtn);
        registBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    EditText email = findViewById(R.id.userMail);
                    EditText mPwd = findViewById(R.id.userPassword);
                    EditText mUser = findViewById(R.id.txtNick);
                    String mail = email.getText().toString().trim();
                    String pwd = mPwd.getText().toString().trim();
                    createAccount(mail, pwd);


            }
        });

        // Button to get to SignIn-Activity
        TextView toSignInBtn =  findViewById(R.id.toSignInText);
        toSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });


    }

    //##########################################################
    //###    Methods - General
    //##########################################################
    // Method to create Account in Firebase-Authentication
    private void createAccount (String mail, String pwd) {
        Log.d(TAG, "createAccount:" + mail);
        if (!validateForm(mail, pwd)) {
            return;
        }
        // Für später falls wir ProgressBar noch schaffen beim "hübsch machen"
        //showProgressBar();

        // Create User with Mail
        mFirebaseAuth.createUserWithEmailAndPassword(mail, pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //User gets signed in by updating UI
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            //Failing sign in display the following Message
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        // Für später falls wir ProgressBar noch schaffen beim "hübsch machen"
                        //hideProgressBar();
                    }
                });
    }
    // Method to validate the Views on Layout
    private boolean validateForm(String mail, String pwd) {
        boolean valid = true;
        EditText email = findViewById(R.id.userMail);
        EditText mPwd = findViewById(R.id.userPassword);

        if (TextUtils.isEmpty(mail)) {
            email.setError("Required.");
            valid = false;
        } else {
            email.setError(null);
        }

        if (TextUtils.isEmpty(pwd)) {
            mPwd.setError("Required.");
            valid = false;
        } else {
            mPwd.setError(null);
        }
        return valid;
    }
    // Methode to handle User
    private void updateUI(FirebaseUser user) {
        // Für später falls wir ProgressBar noch schaffen beim "hübsch machen"
        //hideProgressBar();
        if (user != null) {
            startActivity(new Intent(this, JagdeinrichtungenVerwalten.class));
        } else {
            Toast.makeText(RegisterActivity.this, "Login failed!",
                    Toast.LENGTH_SHORT).show();
        }
}
}
