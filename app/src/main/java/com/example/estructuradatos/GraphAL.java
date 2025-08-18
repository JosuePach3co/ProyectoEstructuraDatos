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
}
