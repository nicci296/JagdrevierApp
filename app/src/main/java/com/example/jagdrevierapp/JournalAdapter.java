package com.example.jagdrevierapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jagdrevierapp.data.model.Journal;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class JournalAdapter extends FirestoreRecyclerAdapter<Journal, JournalAdapter.JournalHolder> {


    public JournalAdapter(FirestoreRecyclerOptions<Journal> options) {
        super(options);
    }


    @Override
    protected void onBindViewHolder(JournalHolder journalHolder, int i, Journal journal) {


        journalHolder.shotView.setText("Schüsse: " + journal.getShots());
        journalHolder.hitView.setText("Treffer: " + journal.getHits());
        journalHolder.caliberView.setText("Kaliber:" + String.valueOf(journal.getCaliber()));
        journalHolder.targetView.setText("Ziel: " + journal.getTarget());
        journalHolder.meanView.setText("Zweck: " + journal.getMean());
        journalHolder.dateView.setText(journal.getDate());
        journalHolder.locationView.setText(journal.getLocation().toString());

    }

    @NonNull
    @Override
    public JournalHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.journal_item,parent,false);
        return new JournalHolder(v);
    }

    public void deleteItem(int position){
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class JournalHolder extends RecyclerView.ViewHolder{
       TextView shotView;
       TextView hitView;
       TextView caliberView;
       TextView targetView;
       TextView meanView;
       TextView dateView;
       TextView locationView;

       public JournalHolder(View itemView) {
           super(itemView);
           shotView = itemView.findViewById(R.id.shot_View);
           hitView = itemView.findViewById(R.id.hit_View);
           caliberView = itemView.findViewById(R.id.caliber_View);
           targetView = itemView.findViewById(R.id.target_View);
           meanView = itemView.findViewById(R.id.mean_View);
           dateView = itemView.findViewById(R.id.date_View);
           locationView = itemView.findViewById(R.id.location_View);
       }
   }
}
