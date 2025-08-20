package com.example.estructuradatos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import java.io.Serializable;
import java.util.*;

public class GraphAL implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Airport> airports; // IATA -> Airport
    private boolean isDirected;

    public GraphAL(boolean isDirected) {
        this.airports = new HashMap<>();
        this.isDirected = isDirected;
    }

    public boolean addAirport(Airport airport) {
        if (airport == null || this.airports.containsKey(airport.getIataCode())) {
            return false;
        }
        this.airports.put(airport.getIataCode(), airport);
        return true;
    }

    public boolean addFlight(String iataOrigin, String iataDestination, double distance, Airline airline) {
        Airport origin = this.airports.get(iataOrigin);
        Airport destination = this.airports.get(iataDestination);

        if (origin == null || destination == null) {
            return false;
        }
        
        if (origin == destination) { //evita self-loop
        return false;
        }
        
        if (hasFlight(iataOrigin, iataDestination)) { //evita duplicados
            return false;
        }

        Flight newFlight = new Flight(origin, destination, distance, airline);
        origin.addFlightListAirport(newFlight);

        if (!this.isDirected) {
            Flight reverseFlight = new Flight(destination, origin, distance, airline);
            destination.addFlightListAirport(reverseFlight);
        }
        return true;
    }

    public Airport findAirport(String iataCode) {
        return this.airports.get(iataCode);
    }

    public Collection<Airport> getVertices() {
        return this.airports.values();
    }

    // Eliminar aeropuerto y limpiar vuelos entrantes
    public boolean removeAirport(Airport airport) {
        if (airport == null || !airports.containsKey(airport.getIataCode())) return false;

        airports.remove(airport.getIataCode());

        for (Airport a : airports.values()) {
            a.getFlightList().removeIf(f -> f.getDestination().equals(airport));
        }
        return true;
    }
    
    public boolean hasFlight(String iataOrigin, String iataDestination) {
        Airport origin = this.airports.get(iataOrigin);
        Airport destination = this.airports.get(iataDestination);

        if (origin == null || destination == null) {
            return false;
        }

        for (Flight f : origin.getFlightList()) {
            if (f.getDestination() == destination) { //misma instancia del mapa
                return true;
            }
        }
        return false;
    }
    
    public boolean removeFlight(String iataOrigin, String iataDestination) {
        Airport origin = this.airports.get(iataOrigin);
        Airport destination = this.airports.get(iataDestination);

        if (origin == null || destination == null) {
            return false;
        }

        boolean removedForward = origin.getFlightList().removeIf(f -> {
            if (f.getDestination() == destination) {
                // limpiar referencia en airline (si la hay)
                Airline al = f.getAirline();
                if (al != null) {
                    al.getFlights().remove(f);
                }
                return true;
            }
            return false;
        });

        if (!this.isDirected) {
            // Remueve vuelta si el grafo fuera no dirigido
            boolean removedBackward = destination.getFlightList().removeIf(f -> {
                if (f.getDestination() == origin) {
                    Airline al = f.getAirline();
                    if (al != null) {
                        al.getFlights().remove(f);
                    }
                    return true;
                }
                return false;
            });
            return removedForward || removedBackward;
        }
        return removedForward;
    }
}
