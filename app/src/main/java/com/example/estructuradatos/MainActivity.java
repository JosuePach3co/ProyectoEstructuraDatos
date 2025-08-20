package com.example.estructuradatos;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

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

        // Cargar desde binario ANTES de usar el grafo
        FlightManager.loadFromDisk(this);

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
        fabAggVuelos.setOnClickListener(v -> startActivity(new Intent(this, AddFlightActivity.class)));
    }

    // Guardar en binario cuando la Activity pasa a pausa
    @Override
    protected void onPause() {
        super.onPause();
        if (flightManager != null) {
            flightManager.saveToDisk(this);
        }
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Añadimos un item de menú programáticamente (sin XML)
        menu.add(0, 1001, 0, "Buscar ruta (Dijkstra)")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1001) {
            showDijkstraDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDijkstraDialog() {
        List<Airport> airports = new ArrayList<>(flightManager.getFlightGraph().getVertices());
        if (airports.size() < 2) {
            Toast.makeText(this, "Se requieren al menos 2 aeropuertos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vista del diálogo (dos spinners)
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, 0);

        Spinner spOrigin = new Spinner(this);
        Spinner spDest = new Spinner(this);

        ArrayAdapter<Airport> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, airports);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spOrigin.setAdapter(adapter);
        spDest.setAdapter(adapter);

        container.addView(spOrigin);
        container.addView(spDest);

        new AlertDialog.Builder(this)
                .setTitle("Ruta más corta (Dijkstra)")
                .setView(container)
                .setPositiveButton("Buscar", (d, w) -> {
                    Airport o = (Airport) spOrigin.getSelectedItem();
                    Airport t = (Airport) spDest.getSelectedItem();
                    if (o == null || t == null) {
                        Toast.makeText(this, "Selecciona origen y destino", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (o.equals(t)) {
                        Toast.makeText(this, "Origen y destino no pueden ser iguales", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ShortestPathResult res = flightManager.getFlightGraph().dijkstra(o.getIataCode(), t.getIataCode());
                    if (!res.isReachable()) {
                        new AlertDialog.Builder(this)
                                .setTitle("Resultado")
                                .setMessage("No existe ruta entre " + o.getIataCode() + " y " + t.getIataCode())
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        // Construir cadena del camino
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < res.getPath().size(); i++) {
                            sb.append(res.getPath().get(i).getIataCode());
                            if (i < res.getPath().size() - 1) sb.append(" -> ");
                        }
                        String msg = "Distancia total: " + res.getTotalDistance() + "\nCamino: " + sb;
                        new AlertDialog.Builder(this)
                                .setTitle("Ruta encontrada")
                                .setMessage(msg)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
