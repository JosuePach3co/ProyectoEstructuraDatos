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

            if (flight == null) {
                Toast.makeText(this, "No se encontró el vuelo a editar", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean routeChanged =
                    !flight.getOrigin().equals(selectedOrigin) ||
                            !flight.getDestination().equals(selectedDestination);

            boolean airlineChanged =
                    (flight.getAirline() == null && selectedAirline != null) ||
                            (flight.getAirline() != null && !flight.getAirline().equals(selectedAirline));

            if (routeChanged) {
                String oldOrigin = flight.getOrigin().getIataCode();
                String oldDest = flight.getDestination().getIataCode();

                // Eliminar el vuelo anterior (limpia Airline y AVL)
                boolean removed = flightManager.removeFlight(oldOrigin, oldDest);
                if (!removed) {
                    Toast.makeText(this, "No se pudo eliminar el vuelo anterior", Toast.LENGTH_SHORT).show();
                    return;
                }

                // PUNTO 14: calcular distancia con método centralizado
                double distancia = FlightManager.computeDistance(selectedOrigin, selectedDestination);

                boolean added = flightManager.addFlight(
                        selectedOrigin.getIataCode(),
                        selectedDestination.getIataCode(),
                        distancia,
                        selectedAirline
                );

                if (!added) {
                    Toast.makeText(this, "No se pudo crear el nuevo vuelo (¿duplicado?)", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(this, "Vuelo actualizado", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if (airlineChanged) {
                Airline oldAirline = flight.getAirline();

                if (oldAirline != null) {
                    oldAirline.getFlights().remove(flight);
                }
                if (selectedAirline != null && !selectedAirline.getFlights().contains(flight)) {
                    selectedAirline.addFlight(flight);
                }

                flight.setAirline(selectedAirline);

                // El comparador del AVL desempata por aerolínea: reconstruimos
                flightManager.rebuildFlightAvl();
                Toast.makeText(this, "Aerolínea actualizada", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Toast.makeText(this, "No hay cambios que guardar", Toast.LENGTH_SHORT).show();
        });
    }
}
