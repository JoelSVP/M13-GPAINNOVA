package com.example.m08_uf2_p3_fatimafirdawsi_joelvinansaca;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SeabotDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seabot_details);

        String seabotNombre = getIntent().getStringExtra("SEABOT_NAME");
        String seabotUbicacion = getIntent().getStringExtra("SEABOT_UBICACION");
        String seabotBasura = getIntent().getStringExtra("SEABOT_BASURA");
        String seabotEstado = getIntent().getStringExtra("SEABOT_ESTADO");
        String seabotDistancia = getIntent().getStringExtra("SEABOT_DISTANCIA");


        TextView seabotNombreTV = findViewById(R.id.seabot);
        TextView seabotUbicacionTV = findViewById(R.id.ubicacionString);
        TextView seabotBasuraTV = findViewById(R.id.cantidadBasuraString);
        TextView seabotEstadoTV = findViewById(R.id.estadoActualString);
        TextView seabotDistanciaTV = findViewById(R.id.distanciaRecorridaString);

        seabotNombreTV.setText(seabotNombre);
        seabotUbicacionTV.setText(seabotUbicacion);
        seabotBasuraTV.setText(seabotBasura);
        seabotEstadoTV.setText(seabotEstado);
        seabotDistanciaTV.setText(seabotDistancia);

        ImageButton backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }



}
