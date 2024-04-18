package com.example.m08_uf2_p3_fatimafirdawsi_joelvinansaca;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.m08_uf2_p3_fatimafirdawsi_joelvinansaca.databinding.ActivityCoordinatesBinding;

public class CoordinatesActivity extends AppCompatActivity {
    private ActivityCoordinatesBinding binding;

    EditText longitudeField;
    EditText latitudeField;
    Button btn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCoordinatesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        longitudeField = binding.longitudeInput;
        latitudeField = binding.latitudeInput;
        btn = binding.getCoordinatesButton;

        btn.setOnClickListener(view -> {
            String longitudeString = String.valueOf(longitudeField.getText());
            String latitudeString = String.valueOf(latitudeField.getText());

            if (latitudeString.isEmpty() || latitudeString.isEmpty()){
                if (longitudeString.isEmpty()){
                    longitudeField.setError("Field cannot be empty");
                }
                if (latitudeString.isEmpty()){
                    latitudeField.setError("Field cannot be empty");
                }
            } else {
                double longitude = Double.parseDouble(longitudeString);
                double latitude = Double.parseDouble(latitudeString);

                if (latitude < -90 || latitude > 90) {
                    latitudeField.setError("Latitude must be between -90 and 90");
                } else if (longitude < -180 || longitude > 180){
                    longitudeField.setError("Longitude must be between -180 and 180");
                } else {
                    Location location = new Location("latitude, longitude");
                    location.setLongitude(longitude);
                    location.setLatitude(latitude);

                    Intent intent = new Intent(CoordinatesActivity.this, CameraActivity.class);
                    intent.putExtra("location", (Parcelable) location);
                    startActivity(intent);
                }
            }
        });
    }
}
