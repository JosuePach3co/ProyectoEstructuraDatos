package com.example.estructuradatos;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Resultado de una consulta de ruta más corta (Dijkstra).
 * Contiene si el destino es alcanzable, la distancia total y el camino (en orden).
 */
public class ShortestPathResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean reachable;
    private final double totalDistance; // ahora es el "peso total" según métrica
    private final List<Airport> path; // Camino en orden desde origen hasta destino (incluye ambos)
    private final GraphAL.WeightMetric metric;
    
    public ShortestPathResult(boolean reachable, double totalDistance, List<Airport> path) {
        this(reachable, totalDistance, path, GraphAL.WeightMetric.DISTANCE);
    }

    public ShortestPathResult(boolean reachable, double totalDistance, List<Airport> path, GraphAL.WeightMetric metric) {
        this.reachable = reachable;
        this.totalDistance = totalDistance;
        this.metric = (metric == null) ? GraphAL.WeightMetric.DISTANCE : metric;
        this.path = (path == null) ? Collections.emptyList() : Collections.unmodifiableList(path);
    }

    public boolean isReachable() {
        return reachable;
    }

    public double getTotalDistance() {
        return Math.round(totalDistance * 100.0) / 100.0;
    }


    public List<Airport> getPath() {
        return path;
    }
    
    public GraphAL.WeightMetric getMetric() {
        return metric;
    }

    @Override
    public String toString() {
        if (!reachable) {
            return "Ruta no disponible";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Distancia total: ").append(totalDistance).append(" | Camino: ");
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i).getIataCode());
            if (i < path.size() - 1) sb.append(" -> ");
        }
        return sb.toString();
    }
}
