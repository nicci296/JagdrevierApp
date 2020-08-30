package com.example.jagdrevierapp;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Revierverwaltung extends AppCompatActivity {



    private static final String TAG = "Revierverwaltung";

    /**
     * Benötigte Konstanten für die Firestore Methoden.
     * Firestore arbeitet mit Key-Value-Pairs, weshalb für die Eingabefelder entsprechende Key-Strings als Konstanten
     * angelegt werden.
     * Außerdem wird jeweils eine Instanzvariable der EdiTextViews und der Firestore database benötigt.
     */
    //-------------------------------------------------------------------------------------------------------------
    private static final String FIRST_NAME_KEY = "first name";
    private static final String NAME_KEY = "name";
    private static final String PASS_KEY = "password";

    private EditText userFirstName;
    private EditText userName;
    private EditText userPass;

    //FirebaseVarible von Michi hinzugefügt
    private FirebaseAuth mAuth;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference jaegerCol = db.collection("Jaeger");
    private DocumentReference docRef = db.collection("Jaeger").document();

    //-------------------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revierverwaltung);

        userFirstName = findViewById(R.id.user_First_Name);
        userName = findViewById(R.id.user_Name);
        userPass = findViewById(R.id.user_Pass);

        //von Michi hinzugefügt: Buttons aus Navigation initialisieren
        //##########################################################
        //###   Nav-Header and Nav-Buttons
        //##########################################################
        //LogOut Button
        ImageButton logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(Revierverwaltung.this, LoginActivity.class));
            }
        });

        //zu Map Button
        ImageButton mapBtn = findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Revierverwaltung.this, RevierKarte.class));
            }
        });

        //zu Schussjournal Button
        ImageButton schussBtn = findViewById(R.id.schussBtn);
        schussBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Revierverwaltung.this, Schussjournal.class));
            }
        });


        mAuth = FirebaseAuth.getInstance();

    }


    //Lässt bei Click einen Jäger in die Datenbank hinzufügen
    public void onClickAddJaeger(View v) {


         //Aufrufen der eingetragenen Strings über getText().toString().
        String firstName = userFirstName.getText().toString();
        String name = userName.getText().toString();
        String pass = userPass.getText().toString();

        //Toast, falls nicht alle Felder ausgefüllt wurden
        if (firstName.isEmpty() || name.isEmpty() || pass.isEmpty()) {
            Toast.makeText(Revierverwaltung.this,
                    R.string.pflichtfelder,
                    Toast.LENGTH_LONG).show();
        }
        /**
         * Erstellen der zu speichernden Daten als Hashmap mit einem Key-Value-Pair aus String und Object,
         * welche als Document in der Collection "jaeger" gespeichert werden sollen.
         * Key-Strings firstname, name und id weiter oben als Konstanten definiert.
         * Als Value-Object wird der im EditText eingetragene String übergeben.
         */
        Map<String, Object> userJaeger = new HashMap<String, Object>();
        userJaeger.put(FIRST_NAME_KEY, firstName);
        userJaeger.put(NAME_KEY, name);
        userJaeger.put(PASS_KEY, pass);

        /**
         * Beim erstmaligen Ausführen der Methode wird automatisch die Collection "Jaeger" über die CollectionRefrence
         * jaegerCol angelegt.
         * .set() fügt die HasMap userJaeger als Document mit eigener ID der Collection hinzu.
         * Der addOnSuccessListener() gibt bei Erfolg einen Toast aus und leert die Eingabefelder.
         * Der addOnFailureListener gibt Toast aus, wenn nicht in die DB geschrieben werden konnte und erstellt eine
         * Log-Message für die Konsole.
         */
        jaegerCol.document(userPass.getText().toString()).set(userJaeger)
                .addOnSuccessListener(new OnSuccessListener<Void>(){
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Revierverwaltung.this,
                                "Jäger wurde angelegt!",
                                Toast.LENGTH_LONG).show();
                        userFirstName.getText().clear();
                        userName.getText().clear();
                        userPass.getText().clear();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    public void onFailure(@NonNull Exception e){
                        Toast.makeText(Revierverwaltung.this,
                                "Jäger konnte nicht angelegt werden!",
                                Toast.LENGTH_LONG).show();
                        Log.d(TAG, e.toString());
                    }
                });
    }


        //Lässt bei Click einen Jäger aus der Datenbank entfernen
    public void onClickDeleteJaeger(View button){
    }


}
