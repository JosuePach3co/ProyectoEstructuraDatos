package com.example.estructuradatos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AirportListActivity extends AppCompatActivity {
    private FlightManager flightManager;
    private AirportAdapter adapter;
    private List<Airport> airportList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_airport_list);

        flightManager = FlightManager.getInstance();

        ListView listView = findViewById(R.id.listAirports);

        airportList = new ArrayList<>(flightManager.getFlightGraph().getVertices());

        adapter = new AirportAdapter(this, airportList);
        listView.setAdapter(adapter);
    }

    // Metodo llamado desde el adaptador para eliminar un aeropuerto
    public void onDeleteAirportClick(final Airport airport) {
        if (isFinishing()) return;

        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Deseas eliminar el aeropuerto " + airport.getIataCode() + "?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // usar FlightManager.removeAirport para limpieza completa (Airline + AVL)
                    boolean removed = flightManager.removeAirport(airport.getIataCode());
                    if (removed && !isFinishing()) {
                        airportList.remove(airport);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Aeropuerto eliminado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No se pudo eliminar el aeropuerto", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void onEditAirportClick(final Airport airport) {
        Intent intent = new Intent(this, EditAirportActivity.class);
        intent.putExtra("iata", airport.getIataCode());
        startActivity(intent);
    }
}
