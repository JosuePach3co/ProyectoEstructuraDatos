package com.example.estructuradatos;

import java.io.Serializable;

public class Flight implements Serializable {
    private static final long serialVersionUID = 1L;

    private Airport origin;
    private Airport destination;
    private double distance;     // km
    private double durationMin;  // minutos
    private double cost;         // moneda
    private Airline airline;
    
    public Flight(Airport origin, Airport destination, double distance, Airline airline) {
        this(origin, destination, distance, 0.0, 0.0, airline);
    }

    public Flight(Airport origin, Airport destination, double distance,double durationMin, double cost, Airline airline) {
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
        this.durationMin = durationMin;
        this.cost = cost;
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

    public double getDurationMin() {
        return durationMin;
    }

    public void setDurationMin(double durationMin) {
        this.durationMin = durationMin;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public Airline getAirline() {
        return airline;
    }

    public void setAirline(Airline airline) {
        this.airline = airline;
    }

    @Override
    public String toString() {
        return origin.getIataCode() + " -> " + destination.getIataCode()
                + " (" + distance + " km"
                + (durationMin > 0 ? ", " + durationMin + " min" : "")
                + (cost > 0 ? ", $" + cost : "")
                + ", " + (airline != null ? airline.getName() : "N/A") + ")";
    }
}
