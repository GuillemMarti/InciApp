package com.example.inciapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;


public class LoginScreenActivity extends AppCompatActivity {

    //Initalize variables
    private TextInputEditText email, password;
    FirebaseFirestore db;
    ImageView logoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        //Assign variables
        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
        db = FirebaseFirestore.getInstance();
        logoView = findViewById(R.id.logo_imageView);

    }

    // Intent to go to RegisterActivity
    public void onClickRegistrar(View v){
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    //When the button is pressed, checks the type of the user and logs in a different activiy according to it
    public void onClickLogin(View v){
        if ((email.getText().toString().length()>0) && (password.getText().toString().length()>0)){
            FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            db.collection("users").document(email.getText().toString()).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                       if  (Objects.equals(documentSnapshot.get("Admin"), true)){
                                           goAdminHome();
                                       }else if(Objects.equals(documentSnapshot.get("Operator"), true)){
                                           goCuadrillaHome();
                                       }else{
                                           goHome();
                                        }
                                    });
                        }else{
                            Toast.makeText(getApplicationContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    //Intent for the operator users
    private void goCuadrillaHome() {
        Intent goCuadrillaHome = new Intent(this, OptActivity.class);
        goCuadrillaHome.putExtra("id", email.getText().toString());
        startActivity(goCuadrillaHome);
    }

    //Intent for the admin users
    private void goAdminHome() {
        Intent goAdminHome = new Intent(this, AdminActivity.class);
        startActivity(goAdminHome);
    }

    //Intent for the normal users
    public void goHome(){
        Intent goHome = new Intent(this, CameraActivity.class);
        goHome.putExtra("email", email.getText().toString());
        startActivity(goHome);
    }
}