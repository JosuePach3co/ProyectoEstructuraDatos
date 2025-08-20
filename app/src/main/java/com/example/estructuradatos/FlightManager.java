package com.example.estructuradatos;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class FlightManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String FILE_NAME = "arch_flights.bin";

    private static FlightManager instance;

    private GraphAL flightGraph;
    private List<Airline> airlines;

    // Árbol AVL para ordenar vuelos (se reconstruye al cargar)
    private transient AvlTree<Flight> flightAvl;

    private FlightManager() {
        this.flightGraph = new GraphAL(true);
        this.airlines = new ArrayList<>();
        rebuildAirlineIndexes();
        initAvl();
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


    public static double computeDistance(Airport a, Airport b) {
        if (a == null || b == null) return Double.POSITIVE_INFINITY;
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        return Math.round(Math.hypot(dx, dy) * 100.0) / 100.0;
    }

    private void loadInitialData() {
        Airline airChina = new Airline("Air China");
        Airline airBritanica = new Airline("British Airways");
        Airline airFrancesa = new Airline("Air France");

        airlines.add(airChina);
        airlines.add(airBritanica);
        airlines.add(airFrancesa);

        Airport pkx = new Airport("PKX", "Daxing", 100, 600);
        Airport jfk = new Airport("JFK", "Nueva York", 200, 100);
        Airport lhr = new Airport("LHR", "Londres", 900, 900);
        Airport cdg = new Airport("CDG", "París", 1700, 1100);
        Airport nrt = new Airport("NRT", "Tokio", 500, 1300);

        flightGraph.addAirport(pkx);
        flightGraph.addAirport(jfk);
        flightGraph.addAirport(lhr);
        flightGraph.addAirport(cdg);
        flightGraph.addAirport(nrt);

        // uso de computeDistance(...) para distancias iniciales
        flightGraph.addFlight("PKX", "LHR", computeDistance(pkx, lhr), airChina);
        flightGraph.addFlight("LHR", "JFK", computeDistance(lhr, jfk), airBritanica);
        flightGraph.addFlight("JFK", "PKX", computeDistance(jfk, pkx), airBritanica);
        flightGraph.addFlight("PKX", "NRT", computeDistance(pkx, nrt), airChina);
        flightGraph.addFlight("LHR", "CDG", computeDistance(lhr, cdg), airFrancesa);
    }


    public synchronized boolean saveToDisk(Context ctx) {
        if (ctx == null) return false;
        try (FileOutputStream fos = ctx.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            Store store = new Store(this.flightGraph, this.airlines);
            oos.writeObject(store);
            oos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static synchronized boolean loadFromDisk(Context ctx) {
        if (ctx == null) return false;
        if (instance == null) instance = new FlightManager();

        try (FileInputStream fis = ctx.openFileInput(FILE_NAME);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Object obj = ois.readObject();
            if (obj instanceof Store) {
                Store store = (Store) obj;
                instance.flightGraph = store.graph;
                instance.airlines = store.airlines;
                instance.rebuildAirlineIndexes();
                instance.initAvl();
                return true;
            }
            return false;
        } catch (FileNotFoundException e) {
            instance.loadInitialData();
            return false; // primera ejecución
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static class Store implements Serializable {
        private static final long serialVersionUID = 1L;
        private final GraphAL graph;
        private final List<Airline> airlines;
        private Store(GraphAL g, List<Airline> a) { this.graph = g; this.airlines = a; }
    }


    public synchronized boolean addFlight(String originIata, String destinationIata, double distance, Airline airline) {
        if (originIata == null || destinationIata == null) return false;

        boolean added = flightGraph.addFlight(originIata, destinationIata, distance, airline);
        if (!added) return false;

        Flight created = findFlight(originIata, destinationIata);
        if (airline != null && created != null && !airline.getFlights().contains(created)) {
            airline.addFlight(created);
        }

        if (created != null) {
            ensureAvl();
            flightAvl.insert(created);
        }
        return true;
    }

    public synchronized boolean removeFlight(String originIata, String destinationIata) {
        Flight toRemove = findFlight(originIata, destinationIata);

        if (toRemove != null) {
            if (toRemove.getAirline() != null) {
                toRemove.getAirline().getFlights().remove(toRemove);
            }
            if (flightAvl != null) {
                flightAvl.delete(toRemove);
            }
        }
        return flightGraph.removeFlight(originIata, destinationIata);
    }

    public Flight findFlight(String originIata, String destinationIata) {
        Airport origin = flightGraph.findAirport(originIata);
        if (origin == null) return null;
        for (Flight f : origin.getFlightList()) {
            if (f != null &&
                    f.getDestination() != null &&
                    destinationIata.equals(f.getDestination().getIataCode())) {
                return f;
            }
        }
        return null;
    }

    public List<Flight> getAllFlights() {
        List<Flight> result = new ArrayList<>();
        Collection<Airport> verts = flightGraph.getVertices();
        if (verts != null) {
            for (Airport a : verts) {
                if (a != null && a.getFlightList() != null) {
                    result.addAll(a.getFlightList());
                }
            }
        }
        return result;
    }

    public void rebuildAirlineIndexes() {
        for (Airline al : airlines) {
            if (al != null && al.getFlights() != null) {
                al.getFlights().clear();
            }
        }
        for (Flight f : getAllFlights()) {
            if (f != null && f.getAirline() != null) {
                f.getAirline().addFlight(f);
            }
        }
    }

    private void initAvl() {
        flightAvl = new AvlTree<>(FlightComparators.BY_DISTANCE_ASC);
        for (Flight f : getAllFlights()) {
            if (f != null) flightAvl.insert(f);
        }
    }

    public void rebuildFlightAvl() {
        if (flightAvl == null) {
            initAvl();
            return;
        }
        flightAvl.clear();
        flightAvl.setComparator(FlightComparators.BY_DISTANCE_ASC);
        for (Flight f : getAllFlights()) {
            if (f != null) flightAvl.insert(f);
        }
    }

    private void ensureAvl() {
        if (flightAvl == null) {
            initAvl();
        } else {
            flightAvl.setComparator(FlightComparators.BY_DISTANCE_ASC);
        }
    }

    public List<Flight> getFlightsSorted() {
        ensureAvl();
        return flightAvl.toSortedList();
    }


    public synchronized boolean removeAirport(String iata) {
        if (iata == null) return false;
        Airport airport = flightGraph.findAirport(iata);
        if (airport == null) return false;

        // SALIENTES
        for (Flight f : new ArrayList<>(airport.getFlightList())) {
            if (f != null && f.getDestination() != null) {
                removeFlight(iata, f.getDestination().getIataCode());
            }
        }
        // ENTRANTES
        for (Airport a : new ArrayList<>(flightGraph.getVertices())) {
            if (a == null || a.getFlightList() == null) continue;
            for (Flight f : new ArrayList<>(a.getFlightList())) {
                if (f != null && f.getDestination() != null &&
                        iata.equals(f.getDestination().getIataCode())) {
                    removeFlight(a.getIataCode(), iata);
                }
            }
        }
        return flightGraph.removeAirport(airport);
    }
}
