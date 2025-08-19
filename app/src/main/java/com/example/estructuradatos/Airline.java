package com.example.estructuradatos;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Airline implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<Flight> flights;

    public Airline(String name) {
        this.name = name;
        this.flights = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public void addFlight(Flight flight) {
        this.flights.add(flight);
    }

    @Override
    public String toString() {
        return name;
    }
}
