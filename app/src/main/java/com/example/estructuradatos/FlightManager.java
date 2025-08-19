package com.example.estructuradatos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FlightManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private static FlightManager instance;
    private GraphAL flightGraph;
    private List<Airline> airlines;

    private FlightManager() {
        this.flightGraph = new GraphAL(true);
        this.airlines = new ArrayList<>();
        loadInitialData();
    }

    public static FlightManager getInstance() {
        if (instance == null) {
            instance = new FlightManager();
        }
        return instance;
    }

    public GraphAL getFlightGraph() {
        return flightGraph;
    }

    public List<Airline> getAirlines() {
        return airlines;
    }

    private void loadInitialData() {
        // Aerolíneas
        Airline airChina = new Airline("Air China");
        Airline airBritanica = new Airline("British Airways");
        Airline airFrancesa = new Airline("Air France");

        airlines.add(airChina);
        airlines.add(airBritanica);
        airlines.add(airFrancesa);

        // Aeropuertos
        Airport pkx = new Airport("PKX", "Daxing", 100, 600);
        Airport jfk = new Airport("JFK", "Nueva York", 600, 800);
        Airport lhr = new Airport("LHR", "Londres", 1200, 900);
        Airport cdg = new Airport("CDG", "París", 1700, 1100);
        Airport nrt = new Airport("NRT", "Tokio", 2000, 1300);

        flightGraph.addAirport(pkx);
        flightGraph.addAirport(jfk);
        flightGraph.addAirport(lhr);
        flightGraph.addAirport(cdg);
        flightGraph.addAirport(nrt);

        // Vuelos
        flightGraph.addFlight("PKX", "LHR", 8140, airChina);
        flightGraph.addFlight("LHR", "JFK", 5570, airBritanica);
        flightGraph.addFlight("JFK", "PKX", 10980, airBritanica);
        flightGraph.addFlight("PKX", "NRT", 2100, airChina);
        flightGraph.addFlight("LHR", "CDG", 343, airFrancesa);
    }

}
