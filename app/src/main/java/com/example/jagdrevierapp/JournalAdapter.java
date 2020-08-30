package com.example.jagdrevierapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jagdrevierapp.data.model.Journal;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;


/**
 * ***************Nico 24.08.20 ***************************
 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * Damit die in der userbezogenen Schussjournal-Collection liegenden Journaleinträge ordentlich
 * in einer RecyclerView(View zur Darstellung mehrerer Docs) dargestellt werden können, bedarf es folgender Schritte:
 *
 *      1. Implementierung der benötigten Dependencies (s. build.gradle(App))
 *      2. Anlegen und Design einer Layout-XML zur Gestaltung der einzelnen
 *         Einträge der RecyclerView (s. journal_item.xml)
 *      3. Anlegen und Implementierung eines FirestoreRecyclerAdapters, welcher
 *         die datenbank nach den gewünschten Docs abhört, diese holt und an die View weitergibt,
 *         welche in der RecyclerView angezeigt wird.
 *
 *      4. Instanzierung in der Schussjournal Activity (s. selbige)
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

/*
    Der FirestoreRecyclerAdapter erbt vom gängigen RecyclerAdapter und ergänzt diesen um einige Automatismen
    in Bezug auf Google Firebase.
    Der Adapter arbeitet in diesem Fall mit der Modelklasse Journal.java sowie einem Objekt JournalHolder der
    internen Klasse ViewHolder.

 */
public class JournalAdapter extends FirestoreRecyclerAdapter<Journal, JournalAdapter.JournalHolder> {

    public OnJournalClickListener listener;

    //super Constructor
    public JournalAdapter(FirestoreRecyclerOptions<Journal> options) {
        super(options);
    }


    //Callback, welcher die an die journal_item-CardView zu übergebenen Werte pro TextView festlegt
    @Override
    protected void onBindViewHolder(JournalHolder journalHolder, int i, Journal journal) {
        /*
          Kürzen des Strings journal.getLocation().toString() String.
          GeoPoint wird als String
            "GeoPoint latitude= xx.xxxxxxx longitude= x.xxxxxxx"
          ausgegeben, was den Platz der View übersteigt.
          Stattdessen werden über substring() die reinen Koordinaten zusammengeführt.
         */
        String lat = journal.getLocation().toString().substring(20,28);
        String lng = journal.getLocation().toString().substring(40,50);
        String latLng = "lat: "+lat+" , "+"lng: "+lng;


        journalHolder.shotView.setText("Schüsse: " + journal.getShots());
        journalHolder.hitView.setText("Treffer: " + journal.getHits());
        journalHolder.caliberView.setText("Kaliber:" +journal.getCaliber());
        journalHolder.targetView.setText("Ziel: " + journal.getTarget());
        journalHolder.meanView.setText("Zweck: " + journal.getMean());
        journalHolder.dateView.setText(journal.getDate());
        journalHolder.locationView.setText(latLng);
    }

    /*
        Callback, welcher festlegt, an welche View die Journal-daten übergeben werden (journal_item.xml)
        und einen neuen JournalHolder mit dieser View als Parameter zurückgibt.
     */
    @NonNull
    @Override
    public JournalHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.journal_item,parent,false);
        return new JournalHolder(v);
    }

    //Methode zum Löschen eines Journaleintrags aus der View und dem Firestore
    public void deleteItem(int position){

        getSnapshots().getSnapshot(position).getReference().delete();

    }

    /*  Eine eigene ViewHolder-Class, welcher die einzelnen Views der item_layout.xml bekannt gemacht werden.
        Wie der Name vermutet lässt, ist diese Klasse in seinen Callbacks verantwortlich, die Werte der Views
        aufzunehmen und an die RecyclerView mittels des Adapters zu übergeben.
     */
    class JournalHolder extends RecyclerView.ViewHolder{
       TextView shotView;
       TextView hitView;
       TextView caliberView;
       TextView targetView;
       TextView meanView;
       TextView dateView;
       TextView locationView;


       //super Constructor
       public JournalHolder(View itemView) {
           super(itemView);
           shotView = itemView.findViewById(R.id.shot_View);
           hitView = itemView.findViewById(R.id.hit_View);
           caliberView = itemView.findViewById(R.id.caliber_View);
           targetView = itemView.findViewById(R.id.target_View);
           meanView = itemView.findViewById(R.id.mean_View);
           dateView = itemView.findViewById(R.id.date_View);
           locationView = itemView.findViewById(R.id.location_View);

           /**
            * *****************27.08.20 Nico ****************************
            * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
            * OnClickListener bei Click auf einen Eintrag in der RecyclerView.
            * Die einzelnen Karten der View werden per getAdapterPosition()
            * unterschieden.
            * Nutzt das OnJournalClickListener-Interface, welches eine
            * onItemClick- Methode implementiert.
            * Diese kann später in der Schussjournal-Activity überschrieben
            * werden.
            * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
            */
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
   /*
    Interface OnJournalClickLstener und Methode setOnJournalClickListener ermöglichen das Senden
    von Daten aus dem Adapter an die Activity, die das Inteface implementiert.
    */
   public interface OnJournalClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
   }
   public void setOnJournalClickListener(OnJournalClickListener listener){
        this.listener = listener;
   }

}
