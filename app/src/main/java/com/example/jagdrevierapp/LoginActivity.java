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

public class LoginActivity extends AppCompatActivity{

    private static final String TAG = "LoginActivity";

    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        final EditText email = findViewById(R.id.userMail);
        final EditText mPwd = findViewById(R.id.userPassword);

        final String mail = email.getText().toString().trim();
        final String pwd = mPwd.getText().toString().trim();

        //Buttons

        // Button to start the login-prozedure
        Button logInBtn = findViewById(R.id.LoginBtn);
        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn(mail, pwd);
            }
        });

        // Text as Button to get to Register
        TextView toRegisterBtn = findViewById(R.id.toRegisterText);
        toRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void signIn(String mail, String pwd) {
        Log.d(TAG, "signIn:" + mail);
        if (!validateForm(mail, pwd)) {
            return;
        }
        // Für später falls wir ProgressBar noch schaffen beim "hübsch machen"
        //showProgressBar();
        mFirebaseAuth.signInWithEmailAndPassword(mail, pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "singInWithEmail:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Log.w(TAG, "singInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

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

    private void updateUI(FirebaseUser user) {
        // Für später falls wir ProgressBar noch schaffen beim "hübsch machen"
        //hideProgressBar();
        if (user != null) {
             startActivity(new Intent(this, JagdeinrichtungenVerwalten.class));
        } else {
            Toast.makeText(LoginActivity.this, "Login failed!",
                    Toast.LENGTH_SHORT).show();
        }

    }
}
