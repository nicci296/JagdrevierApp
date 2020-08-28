package com.example.jagdrevierapp;

import com.example.jagdrevierapp.data.model.Hochsitz;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.google.firebase.firestore.DocumentSnapshot;

public class HochsitzAdapter extends FirestoreRecyclerAdapter<Hochsitz, HochsitzAdapter.HochsitzHolder> {

    public OnHochsitzClickListener listener;

    // Constructor of Superclass
    public HochsitzAdapter(FirestoreRecyclerOptions<Hochsitz> options) {
        super(options);
    }


    @Override
    protected void onBindViewHolder(final HochsitzHolder hochsitzHolder, int position, final Hochsitz hochsitz) {
        if(hochsitz.isBooked()) {
            hochsitzHolder.btnStatus.setBackgroundResource(R.drawable.hochsetz_besetzt);
            hochsitzHolder.btnBook.setBackgroundColor(Color.RED);
        } else {
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

        hochsitzHolder.btnStatus.setWidth(50);
        hochsitzHolder.btnStatus.setHeight(50);

        hochsitzHolder.btnBook.setWidth(50);
        hochsitzHolder.btnBook.setHeight(50);

        hochsitzHolder.btnDamage.setWidth(50);
        hochsitzHolder.btnDamage.setHeight(50);

        hochsitzHolder.btnInsect.setWidth(50);
        hochsitzHolder.btnInsect.setHeight(50);


        hochsitzHolder.btnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), StatusPop.class);
                Bundle extras = new Bundle();
                extras.putString("sitzname", hochsitz.getHochsitzName());
                extras.putString("booker", hochsitz.getBookedBy());
                extras.putBoolean("booked", hochsitz.isBooked());
                extras.putBoolean("damage", hochsitz.isDamaged());
                extras.putBoolean("insect", hochsitz.isInsectious());
                intent.putExtras(extras);
                view.getContext().startActivity(intent);
            }
        });



        hochsitzHolder.btnBook.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(!hochsitz.isBooked()) {
                    hochsitzHolder.btnStatus.setBackgroundResource(R.drawable.hochsetz_besetzt);
                    hochsitzHolder.btnBook.setBackgroundColor(Color.RED);
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
