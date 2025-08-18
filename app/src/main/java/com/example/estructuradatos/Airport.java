package com.example.estructuradatos;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Airport implements Serializable {
    private static final long serialVersionUID = 1L;

    private String iataCode; // codigo para identificacion de cada aeropuerto
    private String name;
    private float x, y;
    private List<Flight> flightList;


    public Airport(String iataCode, String name, float x, float y) {
        this.iataCode = iataCode;
        this.name = name;
        this.x = x;
        this.y = y;
        this.flightList = new LinkedList<>();
    }

    public void addFlightListAirport(Flight flight) {
        this.flightList.add(flight);
    }

    public String getIataCode() {
        return iataCode;
    }

    public String getName() {
        return name;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public List<Flight> getFlightList() {
        return flightList;
    }

    @Override
    public String toString() {
        return iataCode;
    }
}
