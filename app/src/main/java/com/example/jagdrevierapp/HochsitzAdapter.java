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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jagdrevierapp.data.model.Journal;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
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
import com.google.firebase.firestore.SetOptions;

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
            hochsitzHolder.btnBook.setBackgroundColor(Color.RED);

        } else{
            hochsitzHolder.btnStatus.setBackgroundResource(R.drawable.hochsitz_frei);
            hochsitzHolder.btnBook.setBackgroundColor(Color.GREEN);
                    }

        if(hochsitz.isDamaged()) {
            hochsitzHolder.btnDamage.setBackgroundColor(Color.RED);
        } else {
            hochsitzHolder.btnDamage.setBackgroundColor(Color.GREEN);
        }

        if(hochsitz.isInsectious()) {
            hochsitzHolder.btnInsect.setBackgroundColor(Color.RED);
        } else {
            hochsitzHolder.btnInsect.setBackgroundColor(Color.GREEN);
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
                    hochsitzHolder.btnStatus.setBackgroundResource(R.drawable.hochsetz_besetzt);
                    hochsitzHolder.btnBook.setBackgroundColor(Color.RED);
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
                    hochsitzHolder.btnStatus.setBackgroundResource(R.drawable.hochsitz_frei);
                    hochsitzHolder.btnBook.setBackgroundColor(Color.GREEN);
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


        hochsitzHolder.txtHochsitzName.setText(hochsitz.getHochsitzName());
    }


    @NonNull
    @Override
    public HochsitzHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.hochsitz_layout, parent, false);
        return new HochsitzHolder(v);
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
            txtHochsitzName = itemView.findViewById(R.id.textHochsitzName);
            btnBook = itemView.findViewById(R.id.btnBook);
            btnDamage = itemView.findViewById(R.id.btnDamage);
            btnInsect = itemView.findViewById(R.id.btnInsect);
        }
    }


    public interface OnHochsitzClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }


    public void setOnHochsitzClickListener(HochsitzAdapter.OnHochsitzClickListener listener){
        this.listener = listener;
    }
}
