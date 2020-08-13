package com.example.jagdrevierapp;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Revierverwaltung extends AppCompatActivity {

    public static final String FIRST_NAME_KEY = "first name";
    public static final String NAME_KEY = "name";
    public static final String ID_KEY = "id";
    public static final String TAG = "user";

   //Anlegen einer Instanz der Firebase zur Nutzung in den onClick-Methoden onClickAddJaeger & onClickDeleteJaeger
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revierverwaltung);
    }

    //Intent zum Wechseln der Activity
    public void onClickRevierkarte(View button){
        Intent changeIntent = new Intent(this, RevierKarte.class);
        startActivity(changeIntent);
    }

    //Lässt bei Click einen Jäger in die Datenbank hinzufügen
    public void onClickAddJaeger(View button) {

        /**
         * Lokalisierung der Views über findViewById() und Aufrufen der eingetragenen Strings über getText().toString().
         * EditText als final deklariert, um die View später leeren zu können.
         */

        final EditText userFirstName = findViewById(R.id.userFirstName);
        final EditText userName = findViewById(R.id.userName);
        final EditText userID = findViewById(R.id.userID);
        String firstName = userFirstName.getText().toString();
        String name = userName.getText().toString();
        String id = userID.getText().toString();

        //Toast, falls nicht alle Felder ausgefüllt wurden
        if (firstName.isEmpty() || name.isEmpty() || id.isEmpty()) {
            Toast.makeText(Revierverwaltung.this,
                    R.string.Pflichtfelder,
                    Toast.LENGTH_LONG).show();
        }
        /**
         * Erstellen der zu speichernden Daten als Hashmap, welche als Document in der Collection "jaeger" gespeichert
         * werden sollen.
         * Strings firstname, name und id im Parameter als Konstanten definiert.
         */
        Map<String, Object> userJaeger = new HashMap<String, Object>();
        userJaeger.put(FIRST_NAME_KEY, firstName);
        userJaeger.put(NAME_KEY, name);
        userJaeger.put(ID_KEY, id);

        /**
         * Beim erstmaligen Ausführen der Methode wird automatisch die Collection "jaeger"" angelegt.
         * .add() fügt das Document userJaeger mit eigener ID der Collection hinzu.
         * Der addOnSuccessListener() gibt bei Erfolg einen Toast aus und leert die Eingabefelder.
         * Der addOnFailureListener gibt Toast aus, wenn nicht in die DB geschrieben werden konnte.
         */
        db.collection("jaeger")
                .add(userJaeger)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(Revierverwaltung.this,
                                "Jaeger wurde angelegt!",
                                Toast.LENGTH_LONG).show();
                        userFirstName.getText().clear();
                        userName.getText().clear();
                        userID.getText().clear();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    public void onFailure(@NonNull Exception e){
                        Toast.makeText(Revierverwaltung.this,
                                "Jaeger konnte nicht angelegt werden!",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }


        //Lässt bei Click einen Jäger aus der Datenbank entfernen
    public void onClickDeleteJaeger(View button){

    }
}
