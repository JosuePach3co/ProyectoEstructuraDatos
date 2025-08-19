package com.example.estructuradatos;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class EditFlightActivity extends AppCompatActivity {
    private FlightManager flightManager;
    private Flight flight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_flight);

        String originCode = getIntent().getStringExtra("origin");
        String destinationCode = getIntent().getStringExtra("destination");

        flightManager = FlightManager.getInstance();

        // Buscar el vuelo en el grafo
        Airport origin = flightManager.getFlightGraph().findAirport(originCode);
        if (origin != null) {
            for (Flight f : origin.getFlightList()) {
                if (f.getDestination().getIataCode().equals(destinationCode)) {
                    flight = f;
                    break;
                }
            }
        }

        Spinner spinnerOrigin = findViewById(R.id.spinnerOrigin);
        Spinner spinnerDestination = findViewById(R.id.spinnerDestination);
        Spinner spinnerAirline = findViewById(R.id.spinnerAirline);
        Button btnSave = findViewById(R.id.btnSaveFlight);

        List<Airport> airportList = new ArrayList<>(flightManager.getFlightGraph().getVertices());

        // Adaptadores para origen y destino
        ArrayAdapter<Airport> adapterOrigin = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, airportList);
        adapterOrigin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrigin.setAdapter(adapterOrigin);

        ArrayAdapter<Airport> adapterDestination = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, airportList);
        adapterDestination.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDestination.setAdapter(adapterDestination);

        // Adaptador para aerolínea
        ArrayAdapter<Airline> adapterAirline = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, flightManager.getAirlines());
        adapterAirline.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAirline.setAdapter(adapterAirline);

        // Seleccionar valores actuales
        if (flight != null) {
            int posOrigin = airportList.indexOf(flight.getOrigin());
            int posDest = airportList.indexOf(flight.getDestination());
            int posAirline = flightManager.getAirlines().indexOf(flight.getAirline());

            if (posOrigin >= 0) spinnerOrigin.setSelection(posOrigin);
            if (posDest >= 0) spinnerDestination.setSelection(posDest);
            if (posAirline >= 0) spinnerAirline.setSelection(posAirline);
        }

        btnSave.setOnClickListener(v -> {
            Airport selectedOrigin = (Airport) spinnerOrigin.getSelectedItem();
            Airport selectedDestination = (Airport) spinnerDestination.getSelectedItem();
            Airline selectedAirline = (Airline) spinnerAirline.getSelectedItem();

            if (selectedOrigin.equals(selectedDestination)) {
                Toast.makeText(this, "El aeropuerto de origen y destino no pueden ser el mismo", Toast.LENGTH_SHORT).show();
                return;
            }

            // Actualizar vuelo en el grafo
            if (flight != null) {
                // Si cambiaron origen o destino, eliminar el vuelo anterior y crear uno nuevo
                if (!flight.getOrigin().equals(selectedOrigin) || !flight.getDestination().equals(selectedDestination)) {
                    flight.getOrigin().getFlightList().remove(flight);
                    flight = new Flight(selectedOrigin, selectedDestination, flight.getDistance(), selectedAirline);
                    selectedOrigin.addFlightListAirport(flight);
                } else {
                    // Solo cambiar aerolínea
                    flight.setAirline(selectedAirline);
                }
            }

            Toast.makeText(this, "Vuelo actualizado", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}