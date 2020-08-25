package com.example.jagdrevierapp;

import android.icu.text.SimpleDateFormat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jagdrevierapp.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class RegisterActivity extends AppCompatActivity {

    /** *******************23.08.20 Nico ****************
     * ++++++++++++++++++++++++++++++++++++++++++++++++++
     * Habe die keys user, nick und paechter an die Atrribute aus der User-Klasse angepasst.
     * Also von Grpß- auf Kleinbuchstabe geändert.
     * ++++++++++++++++++++++++++++++++++++++++++++++++++
     */
    private static final String TAG = "RegisterActivity";
    private static final String COLLECTION_KEY = "User";
    private static final String NICK = "nick";
    private static final String PAECHTER = "paechter";
    private static final String REGISTERED = "registered";
    private static final String MAIL = "mail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
                    Switch pachter = findViewById(R.id.switch1);
                    if (validateForm(email.getText().toString().trim(), mPwd.getText().toString().trim(), mUser.getText().toString().trim())) {
                        createAccount(email.getText().toString().trim(), mPwd.getText().toString().trim(), mUser.getText().toString().trim(), pachter.isChecked());
                    } else {
                        Toast.makeText(RegisterActivity.this, "Bitte alle Felder ausfüllen.", Toast.LENGTH_SHORT).show();
                    }
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
    private void createAccount (String mail, String pwd, String user, boolean paechter) {

        // Für später falls wir ProgressBar noch schaffen beim "hübsch machen"
        //showProgressBar();


        //##########################################################
        //###    Firebase - Firestore
        //##########################################################
        //Initialize FireStore - Collection User
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference colUser = db.collection(COLLECTION_KEY);

        /**
         * *********UPDATE 25.08.20 Nico ***********************
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++
         * Anstelle einer HashMap wird nun ein User-Objekt an die Datenbank übergeben
         * Mehr wars gar nicht. Hat sofort funktioniert :)
         * Außerdem noch ein String eingefügt, um das Datum der Registrierung festzuhalten.
         * Hab den alten Code noch stehen gelassen, damit der Unterschied besser erkenntlich ist.
         * +++++++++++++++++++++++++++++++++++++++++++++++++++++
         */
        //Calendar c = Calendar.getInstance();
        /*Map<String, Object> userJaeger = new HashMap<String, Object>();
        userJaeger.put(NICK, user);
        userJaeger.put(PAECHTER, paechter);*/
        /*userJaeger.put(REGISTERED, registerDate);
        userJaeger.put(MAIL, mail);*/
        String registerDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).
                format(new Date());
        User userJaeger = new User(mail,user,paechter,registerDate);


        colUser.document(mail).set(userJaeger)
                .addOnSuccessListener(new OnSuccessListener<Void>(){
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RegisterActivity.this,
                                "Jäger wurde angelegt!",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    public void onFailure(@NonNull Exception e){
                        Toast.makeText(RegisterActivity.this,
                                "Jäger konnte nicht angelegt werden!",
                                Toast.LENGTH_LONG).show();
                        Log.d(TAG, e.toString());
                    }
                });

        //##########################################################
        //###    Firebase - Authentication
        //##########################################################
        //Firebase instance variables
        final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

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
    private boolean validateForm(String mail, String pwd, String user) {
        boolean valid = true;
        EditText email = findViewById(R.id.userMail);
        EditText mPwd = findViewById(R.id.userPassword);
        EditText mUser = findViewById(R.id.txtNick);

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
        if (TextUtils.isEmpty(user)) {
            mUser.setError("Required.");
            valid = false;
        } else {
            mUser.setError(null);
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
