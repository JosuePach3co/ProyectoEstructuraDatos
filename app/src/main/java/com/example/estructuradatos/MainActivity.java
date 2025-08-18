package com.example.estructuradatos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private GraphView graphView;
    private FlightManager flightManager;
    public static final float DISTANCIA_MINIMA = GraphView.AIRPORT_SIZE * 3.0f;

    private ActivityResultLauncher<Intent> addAirportLauncher;


    @Override
    protected void onResume() {
        super.onResume();
        graphView.invalidate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        graphView = findViewById(R.id.graph_view);
        flightManager = FlightManager.getInstance();
        graphView.setGraph(flightManager.getFlightGraph());

        addAirportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String iata = data.getStringExtra("iata");
                        String nombre = data.getStringExtra("nombre");
                        float x = data.getFloatExtra("x", 0);
                        float y = data.getFloatExtra("y", 0);
                        agregarAeropuerto(iata, nombre, x, y);
                    }
                }
        );

        // Click en aeropuerto: mostrar diálogo con vuelos
        graphView.setOnAirportClickListener(airport -> {
            StringBuilder sb = new StringBuilder();
            if (airport.getFlightList().isEmpty()) {
                sb.append("No tiene vuelos registrados.");
            } else {
                for (Flight f : airport.getFlightList()) {
                    sb.append("De ").append(f.getOrigin().getIataCode())
                            .append(" a ").append(f.getDestination().getIataCode())
                            .append(" | Aerolínea: ").append(f.getAirline().getName())
                            .append(" | Distancia: ").append(f.getDistance()).append(" km\n");
                }
            }

            new AlertDialog.Builder(this)
                    .setTitle("Vuelos del aeropuerto " + airport.getIataCode())
                    .setMessage(sb.toString())
                    .setPositiveButton("OK", null)
                    .show();
        });

        // Click en espacio vacío: lanzar AddAirportActivity solo si posición válida
        graphView.setOnEmptySpaceClickListener((x, y) -> {
            boolean valido = graphView.isPositionValid(x, y, flightManager.getFlightGraph().getVertices(), DISTANCIA_MINIMA);
            if (!valido) {
                Toast.makeText(this, "Demasiado cerca de otro aeropuerto", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> existingIatas = new ArrayList<>();
            for (Airport ap : flightManager.getFlightGraph().getVertices()) {
                existingIatas.add(ap.getIataCode());
            }
            Intent intent = new Intent(this, AddAirportActivity.class);
            intent.putExtra("x", x);
            intent.putExtra("y", y);
            intent.putStringArrayListExtra("existing_iatas", existingIatas);
            addAirportLauncher.launch(intent);
        });

        FloatingActionButton fabEliminar = findViewById(R.id.btnEliminar);
        fabEliminar.setOnClickListener(v -> startActivity(new Intent(this, DeleteActivity.class)));

        FloatingActionButton fabAggVuelos = findViewById(R.id.btn_add_vuelos);
        fabAggVuelos.setOnClickListener(v -> startActivity(new Intent(this,AddFlightActivity.class )));

    }

    private void agregarAeropuerto(String iata, String nombre, float x, float y) {
        boolean valido = graphView.isPositionValid(x, y, flightManager.getFlightGraph().getVertices(), DISTANCIA_MINIMA);
        if (!valido) {
            Toast.makeText(this, "No se puede agregar, demasiado cerca de otro aeropuerto", Toast.LENGTH_SHORT).show();
            graphView.setPreview(x, y, false);
            return;
        }

        Airport nuevo = new Airport(iata, nombre, x, y);
        if (flightManager.getFlightGraph().addAirport(nuevo)) {
            graphView.setPreview(null, null, true);
            graphView.postInvalidate();
        } else {
            Toast.makeText(this, "IATA repetido", Toast.LENGTH_SHORT).show();
        }
    }
}