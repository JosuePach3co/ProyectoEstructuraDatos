package com.example.estructuradatos;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Airport implements Serializable {
    private static final long serialVersionUID = 1L;

    private String iataCode;
    private String name;
    private float x, y;
    private List<Flight> flightList;
    private transient boolean visited;


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

    public boolean isVisited() { return visited; }

    public void setVisited(boolean visited) { this.visited = visited; }


    public void setName(String name) {
        this.name = name;
    }

    public void setIataCode(String iata) {
        this.iataCode = iata;
    }

    public List<Flight> getFlightList() {
        return flightList;
    }

    @Override
    public String toString() {
        return iataCode;
    }
}
