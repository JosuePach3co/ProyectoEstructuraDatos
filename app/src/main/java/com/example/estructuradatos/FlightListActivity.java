package com.example.estructuradatos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class FlightListActivity extends AppCompatActivity {
    private FlightManager flightManager;
    private FlightAdapter adapter;
    private List<Flight> flightList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_flight_list);

        flightManager = FlightManager.getInstance();

        ListView listView = findViewById(R.id.listFlights);

        // tomar la lista ordenada desde el AVL
        flightList = new ArrayList<>(flightManager.getFlightsSorted());

        adapter = new FlightAdapter(this, flightList);
        listView.setAdapter(adapter);
    }

    // metodo llamado desde el adaptador para eliminar un vuelo
    public void onDeleteFlightClick(final Flight flight) {
        if (isFinishing()) return;

        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Deseas eliminar el vuelo de " +
                        flight.getOrigin().getIataCode() + " a " +
                        flight.getDestination().getIataCode() + "?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // usar los métodos centralizados del FlightManager
                    boolean removed = flightManager.removeFlight(
                            flight.getOrigin().getIataCode(),
                            flight.getDestination().getIataCode()
                    );
                    if (removed && !isFinishing()) {
                        reloadFlights();
                        Toast.makeText(this, "Vuelo eliminado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No se pudo eliminar el vuelo", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void onEditFlightClick(final Flight flight) {
        Intent intent = new Intent(this, EditFlightActivity.class);
        intent.putExtra("origin", flight.getOrigin().getIataCode());
        intent.putExtra("destination", flight.getDestination().getIataCode());
        startActivity(intent);
    }

    // Refrescar la lista al volver desde editar/agregar
    @Override
    protected void onResume() {
        super.onResume();
        reloadFlights();
    }

    private void reloadFlights() {
        flightList.clear();
        // volver a tomar el orden desde el AVL
        flightList.addAll(flightManager.getFlightsSorted());
        adapter.notifyDataSetChanged();
    }
}
