package com.example.estructuradatos;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
        if (flightManager != null) {
            flightManager.saveToDisk(this);
        }
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

            // Vuelos salientes
            List<Flight> out = airport.getFlightList();
            sb.append("Vuelos SALIENTES:\n");
            if (out.isEmpty()) {
                sb.append("  (ninguno)\n");
            } else {
                for (Flight f : out) {
                    String al = (f.getAirline() != null && f.getAirline().getName() != null)
                            ? f.getAirline().getName() : "N/A";
                    sb.append("  ")
                            .append(f.getOrigin().getIataCode())
                            .append(" → ")
                            .append(f.getDestination().getIataCode())
                            .append(" | Aerolínea: ").append(al)
                            .append(" | Dist: ").append(String.format("%.1f km", f.getDistance()))
                            .append("\n");
                }
            }

            // Vuelos ENTRANTES
            List<Flight> in = flightManager.getFlightGraph().getIncomingFlights(airport);
            sb.append("\nVuelos ENTRANTES:\n");
            if (in.isEmpty()) {
                sb.append("  (ninguno)\n");
            } else {
                for (Flight f : in) {
                    String al = (f.getAirline() != null && f.getAirline().getName() != null)
                            ? f.getAirline().getName() : "N/A";
                    sb.append("  ")
                            .append(f.getOrigin().getIataCode())
                            .append(" → ")
                            .append(f.getDestination().getIataCode())
                            .append(" | Aerolínea: ").append(al)
                            .append(" | Dist: ").append(String.format("%.1f km", f.getDistance()))
                            .append("\n");
                }
            }

            sb.append("\nTotal salidas: ").append(out.size());
            sb.append(" | Total entradas: ").append(in.size());

            new AlertDialog.Builder(this)
                    .setTitle("Aeropuerto " + airport.getIataCode())
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
        menu.add(0, 1001, 0, "Buscar ruta (Dijkstra)")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add(0, 1002, 1, "Verificar último nodo")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

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

    private void showLastNodeInfo() {
        GraphAL graph = flightManager.getFlightGraph();
        Airport last = graph.getLastAirport();
        if (last == null) {
            Toast.makeText(this, "No hay aeropuertos en el grafo", Toast.LENGTH_SHORT).show();
            return;
        }
        int in = graph.inDegree(last.getIataCode());
        int out = graph.outDegree(last.getIataCode());
        List<Flight> incoming = graph.getIncomingFlights(last);

        StringBuilder sb = new StringBuilder();
        sb.append("Aeropuerto: ").append(last.getIataCode()).append("\n");
        sb.append("Entradas: ").append(in).append(" | Salidas: ").append(out).append("\n\n");

        sb.append("Vuelos ENTRANTES:\n");
        if (incoming.isEmpty()) {
            sb.append("  (ninguno)\n");
        } else {
            for (Flight f : incoming) {
                String al = (f.getAirline() != null && f.getAirline().getName() != null)
                        ? f.getAirline().getName() : "N/A";
                sb.append("  ")
                        .append(f.getOrigin().getIataCode())
                        .append(" → ")
                        .append(f.getDestination().getIataCode())
                        .append(" | Aerolínea: ").append(al)
                        .append(" | Dist: ").append(String.format("%.1f km", f.getDistance()))
                        .append("\n");
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Último nodo (por inserción)")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }



    private void showDijkstraDialog() {
        List<Airport> airports = new ArrayList<>(flightManager.getFlightGraph().getVertices());
        if (airports.size() < 2) {
            Toast.makeText(this, "Se requieren al menos 2 aeropuertos", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, 0);

        Spinner spOrigin = new Spinner(this);
        Spinner spDest = new Spinner(this);
        Spinner spMetric = new Spinner(this);

        TextView txtOrigen = new TextView(this);
        txtOrigen.setText("Aeropuerto de Origen");

        TextView txtDestino = new TextView(this);
        txtDestino.setText("Aeropuerto de Destino");

        TextView txtRuta = new TextView(this);
        txtRuta.setText("Por");


        ArrayAdapter<Airport> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, airports);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        spOrigin.setAdapter(adapter);
        spDest.setAdapter(adapter);
        
        ArrayAdapter<String> metAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Distancia", "Tiempo", "Costo"});
        metAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMetric.setAdapter(metAdapter);

        container.addView(txtOrigen);
        container.addView(spOrigin);
        container.addView(txtDestino);
        container.addView(spDest);
        container.addView(txtRuta);
        container.addView(spMetric);
        
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
                    
                    String m = (String) spMetric.getSelectedItem();
                    GraphAL.WeightMetric metric = GraphAL.WeightMetric.DISTANCE;
                    if ("Tiempo".equals(m)) metric = GraphAL.WeightMetric.TIME;
                    else if ("Costo".equals(m)) metric = GraphAL.WeightMetric.COST;

                    ShortestPathResult res = flightManager.getFlightGraph().dijkstra(o.getIataCode(), t.getIataCode(), metric);
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
                        String unidad = (metric == GraphAL.WeightMetric.DISTANCE) ? "km"
                                   : (metric == GraphAL.WeightMetric.TIME)     ? "min"
                                   : "$";
                        String msg = "Total (" + m + "): " + res.getTotalDistance() + " " + unidad + "\nCamino: " + sb;
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
