package com.example.m08_uf2_p3_fatimafirdawsi_joelvinansaca;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.example.m08_uf2_p3_fatimafirdawsi_joelvinansaca.databinding.ActivityMapsBinding;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    //private FloatingActionButton fab;
    private Location lastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage;
    StorageReference storageRef;
    private final LatLng defaultLocation = new LatLng(41.38879, 2.15899);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("images/");
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MapsActivity.this, AuthActivity.class));
                finish();
            }
        });
        //fab = findViewById(R.id.fab);
        /*fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Confirmation")
                        .setMessage("Do you want to use your current location?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MapsActivity.this, CameraActivity.class);
                                intent.putExtra("location", (Parcelable) lastKnownLocation);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("set custom location ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(MapsActivity.this, CoordinatesActivity.class));
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
         */
    }

    /*
    private Bitmap createDrawableFromView() {
        View customMarkerView = getLayoutInflater().inflate(R.layout.custom_location_marker, null);

        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(customMarkerView.getWidth(), customMarkerView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        customMarkerView.draw(canvas);
        return bitmap;
    }

     */


    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        db.collection("seabots")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.contains("lat") && document.contains("lon") ) {
                                    double lat = document.getDouble("lat");
                                    double lon = document.getDouble("lon");

                                    /*DecimalFormat df = new DecimalFormat("#.#####");
                                    String latStr = df.format(lat);
                                    String lonStr = df.format(lon);*/

                                    //String fileName = "images/" + latStr + "_" + lonStr + ".jpg";

                                    LatLng position = new LatLng(lat, lon);

                                    //StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(fileName);

                                    storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            //Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                            //int width = 100;
                                            //int height = 100;
                                            //Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, width, height, false);
                                            //BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(smallMarker);

                                            String seabotName = document.getString("seabot");
                                            mMap.addMarker(new MarkerOptions()
                                                    .position(position)
                                                    .title(seabotName));
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Log.d(TAG, "onFailure: ERROR");
                                        }
                                    });
                                }
                            }
                        } else {
                            Log.w(TAG, "Error at getting documents", task.getException());
                        }
                    }
                });

        getLocationPermission();

        mMap.setOnMarkerClickListener((Marker marker) -> {
            String seabotName = marker.getTitle();
            db.collection("seabots")
                    .whereEqualTo("seabot", seabotName)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String ubicacion = document.getString("ubicacion");
                                    long cantidadBasura = document.getLong("cantidadBasura");
                                    String estado = document.getString("estado");

                                    Intent intent = new Intent(MapsActivity.this, SeabotDetailsActivity.class);
                                    intent.putExtra("SEABOT_NAME", seabotName);
                                    intent.putExtra("SEABOT_UBICACION", ubicacion);
                                    intent.putExtra("SEABOT_BASURA", String.valueOf(cantidadBasura));
                                    intent.putExtra("SEABOT_ESTADO", estado);
                                    startActivity(intent);
                                }
                            } else {
                                Log.e(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
            return false;
        });


        updateLocationUI();
        getDeviceLocation();
        //populateMap(getPictures());
    }


    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    /*
    public Map<LatLng, StorageReference> getPictures() {
        Map<LatLng, StorageReference> fileMap = new HashMap<>();

        storageRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                String filename = item.getName();
                fileMap.put(getLocationFromFileName(filename), item);
            }
        }).addOnFailureListener(e -> {
            Log.e("TAGGG", "line: 212" + e.getMessage());
        });

        return fileMap;
    }
     */

    /*
    public LatLng getLocationFromFileName(String pictureName) {
        pictureName = pictureName.replace(".jpg", "");
        String[] pictureNameSplit = pictureName.split("_");

        Double latitude = Double.parseDouble(pictureNameSplit[0].replace(",", "."));
        Double longitude = Double.parseDouble(pictureNameSplit[1].replace(",", "."));
        return new LatLng(latitude, longitude);
    }
     */

    /*@Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            populateMap(getPictures());
        }
    }

     */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth.getInstance().signOut();
        finish();
    }

    /*public void populateMap(Map<LatLng, StorageReference> photos) {
        for (LatLng key : photos.keySet()) {
            File localFile = null;
            try {
                localFile = File.createTempFile("images", "jpg", getApplicationContext().getCacheDir());
                File finalLocalFile = localFile;
                photos.get(key).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap = BitmapFactory.decodeFile(finalLocalFile.getAbsolutePath());

                        int height = 150;
                        int width = 150;
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(key)
                                .title(photos.get(key).getName())
                                .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
                        mMap.addMarker(markerOptions);
                    }
                }).addOnFailureListener((@NonNull Exception exception) -> {
                    Log.e("TAGGG", "line: 256" + exception.getMessage());
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

     */
}