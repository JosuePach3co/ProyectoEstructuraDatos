package com.example.estructuradatos;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditAirportActivity extends AppCompatActivity {
    private FlightManager flightManager;
    private Airport airport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_airport);

        String originalIata = getIntent().getStringExtra("iata");
        flightManager = FlightManager.getInstance();
        airport = flightManager.getFlightGraph().findAirport(originalIata);

        EditText etName = findViewById(R.id.etAirportName);
        EditText etIata = findViewById(R.id.etAirportIata);
        Button btnSave = findViewById(R.id.btnSaveAirport);

        if (airport != null) {
            etName.setText(airport.getName());
            etIata.setText(airport.getIataCode());
        }

        btnSave.setOnClickListener(v -> {
            if (airport != null) {
                String newName = etName.getText().toString().trim();
                String newIata = etIata.getText().toString().trim().toUpperCase();

                if (newName.isEmpty() || newIata.isEmpty()) {
                    Toast.makeText(this, "El nombre y el código IATA no pueden estar vacíos", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validar que no exista otro aeropuerto con el mismo IATA
                Airport existing = flightManager.getFlightGraph().findAirport(newIata);
                if (existing != null && !existing.getIataCode().equals(originalIata)) {
                    Toast.makeText(this, "Ya existe un aeropuerto con ese código IATA", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Actualizar valores
                airport.setName(newName);

                // Si cambió el IATA, re-clavea en el mapa sin tocar los vuelos
                if (!originalIata.equals(newIata)) {
                    boolean ok = flightManager.getFlightGraph().changeIata(originalIata, newIata);
                    if (!ok) {
                        Toast.makeText(this, "Ya existe un aeropuerto con ese IATA o no se pudo actualizar", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Toast.makeText(this, "Aeropuerto actualizado", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
