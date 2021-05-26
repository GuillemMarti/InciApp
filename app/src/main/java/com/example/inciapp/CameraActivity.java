package com.example.inciapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CameraActivity extends AppCompatActivity {

    //Initalize variables
    ImageView cameraView;
    Button btCamera, btUpload;
    ProgressBar progressBar;
    Spinner options;
    TextInputEditText description;


    FusedLocationProviderClient locationProviderClient;
    Map<String, Object> map = new HashMap<>();
    Map<String, Object> map2 = new HashMap<>();
    FirebaseFirestore db;
    StorageReference storageReference;
    Intent previous;
    List<Address> address;
    Uri captureImage;
    UploadTask uploadTask;
    String randomUUID;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //Assign variables
        cameraView = findViewById(R.id.camera_view);
        btCamera = findViewById(R.id.bt_camera);
        btUpload = findViewById(R.id.bt_upload);
        progressBar = findViewById(R.id.progress_horizontal);
        options = findViewById(R.id.opt_list);
        description = findViewById(R.id.description);

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        previous = getIntent();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.opt_list, R.layout.support_simple_spinner_dropdown_item);
        options.setAdapter(adapter);

        //Get permission for camera and location
        getPermissions();

        //Button listeners
        btCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(CameraActivity.this , Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                //Open camera
                openCamera();
            }

        });

        btUpload.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(CameraActivity.this , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                randomUUID = UUID.randomUUID().toString();
                locationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                    Location location = task.getResult();
                    try {
                        Geocoder geocoder = new Geocoder(CameraActivity.this,
                                Locale.getDefault());
                        address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                if (uploadTask != null && uploadTask.isInProgress()){
                    Toast.makeText(getApplicationContext(), "Ya se está realizando una subida",Toast.LENGTH_SHORT).show();
                }else{
                    uploadImage();
                }
            }

        });

    }

    //Asks for Camera and Location permissions
    private void getPermissions() {
        //Request for permissions
        if ((ContextCompat.checkSelfPermission(CameraActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) || ContextCompat.checkSelfPermission(CameraActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(CameraActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA
                    },
                    100);
        }
    }

    //Sets the image of the camera into the image View
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            cameraView.setImageURI(captureImage);
        }
    }

    //Open camera
    private void openCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From camera");
        captureImage = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, captureImage);
        startActivityForResult(intent, 100);
    }

    //Gets the extension of an uri
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    //Uploads the image to the cloud storage
    private void uploadImage() {
        if (captureImage != null){

            StorageReference reference = storageReference.child("images/" + randomUUID + "." + getFileExtension(captureImage)) ;

            uploadTask = (UploadTask) reference.putFile(captureImage)
                    .addOnSuccessListener(taskSnapshot -> {

                        Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            progressBar.setProgress(0);
                            captureImage = null;
                            cameraView.setImageURI(null);
                            description.setText(null);
                            options.setSelection(0);
                        }, 1500);
                        Toast.makeText(getApplicationContext(), "Imagen subida", Toast.LENGTH_SHORT).show();
                        updateDB();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Error durante la subida", Toast.LENGTH_SHORT).show())
                    .addOnProgressListener(snapshot -> {
                        double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressBar.setProgress((int)progressPercent);
                    });
        }else{
            Toast.makeText(getApplicationContext(), "Primero debes hacer una foto", Toast.LENGTH_LONG).show();
        }

    }

    //Maps all the fields to be uploaded in the database
    //If a locality is not created, it creates an id for its collection
    private void updateDB() {
        map.put("Id", randomUUID);
        map.put("User", previous.getStringExtra("email"));
        map.put("Latitude", address.get(0).getLatitude());
        map.put("Longitude", address.get(0).getLongitude());
        map.put("Description", Objects.requireNonNull(description.getText()).toString());
        if (options.getSelectedItem().toString().equals("Tipo Incidencia")){
            map.put("Type", "Otro");
        }else{
            map.put("Type", options.getSelectedItem().toString());
        }
        map.put("Under reparation", false);
        db.collection("incidents").document(address.get(0).getLocality()).get().addOnSuccessListener(v ->{
            if (v.get("Id")==null){
                map2.put("Id", address.get(0).getLocality());
                db.collection("incidents").document(address.get(0).getLocality()).set(map2);
            }
        });
        db.collection("incidents").document(address.get(0).getLocality()).collection("local incidents").document(randomUUID).set(map);
    }
}