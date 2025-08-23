package com.example.estructuradatos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AddFlightActivity extends AppCompatActivity {

    private Spinner spinnerOrigen, spinnerDestino, spinnerAerolinea;
    private Button btnGuardar, btnCancelar;
    private EditText etDuration, etCost;
    private FlightManager flightManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flight);


        spinnerOrigen = findViewById(R.id.spinnerOrigen);
        spinnerDestino = findViewById(R.id.spinnerDestino);
        spinnerAerolinea = findViewById(R.id.spinnerAerolinea);
        etDuration = findViewById(R.id.etDuration);
        etCost = findViewById(R.id.etCost);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);

        flightManager = FlightManager.getInstance();

        // cargar aeropuertos
        ArrayList<Airport> aeropuertos = new ArrayList<>(flightManager.getFlightGraph().getVertices());

        List<String> codigosAeropuertos = new ArrayList<>();
        for (Airport a : aeropuertos) {
            codigosAeropuertos.add(a.getIataCode());
        }

        ArrayAdapter<String> adapterAirports = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, codigosAeropuertos);
        adapterAirports.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrigen.setAdapter(adapterAirports);
        spinnerDestino.setAdapter(adapterAirports);

        // cargar aerolíneas
        List<String> nombresAerolineas = new ArrayList<>();
        for (Airline al : flightManager.getAirlines()) {
            nombresAerolineas.add(al.getName());
        }

        ArrayAdapter<String> adapterAirlines = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombresAerolineas);
        adapterAirlines.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAerolinea.setAdapter(adapterAirlines);

        // Guardar vuelo
        btnGuardar.setOnClickListener(v -> {
            String origenIata = (String) spinnerOrigen.getSelectedItem();
            String destinoIata = (String) spinnerDestino.getSelectedItem();
            String aerolineaName = (String) spinnerAerolinea.getSelectedItem();

            if (origenIata.equals(destinoIata)) {
                Toast.makeText(this, "El origen y destino no pueden ser iguales", Toast.LENGTH_SHORT).show();
                return;
            }

            Airport origen = flightManager.getFlightGraph().findAirport(origenIata);
            Airport destino = flightManager.getFlightGraph().findAirport(destinoIata);
            Airline aerolinea = null;
            for (Airline al : flightManager.getAirlines()) {
                if (al.getName().equals(aerolineaName)) {
                    aerolinea = al;
                }
            }

            // calcular distancia desde metodo centralizado
            double distancia = FlightManager.computeDistance(origen, destino);
            
            double durMin;
            double costVal;
            
            String sDur = etDuration.getText().toString().trim();
            String sCost = etCost.getText().toString().trim();
            if (sDur.isEmpty()) {
                durMin = FlightManager.computeEstimatedDurationMin(distancia);
            } else {
                durMin = Double.parseDouble(sDur);
            }
            if (sCost.isEmpty()) {
                costVal = FlightManager.computeEstimatedCost(distancia);
            } else {
                costVal = Double.parseDouble(sCost);
            }

            // Agregar vuelo al grafo
            boolean agregado = flightManager.addFlight(origenIata, destinoIata, distancia, durMin, costVal, aerolinea);

            if (agregado) {
                Toast.makeText(this, "Vuelo agregado correctamente", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK, new Intent());
                finish();
            } else {
                Toast.makeText(this, "No se pudo agregar el vuelo (quizá ya existe)", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelar.setOnClickListener(v -> finish());
    }
}
