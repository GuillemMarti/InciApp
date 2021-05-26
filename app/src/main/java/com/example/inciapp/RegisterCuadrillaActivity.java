package com.example.inciapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterCuadrillaActivity extends AppCompatActivity {

    //Initalize variables
    TextInputEditText emailView, passwordView, repeatPasswordView, telephoneView, localityView;
    String email, password, password2, telephone, locality;
    FirebaseAuth fAuth;
    FirebaseFirestore db;
    Map<String, Object> map = new HashMap<>();
    Button registerBtn;
    String errorMsg = "Las contraseñas no coinciden";
    String errorMsg2 = "Se ha producido un error al intentar registrar la cuadrilla";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_cuadrilla);

        //Assign variables
        emailView = findViewById(R.id.cuadrilla_registerEditText);
        passwordView = findViewById(R.id.cuadrilla_passwordEditText);
        repeatPasswordView = findViewById(R.id.cuadrilla_repeatPasswordEditText);
        telephoneView = findViewById(R.id.cuadrilla_telephoneEditText);
        localityView = findViewById(R.id.cuadrilla_localityEditText);
        registerBtn = findViewById(R.id.registerCuadrillaButton);

        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registerBtn.setOnClickListener(v -> {
            email = emailView.getText().toString().trim();
            password = passwordView.getText().toString().trim();
            password2 = repeatPasswordView.getText().toString().trim();
            telephone = telephoneView.getText().toString().trim();
            locality = localityView.getText().toString().trim();

            if (TextUtils.isEmpty(email)){
                emailView.setError("Introduce un email.");
                return;
            }

            if (TextUtils.isEmpty(password)){
                passwordView.setError("Introduce una contraseña.");
                return;
            }

            if (password.length() < 6){
                passwordView.setError("La contraseña debe contener al menos 6 caracteres.");
                return;
            }

            if (TextUtils.isEmpty(password2)){
                repeatPasswordView.setError("Introduce una contraseña.");
                return;
            }

            if (TextUtils.isEmpty(telephone)){
                telephoneView.setError("Introduce un nombre y apellidos.");
                return;
            }

            if (TextUtils.isEmpty(locality)){
                localityView.setError("Introduce un número de teléfono.");
                return;
            }

            if (password.equals(password2)){

                fAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                map.put("Id", email);
                                map.put("Locality", locality);
                                map.put("Telephone", telephone);
                                map.put("Admin", false);
                                map.put("Operator", true);
                                map.put("Asigned", false);
                                db.collection("users").document(email).set(map);
                                Toast.makeText(getApplicationContext(),"Cuadrilla registrada", Toast.LENGTH_LONG).show();
                                goAdmin();
                            }else{
                                Toast.makeText(getApplicationContext(), errorMsg2, Toast.LENGTH_LONG).show();
                            }
                        });
            }else{
                Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goAdmin() {
        Intent goAdmin = new Intent(RegisterCuadrillaActivity.this, AdminActivity.class);
        startActivity(goAdmin);
    }
}