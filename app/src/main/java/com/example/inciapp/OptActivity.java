package com.example.inciapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class OptActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Initialize variables
    GoogleMap map;
    SupportMapFragment mapFragment;
    FirebaseFirestore db;
    Intent previous;
    String locality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opt);

        //Assign variables
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        db = FirebaseFirestore.getInstance();
        previous = getIntent();

        setMapMarkers();

        mapFragment.getMapAsync(this);


    }

    //Gets the locality where the operator works
    private void setMapMarkers() {
        db.collection("users").document(previous.getStringExtra("id")).get()
                .addOnSuccessListener(task -> {
                    locality = task.getString("Locality");
                    getCorresponding();
                });
    }

    //Get the corresponding incidencies on the map, checking from all local incidencies and the asigned incidencies
    private void getCorresponding() {

        db.collection("users").document(previous.getStringExtra("id")).collection("asigned incidents").get()
                .addOnSuccessListener(asignedIncidencies -> {
                    for (QueryDocumentSnapshot asignedIncidency : asignedIncidencies){
                        db.collection("incidents").document(locality).collection("local incidents").get()
                                .addOnSuccessListener(localIncidencies -> {
                                    for (QueryDocumentSnapshot localIncidency : localIncidencies){
                                        if (localIncidency.getId().equals(asignedIncidency.getId())){
                                            LatLng mapCoord = new LatLng(localIncidency.getDouble("Latitude"), localIncidency.getDouble("Longitude"));
                                            map.addMarker(new MarkerOptions().position(mapCoord).title(localIncidency.getId()));
                                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(mapCoord, 13));
                                        }
                                    }
                                });
                    }
                    markerClick();
                });
    }

    //Sets the listeners for the markers when are retrieved from the database
    private void markerClick() {
        map.setOnMarkerClickListener(marker -> {
            String id = marker.getTitle();

            Intent goDetails = new Intent(OptActivity.this, DetailsActivity.class);
            goDetails.putExtra("id", id);
            goDetails.putExtra("op", previous.getStringExtra("id"));
            startActivity(goDetails);

            return false;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
    }
}