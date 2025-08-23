package com.example.estructuradatos;

import java.io.Serializable;
import java.util.*;


public class GraphAL implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum WeightMetric { DISTANCE, TIME, COST }

    private final Map<String, Airport> airports; // IATA - Airport
    private final boolean isDirected;

    public GraphAL(boolean isDirected) {
        this.airports = new HashMap<>();
        this.isDirected = isDirected;
    }

    public boolean addAirport(Airport airport) {
        if (airport == null || airport.getIataCode() == null || this.airports.containsKey(airport.getIataCode())) {
            return false;
        }
        this.airports.put(airport.getIataCode(), airport);
        return true;
    }

    public boolean addFlight(String iataOrigin, String iataDestination, double distance, double durationMin, double cost, Airline airline) {
        Airport origin = this.airports.get(iataOrigin);
        Airport destination = this.airports.get(iataDestination);

        if (origin == null || destination == null) {
            return false;
        }
        if (origin == destination) { // evita self-loop por referencia
            return false;
        }
        if (Objects.equals(iataOrigin, iataDestination)) { // evita self-loop por IATA
            return false;
        }/**
        if (hasFlight(iataOrigin, iataDestination)) { // evita duplicados
            return false;
        }**/

        Flight newFlight = new Flight(origin, destination, distance, durationMin, cost, airline);
        origin.addFlightListAirport(newFlight);

        if (!this.isDirected) {
            Flight reverseFlight = new Flight(destination, origin, distance, durationMin, cost, airline);
            destination.addFlightListAirport(reverseFlight);
        }
        return true;
    }
    /**
    // Compatibilidad con llamadas antiguas: calcula valores por defecto
    public boolean addFlight(String iataOrigin, String iataDestination, double distance, Airline airline) {
        double defDur = FlightManager.computeEstimatedDurationMin(distance);
        double defCost = FlightManager.computeEstimatedCost(distance);
        return addFlight(iataOrigin, iataDestination, distance, defDur, defCost, airline);
    }**/


    public Airport findAirport(String iataCode) {
        return this.airports.get(iataCode);
    }

    public Collection<Airport> getVertices() {
        return this.airports.values();
    }


    public boolean removeAirport(Airport airport) {
        if (airport == null || airport.getIataCode() == null || !airports.containsKey(airport.getIataCode())) return false;

        final String toRemoveIata = airport.getIataCode();

        // Limpiar vuelos SALIENTES de airport en listas de aerolíneas
        if (airport.getFlightList() != null) {
            for (Flight f : new ArrayList<>(airport.getFlightList())) {
                if (f != null) {
                    Airline al = f.getAirline();
                    if (al != null && al.getFlights() != null) {
                        al.getFlights().remove(f);
                    }
                }
            }
            // Opcional: limpiar la lista local (no estrictamente necesario al remover el vértice)
            airport.getFlightList().clear();
        }

        // Remover el vértice del mapa
        airports.remove(toRemoveIata);

        // Limpiar vuelos ENTRANTES hacia airport en los demás aeropuertos,
        //    y quitar también de Airline.getFlights()
        for (Airport a : airports.values()) {
            if (a == null || a.getFlightList() == null) continue;
            a.getFlightList().removeIf(f -> {
                if (f == null || f.getDestination() == null) return false;
                boolean match = toRemoveIata.equals(f.getDestination().getIataCode());
                if (match) {
                    Airline al = f.getAirline();
                    if (al != null && al.getFlights() != null) {
                        al.getFlights().remove(f);
                    }
                }
                return match;
            });
        }
        return true;
    }

    /**
    public boolean hasFlight(String iataOrigin, String iataDestination) {
        Airport origin = this.airports.get(iataOrigin);
        if (origin == null) return false;

        for (Flight f : origin.getFlightList()) {
            Airport dest = (f != null) ? f.getDestination() : null;
            String destIata = (dest != null) ? dest.getIataCode() : null;
            if (Objects.equals(destIata, iataDestination)) {
                return true;
            }
        }
        return false;
    }**/


    public boolean removeFlight(String iataOrigin, String iataDestination) {
        Airport origin = this.airports.get(iataOrigin);
        Airport destination = this.airports.get(iataDestination);

        if (origin == null || destination == null) {
            return false;
        }

        boolean removedForward = origin.getFlightList().removeIf(f -> {
            if (f == null || f.getDestination() == null) return false;
            boolean match = Objects.equals(f.getDestination().getIataCode(), iataDestination);
            if (match) {
                Airline al = f.getAirline();
                if (al != null && al.getFlights() != null) {
                    al.getFlights().remove(f);
                }
            }
            return match;
        });

        if (!this.isDirected) {
            boolean removedBackward = destination.getFlightList().removeIf(f -> {
                if (f == null || f.getDestination() == null) return false;
                boolean match = Objects.equals(f.getDestination().getIataCode(), iataOrigin);
                if (match) {
                    Airline al = f.getAirline();
                    if (al != null && al.getFlights() != null) {
                        al.getFlights().remove(f);
                    }
                }
                return match;
            });
            return removedForward || removedBackward;
        }
        return removedForward;
    }


    public boolean changeIata(String oldIata, String newIata) {
        if (oldIata == null || newIata == null) {
            return false;
        }
        if (oldIata.equals(newIata)) {
            return true;
        }

        Airport airport = this.airports.get(oldIata);
        if (airport == null) return false;        // no existe el viejo
        if (this.airports.containsKey(newIata)) { // ya existe el nuevo
            return false;
        }

        this.airports.remove(oldIata);
        airport.setIataCode(newIata);   // actualiza el campo visible
        this.airports.put(newIata, airport);

        return true;
    }

    // ============================================================
    //  Dijkstra
    // ============================================================

    public ShortestPathResult dijkstra(String fromIata, String toIata, WeightMetric metric) {
        Airport source = airports.get(fromIata);
        Airport target = airports.get(toIata);

        if (source == null || target == null) {
            return new ShortestPathResult(false, Double.POSITIVE_INFINITY, Collections.emptyList(), metric);
        }

        Map<Airport, Double> dist = new HashMap<>();
        Map<Airport, Airport> prev = new HashMap<>();

        for (Airport a : airports.values()) {
            dist.put(a, Double.POSITIVE_INFINITY);
        }
        dist.put(source, 0.0);

        PriorityQueue<Airport> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.add(source);

        while (!pq.isEmpty()) {
            Airport u = pq.poll();
            if (u.equals(target)) break;

            for (Flight f : u.getFlightList()) {
                if (f == null || f.getDestination() == null) continue;
                Airport v = f.getDestination();
                double w;
                switch (metric) {
                    case TIME: w = f.getDurationMin() > 0 ? f.getDurationMin()
                            : FlightManager.computeEstimatedDurationMin(f.getDistance()); break;
                    case COST: w = f.getCost() > 0 ? f.getCost()
                            : FlightManager.computeEstimatedCost(f.getDistance()); break;
                    case DISTANCE:
                    default:   w = f.getDistance();
                }
                double alt = dist.get(u) + w;
                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.remove(v);
                    pq.add(v);
                }
            }
        }

        double total = dist.get(target);
        if (total == Double.POSITIVE_INFINITY) {
            return new ShortestPathResult(false, total, Collections.emptyList(), metric);
        }

        LinkedList<Airport> path = new LinkedList<>();
        Airport step = target;
        while (step != null) {
            path.addFirst(step);
            step = prev.get(step);
        }

        return new ShortestPathResult(true, total, path, metric);
    }
    
    public ShortestPathResult dijkstra(String fromIata, String toIata) {
        return dijkstra(fromIata, toIata, WeightMetric.DISTANCE);
    }
    
    // ====================== ESTADÍSTICAS ============================
    public int outDegree(String iata) {
        Airport a = airports.get(iata);
        return (a == null || a.getFlightList() == null) ? 0 : a.getFlightList().size();
    }

    public int inDegree(String iata) {
        Airport target = airports.get(iata);
        if (target == null) return 0;
        int count = 0;
        for (Airport a : airports.values()) {
            if (a == null || a.getFlightList() == null) continue;
            for (Flight f : a.getFlightList()) {
                if (f != null && f.getDestination() != null && target.equals(f.getDestination())) count++;
            }
        }
        return count;
    }

    // conexiones = (includeIn ? entrantes : 0) + (includeOut ? salientes : 0)
    public Map<Airport,Integer> connections(boolean includeIn, boolean includeOut) {
        Map<Airport,Integer> map = new HashMap<>();
        for (Airport a : airports.values()) {
            int val = 0;
            if (includeOut) val += outDegree(a.getIataCode());
            if (includeIn)  val += inDegree(a.getIataCode());
            map.put(a, val);
        }
        return map;
    }

    public Airport mostConnected(boolean includeIn, boolean includeOut) {
        Map<Airport,Integer> map = connections(includeIn, includeOut);
        return map.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    public Airport leastConnected(boolean includeIn, boolean includeOut) {
        Map<Airport,Integer> map = connections(includeIn, includeOut);
        return map.entrySet().stream().min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }
}
