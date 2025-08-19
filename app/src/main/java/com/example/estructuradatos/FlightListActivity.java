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

        flightList = new ArrayList<>();
        for (Airport a : flightManager.getFlightGraph().getVertices()) {
            flightList.addAll(new ArrayList<>(a.getFlightList())); // Añadir los vuelos a la nueva lista
        }

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
                    boolean removed = flight.getOrigin().getFlightList().remove(flight);
                    if (removed && !isFinishing()) {
                        flightList.remove(flight);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Vuelo eliminado", Toast.LENGTH_SHORT).show();
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
}
