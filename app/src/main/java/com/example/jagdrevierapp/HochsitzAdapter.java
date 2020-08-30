package com.example.jagdrevierapp;

import com.example.jagdrevierapp.data.model.Hochsitz;
import com.example.jagdrevierapp.data.model.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HochsitzAdapter extends FirestoreRecyclerAdapter<Hochsitz, HochsitzAdapter.HochsitzHolder> {
    //##########################################################
    //###    Constant Variables
    //##########################################################
    private static final String TAG = "JagdeinrichtungenVer";
    private static final String COLLECTION_HS_KEY ="HochsitzeMichi";
    private static final String COLLECTION_US_KEY ="User";



    //##########################################################
    //###    Firebase - Authentication
    //##########################################################
    //Initialize Firebase Auth
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final FirebaseUser mFirebaseUser = mAuth.getCurrentUser();



    //##########################################################
    //###    Firebase - Firestore
    //##########################################################
    //Initialize FireStore and References
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference dbHochsitze = db.collection(COLLECTION_HS_KEY);
    private final CollectionReference dbUser = db.collection(COLLECTION_US_KEY);

    private OnHochsitzClickListener listener;


    //##########################################################
    //###    Consturctors
    //##########################################################
    // Constructor of Superclass
    public HochsitzAdapter(FirestoreRecyclerOptions<Hochsitz> options) {
        super(options);
    }



    private User currentUser;



    //##########################################################
    //###   List of all Hochsitzen in RecylcerView
    //##########################################################
    @Override
    protected void onBindViewHolder(final HochsitzHolder hochsitzHolder, int position, final Hochsitz hochsitz) {


        //##########################################################
        //###   User aus Datenbank extrahieren
        //##########################################################

        //get UserQuery per unique Mail from FirbaseUser
        Query userQuery = dbUser.whereEqualTo("mail", mFirebaseUser.getEmail());
        //get actual dataset from dbUser
        userQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // for each document in Collection "User" do
                    //          Log.d
                    //          set all getters for class User
                    //          if currentuser is not null print
                    //              "WMH currentUser" in TextView "HelloUser"
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                       currentUser = document.toObject(User.class);
                        if(currentUser.getMail() != null) {
                        }
                    }
                } else {
                    // in case of error print error to log.d
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });




        //##########################################################
        //###  Buttons of Hochsitz auslesen und initialisieren
        //##########################################################
        if(hochsitz.isBooked()) {
            hochsitzHolder.btnStatus.setBackgroundResource(R.drawable.hochsetz_besetzt);
            hochsitzHolder.btnBook.setBackgroundColor(Color.parseColor("#354559"));
            hochsitzHolder.btnBook.setTextColor(Color.parseColor("#DCD1D1"));

        } else{
            hochsitzHolder.btnStatus.setBackgroundResource(R.drawable.hochsitz_frei);
            hochsitzHolder.btnBook.setBackgroundColor(Color.parseColor("#DCD1D1"));
            hochsitzHolder.btnBook.setTextColor(Color.parseColor("#354559"));
                    }

        if(hochsitz.isDamaged()) {
            hochsitzHolder.btnDamage.setBackgroundColor(Color.parseColor("#354559"));
            hochsitzHolder.btnDamage.setTextColor(Color.parseColor("#DCD1D1"));
        } else {
            hochsitzHolder.btnDamage.setBackgroundColor(Color.parseColor("#DCD1D1"));
            hochsitzHolder.btnDamage.setTextColor(Color.parseColor("#354559"));
        }

        if(hochsitz.isInsectious()) {
            hochsitzHolder.btnInsect.setBackgroundColor(Color.parseColor("#354559"));
            hochsitzHolder.btnInsect.setTextColor(Color.parseColor("#DCD1D1"));
        } else {

            hochsitzHolder.btnInsect.setBackgroundColor(Color.parseColor("#DCD1D1"));
            hochsitzHolder.btnInsect.setTextColor(Color.parseColor("#354559"));
        }


        //##########################################################
        //###  Buttons Größe anpassen
        //##########################################################
        hochsitzHolder.btnStatus.setWidth(50);
        hochsitzHolder.btnStatus.setHeight(50);

        hochsitzHolder.btnBook.setWidth(50);
        hochsitzHolder.btnBook.setHeight(50);

        hochsitzHolder.btnDamage.setWidth(50);
        hochsitzHolder.btnDamage.setHeight(50);

        hochsitzHolder.btnInsect.setWidth(50);
        hochsitzHolder.btnInsect.setHeight(50);


        //##########################################################
        //###  Status Button - ruft StatusPopUp auf
        //##########################################################
        hochsitzHolder.btnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), StatusPop.class);
                Bundle extras = new Bundle();
                extras.putString("sitzname", hochsitz.getHochsitzName());
                intent.putExtras(extras);
                view.getContext().startActivity(intent);
            }
        });



        //##########################################################
        //###  Buchen-Button - toggled ob gebucht oder nicht (Userabhängig)
        //##########################################################
        hochsitzHolder.btnBook.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(!hochsitz.isBooked()) {
                    Map<String, Object> hunter = new HashMap<>();
                    hunter.put("booked", true);
                    hunter.put("bookedBy", currentUser.getNick());
                    dbHochsitze.document(hochsitz.getHochsitzName())
                            .update(hunter)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error updating document", e);
                                }
                            });
                    Toast.makeText(view.getContext(),"WMH " + currentUser.getNick(),Toast.LENGTH_LONG).show();
                } else if (hochsitz.getBookedBy().equals(currentUser.getNick())) {
                    Map<String, Object> hunter = new HashMap<>();
                    hunter.put("booked", false);
                    hunter.put("bookedBy", "");
                    dbHochsitze.document(hochsitz.getHochsitzName())
                            .update(hunter)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error updating document", e);
                                }
                            });

                    Toast.makeText(view.getContext(),hochsitz.getBookedBy() + ", dein Sitz ist freigegeben",Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(view.getContext(),"Heute Nacht sitzt hier: " + hochsitz.getBookedBy(),Toast.LENGTH_LONG).show();
                }
            }
        });

        //##########################################################
        //###  Damage-Button
        //##########################################################
        hochsitzHolder.btnDamage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hochsitz.isDamaged()) {
                    Map<String, Object> damage = new HashMap<>();
                    damage.put("damaged", true);
                    dbHochsitze.document(hochsitz.getHochsitzName())
                            .update(damage)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Damage DocumentSnapshot successfully updated!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error updating document", e);
                                }
                            });
                    Toast.makeText(view.getContext(),"Danke für die Schadensmeldung, " + currentUser.getNick(),Toast.LENGTH_LONG).show();


                } else {
                    Map<String, Object> damage = new HashMap<>();
                    damage.put("damaged", false);
                    dbHochsitze.document(hochsitz.getHochsitzName())
                            .update(damage)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Damage DocumentSnapshot successfully updated!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error updating document", e);
                                }
                            });
                    Toast.makeText(view.getContext(),"Schadensmeldung ist aufgehoben.",Toast.LENGTH_LONG).show();
                }
            }
        });

        //##########################################################
        //###  Insect-Button
        //##########################################################
        hochsitzHolder.btnInsect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hochsitz.isInsectious()) {
                    Map<String, Object> insect = new HashMap<>();
                    insect.put("insectious", true);
                    dbHochsitze.document(hochsitz.getHochsitzName())
                            .update(insect)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Insect DocumentSnapshot successfully updated!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error updating document", e);
                                }
                            });
                    Toast.makeText(view.getContext(),"Danke für die Insekten-Meldung, " + currentUser.getNick(),Toast.LENGTH_LONG).show();


                } else {
                    Map<String, Object> insect = new HashMap<>();
                    insect.put("insectious", false);
                    dbHochsitze.document(hochsitz.getHochsitzName())
                            .update(insect)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Insect DocumentSnapshot successfully updated!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error updating document", e);
                                }
                            });
                    Toast.makeText(view.getContext(),"Insekten sind abgezogen.",Toast.LENGTH_LONG).show();
                }
            }
        });


        //##########################################################
        //###  Hochsitzbezeichnung in RecyclerView
        //##########################################################
        hochsitzHolder.txtHochsitzName.setText(hochsitz.getHochsitzName());
        hochsitzHolder.txtHochsitzName.setTextColor(Color.parseColor("#DCD1D1"));
        hochsitzHolder.txtHochsitzName.setTextSize(20);
    }


    @NonNull
    @Override
    public HochsitzHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.hochsitz_layout, parent, false);
        return new HochsitzHolder(v);
    }

    //Methode zum Löschen eines Hochsitzeintrags aus der View und dem Firestore
    public void deleteItem(int position){
        getSnapshots().getSnapshot(position).getReference().delete();
    }


    class HochsitzHolder extends RecyclerView.ViewHolder {
        Button btnStatus;
        TextView txtHochsitzName;
        Button btnBook;
        Button btnDamage;
        Button btnInsect;

        //superConstructor
        public HochsitzHolder(View itemView) {
            super(itemView);
            btnStatus = itemView.findViewById(R.id.btnStatusHochsitz);
            txtHochsitzName = itemView.findViewById(R.id.textJVJagerName);
            btnBook = itemView.findViewById(R.id.btnBook);
            btnDamage = itemView.findViewById(R.id.btnDamage);
            btnInsect = itemView.findViewById(R.id.btnInsect);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    /*Interface OnJournalClickListener aufrufen.
                    If-Abfrage, damit die App nicht crasht während man auf ein Item clickt, welches
                    gerade per Swipe entfernt wird. Dies würde nämlich Position -1(NO_POSITION) zurückgeben und
                    in Verbindung mit einem documentSnapshop zum Absturz der App führen.*/
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }

                }
            });
        }
    }


    public interface OnHochsitzClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }


    public void setOnHochsitzClickListener(HochsitzAdapter.OnHochsitzClickListener listener){
        this.listener = listener;
    }
}
