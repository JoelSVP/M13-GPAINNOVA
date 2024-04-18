package com.example.m08_uf2_p3_fatimafirdawsi_joelvinansaca;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class SeabotDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seabot_details);

        String seabotNombre = getIntent().getStringExtra("SEABOT_NAME");
        String seabotUbicacion = getIntent().getStringExtra("SEABOT_UBICACION");
        String seabotBasura = getIntent().getStringExtra("SEABOT_BASURA");
        String seabotEstado = getIntent().getStringExtra("SEABOT_ESTADO");


        TextView seabotNombreTV = findViewById(R.id.seabot);
        TextView seabotUbicacionTV = findViewById(R.id.ubicacionString);
        TextView seabotBasuraTV = findViewById(R.id.cantidadBasuraString);
        TextView seabotEstadoTV = findViewById(R.id.estadoActualString);

        seabotNombreTV.setText(seabotNombre);
        seabotUbicacionTV.setText(seabotUbicacion);
        seabotBasuraTV.setText(seabotBasura);
        seabotEstadoTV.setText(seabotEstado);

        connectToDatabase();

        ImageButton backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void connectToDatabase(){
        db.collection("seabots")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.contains("state") && document.contains("canitdadBasura") && document.contains("ubicacion")) {
                                    TextView estado = findViewById(R.id.estadoActualString);
                                    TextView ubicacion = findViewById(R.id.ubicacionString);
                                    TextView cantidadDeBasura = findViewById(R.id.cantidadBasuraString);

                                    String state = document.getString("estado");
                                    String ubication = document.getString("ubicacion");
                                    long cantidadBasura = document.getLong("canitdadBasura");

                                    estado.setText(state);
                                    ubicacion.setText(ubication);
                                    cantidadDeBasura.setText(String.valueOf(cantidadBasura));

                                    Log.e(TAG, "onSuccess: " + state);
                                }

                            }
                        }
                    }
                });

    }

}
