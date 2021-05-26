package com.example.inciapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AdminActivity extends AppCompatActivity {

    //Initalize variables
    Intent previous;
    TextView incidency, locality;
    Button mapBtn, assignBtn, registerBtn;
    Spinner listCuadrilla;
    List<String> list = new ArrayList<>();
    List<String> idIncidencies = new ArrayList<>();
    Map<String, Object> map = new HashMap<>();
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        //Assign variables
        previous = getIntent();
        incidency = findViewById(R.id.incidencia_seleccionada);
        locality = findViewById(R.id.municipio_seleccionado);
        mapBtn = findViewById(R.id.map_button);
        assignBtn = findViewById(R.id.assign_cuadrillaButton2);
        registerBtn = findViewById(R.id.registerCuadrillaButton2);
        listCuadrilla = findViewById(R.id.list_cuadrilla);

        db = FirebaseFirestore.getInstance();

        //Get the values for the spinner
        getLocalCuadrillas();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item,list);
        listCuadrilla.setAdapter(adapter);

        //If we come from MapActivity, set text in textViews
        setTextViewValues();

        mapBtn.setOnClickListener(v -> goMap());

        assignBtn.setOnClickListener(v -> assignCuadrilla());

        registerBtn.setOnClickListener(v -> goRegister());
    }

    //Assigns the selected local operator to the selected local incidencies
    private void assignCuadrilla() {
        if (listCuadrilla.getSelectedItem().toString().equals("Cuadrillas disponibles")) {
            Toast.makeText(getApplicationContext(), "Se debe seleccionar una cuadrilla disponible", Toast.LENGTH_LONG).show();
        }else{
            //Update the asigned incidencies
            updateLocalIncidencies();
            //Update the operator to display that is already asigned
            db.collection("users").document(listCuadrilla.getSelectedItem().toString()).update("Asigned", true);
            Toast.makeText(getApplicationContext(), "Cuadrilla asignada", Toast.LENGTH_SHORT).show();
        }
    }


    //Gets the filtered incidents in the locality
    private void updateLocalIncidencies() {
        idIncidencies.clear();
        db.collection("incidents").document(locality.getText().toString()).collection("local incidents").get()
                .addOnSuccessListener(DocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : DocumentSnapshots){
                        if (Objects.equals(documentSnapshot.get("Type"), incidency.getText())){
                            db.collection("incidents").document(locality.getText().toString()).collection("local incidents").document(documentSnapshot.getId()).update("Under reparation", true);
                            map.put("Id", documentSnapshot.getId());
                            db.collection("users").document(listCuadrilla.getSelectedItem().toString()).collection("asigned incidents").document(documentSnapshot.getId()).set(map);
                        }
                    }
                });
    }

    //Gets the local operators
    private void getLocalCuadrillas() {
        list.clear();
        list.add("Cuadrillas disponibles");
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                if ((Objects.equals(documentSnapshot.get("Locality"), locality.getText())) && (Objects.equals(documentSnapshot.get("Asigned"), false))){
                    list.add(documentSnapshot.getId());
                }
            }
        });
    }

    //Change activity
    private void goRegister() {
        Intent goRegister = new Intent(AdminActivity.this, RegisterCuadrillaActivity.class);
        startActivity(goRegister);
    }

    //If there are extra arguments in the intent, place the incidency and locality in the text views
    private void setTextViewValues() {
        if (previous.getStringExtra("incidency")!=null){
            incidency.setText(previous.getStringExtra("incidency"));
        }else{
            incidency.setText(R.string.no_seleccionado);
        }

        if (previous.getStringExtra("locality")!=null){
            locality.setText(previous.getStringExtra("locality"));
        }else{
            locality.setText(R.string.no_seleccionado);
        }
    }

    //Change activity
    private void goMap(){
        Intent goMap = new Intent(AdminActivity.this, MapActivity.class);
        startActivity(goMap);
    }

}

