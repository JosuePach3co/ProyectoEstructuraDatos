package com.example.estructuradatos;

import java.io.Serializable;

public class Flight implements Serializable {
    private static final long serialVersionUID = 1L;

    private Airport origin;
    private Airport destination;
    private double distance;
    private Airline airline;

    public Flight(Airport origin, Airport destination, double distance, Airline airline) {
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
        this.airline = airline;

        if (this.airline != null) {
            this.airline.addFlight(this);
        }
    }

    public Airport getOrigin() {
        return origin;
    }
    public Airport getDestination() {
        return destination;
    }
    public double getDistance() {
        return distance;
    }
    public Airline getAirline() {
        return airline;
    }

    @Override
    public String toString() {
        return origin.getIataCode() + " -> " + destination.getIataCode()
                + " (" + distance + " km, "
                + (airline != null ? airline.getName() : "N/A") + ")";
    }
}
