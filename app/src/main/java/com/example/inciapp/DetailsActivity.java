package com.example.inciapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class DetailsActivity extends AppCompatActivity {

    //Initialize variables
    TextView typeTextView, descriptionTextView;
    ImageView imageView;
    Button repairedButton;
    FirebaseFirestore db;
    StorageReference storageReference;
    Intent previous;
    String locality;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //Assign variables
        typeTextView = findViewById(R.id.tipoIncidencia_TextView);
        descriptionTextView = findViewById(R.id.descripcion_TextView);
        imageView = findViewById(R.id.imagen_incidencia);
        repairedButton = findViewById(R.id.incidenciaReparada_Button);

        previous = getIntent();

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference().child("images");


        setInfoViews();


    }

    private void setInfoViews() {
        db.collection("users").document(previous.getStringExtra("op")).get()
                .addOnSuccessListener(task -> {
                    locality = task.getString("Locality");
                    getCorresponding();
                });
    }

    private void getCorresponding() {

        db.collection("incidents").document(locality).collection("local incidents").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots){
                        if (queryDocumentSnapshot.getId().equals(previous.getStringExtra("id"))){
                            id = queryDocumentSnapshot.getId();
                            typeTextView.setText(queryDocumentSnapshot.getString("Type"));
                            descriptionTextView.setText(queryDocumentSnapshot.getString("Description"));
                            getImageFromStorage();
                        }
                    }
                    setOnClick();
                });
    }

    private void setOnClick() {
        repairedButton.setOnClickListener(v -> {
            storageReference.child(previous.getStringExtra("id")+".jpg").delete();
            db.collection("incidents").document(locality).collection("local incidents").document(id).delete();
            db.collection("users").document(previous.getStringExtra("op")).collection("asigned incidents").document(previous.getStringExtra("id")).delete();
            db.collection("users").document(previous.getStringExtra("op")).collection("asigned incidents").document(previous.getStringExtra("id")).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()){
                            db.collection("users").document(previous.getStringExtra("op")).update("Asigned", false);
                        }
                    });
            Intent goCuadrillaHome = new Intent(this, OptActivity.class);
            goCuadrillaHome.putExtra("id", previous.getStringExtra("op"));
            startActivity(goCuadrillaHome);
        });
    }

    private void getImageFromStorage( ) {
            storageReference.child(previous.getStringExtra("id")+".jpg").getDownloadUrl().addOnSuccessListener(uri -> {
                Picasso.get().load(uri).rotate(90f).into(imageView);
            });
    }
}