package com.example.inciapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class

MapActivity extends FragmentActivity implements OnMapReadyCallback {

    //Initialize variables
    GoogleMap map;
    SupportMapFragment mapFragment;
    FirebaseFirestore db;
    Spinner filter, localities;
    Button updateBtn, cuadrillaBtn;
    List<String> listLocalities = new ArrayList<>();
    List<LatLng> coords = new ArrayList<>();
    List<Marker> markers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //Assign variables
        filter = findViewById(R.id.filt_list);
        localities = findViewById(R.id.locality_list);
        updateBtn = findViewById(R.id.update_button);
        cuadrillaBtn = findViewById(R.id.assign_cuadrillaButton);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        db = FirebaseFirestore.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.opt_list, R.layout.support_simple_spinner_dropdown_item);
        filter.setAdapter(adapter);


        getLocalities();
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, listLocalities);
        localities.setAdapter(adapter1);

        updateBtn.setOnClickListener(v -> {
            if (localities.getSelectedItem().toString().equals("Municipio")) {
                Toast.makeText(getApplicationContext(), "No se ha seleccionado un municipio", Toast.LENGTH_SHORT).show();
            } else {

                getLocalIncidencies();

                //Print the incidents on the map and clean the list for the next execution
                if (coords != null) {
                    //Clear the markers of the previous execution
                    removeMarkers();
                    for (LatLng coord : coords) {
                        markers.add(map.addMarker(new MarkerOptions().position(coord)));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 13));
                    }
                    coords.clear();
                }
            }
        });

        cuadrillaBtn.setOnClickListener(v -> {
            if (filter.getSelectedItem().toString().equals("Tipo Incidencia") || (localities.getSelectedItem().toString().equals("Municipio"))) {
                Toast.makeText(getApplicationContext(), "Se debe especificar tipo incidencia y municipio", Toast.LENGTH_LONG).show();
            } else {
                Intent goAdmin = new Intent(MapActivity.this, AdminActivity.class);
                goAdmin.putExtra("incidency", filter.getSelectedItem().toString());
                goAdmin.putExtra("locality", localities.getSelectedItem().toString());
                startActivity(goAdmin);
            }
        });
        mapFragment.getMapAsync(this);
    }


    private void getLocalIncidencies() {
        //Filter the results according to the type of incidency
        if (filter.getSelectedItem().toString().equals("Tipo Incidencia")) {
            //We get all the incidents in the locality
            db.collection("incidents").document(localities.getSelectedItem().toString()).collection("local incidents").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                            LatLng mapCoord = new LatLng(queryDocumentSnapshot.getDouble("Latitude"), queryDocumentSnapshot.getDouble("Longitude"));
                            coords.add(mapCoord);
                        }
                    });
        } else {
            //We get filtered incidents in the locality
            db.collection("incidents").document(localities.getSelectedItem().toString()).collection("local incidents").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                            if ((Objects.equals(queryDocumentSnapshot.getString("Type"), filter.getSelectedItem().toString())) && (Objects.equals(queryDocumentSnapshot.get("Under reparation"), false))) {
                                LatLng mapCoord = new LatLng(queryDocumentSnapshot.getDouble("Latitude"), queryDocumentSnapshot.getDouble("Longitude"));
                                coords.add(mapCoord);
                            }
                        }
                    });
        }
    }

    //Removes map markers
    private void removeMarkers() {
        if (markers.size() > 0) {
            for (Marker marker : markers) {
                marker.remove();
            }
        }
    }

    //Gets all the localities currently saved in the database
    private void getLocalities() {
        listLocalities.clear();
        listLocalities.add("Municipio");
        db.collection("incidents").get().addOnSuccessListener(values -> {
            for (DocumentSnapshot value : values) {
                listLocalities.add(value.getString("Id"));
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
    }
}