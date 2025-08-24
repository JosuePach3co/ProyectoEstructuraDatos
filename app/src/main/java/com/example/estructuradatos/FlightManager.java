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
    // Parámetros para estimar si no se proveen (se puede ajustar)
    private static final double AVG_SPEED_KMH = 800.0;   // velocidad comercial
    private static final double COST_PER_KM   = 0.12;    // costo estimado por km
    
    private static final long serialVersionUID = 1L;

    private static final String FILE_NAME = "archivo_flight6.bin";

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
    
    public static double computeEstimatedDurationMin(double distanceKm) {
        if (distanceKm <= 0) return 0;
        double hours = distanceKm / AVG_SPEED_KMH;
        return Math.round(hours * 60.0 * 100.0) / 100.0;
    }
    
    public static double computeEstimatedCost(double distanceKm) {
        if (distanceKm <= 0) return 0;
        return Math.round(distanceKm * COST_PER_KM * 100.0) / 100.0;
    }

    private void loadInitialData() {
        // Crear aerolineas
        Airline airChina = new Airline("Air China");
        Airline british = new Airline("British Airways");
        Airline airFrance = new Airline("Air France");
        Airline delta = new Airline("Delta Airlines");
        Airline latam = new Airline("LATAM");

        airlines.add(airChina);
        airlines.add(british);
        airlines.add(airFrance);
        airlines.add(delta);
        airlines.add(latam);

        // Crear 10 aeropuertos
        Airport pkx = new Airport("PKX", "Daxing", 100, 600);
        Airport jfk = new Airport("JFK", "Nueva York", 300, 100);
        Airport lhr = new Airport("LHR", "Londres", 900, 900);
        Airport cdg = new Airport("CDG", "París", 1700, 1100);
        Airport nrt = new Airport("NRT", "Tokio", 500, 1300);
        Airport gru = new Airport("GRU", "Sao Paulo", 300, 330);
        Airport syd = new Airport("SYD", "Sídney", 400, 1700);
        Airport lax = new Airport("LAX", "Los Ángeles", 1000, 500);
        Airport mxc = new Airport("MEX", "Ciudad de México", 1200, 1500);
        Airport mad = new Airport("MAD", "Madrid", 1200, 1000);

        // Agregar aeropuertos al grafo
        flightGraph.addAirport(pkx);
        flightGraph.addAirport(jfk);
        flightGraph.addAirport(lhr);
        flightGraph.addAirport(cdg);
        flightGraph.addAirport(nrt);
        flightGraph.addAirport(gru);
        flightGraph.addAirport(syd);
        flightGraph.addAirport(lax);
        flightGraph.addAirport(mxc);
        flightGraph.addAirport(mad);

        //creamos los vuelos
        addFlight("PKX", "LHR", computeDistance(pkx, lhr), computeEstimatedDurationMin(computeDistance(pkx, lhr)), computeEstimatedCost(computeDistance(pkx, lhr)), airChina);
        addFlight("LHR", "JFK", computeDistance(lhr, jfk), computeEstimatedDurationMin(computeDistance(lhr, jfk)), computeEstimatedCost(computeDistance(lhr, jfk)), british);
        addFlight("JFK", "GRU", computeDistance(jfk, gru), computeEstimatedDurationMin(computeDistance(jfk, gru)), computeEstimatedCost(computeDistance(jfk, gru)), delta);
        addFlight("GRU", "NRT", computeDistance(gru, nrt), computeEstimatedDurationMin(computeDistance(gru, nrt)), computeEstimatedCost(computeDistance(gru, nrt)), latam);
        addFlight("MAD", "CDG", computeDistance(mad, cdg), computeEstimatedDurationMin(computeDistance(mad, cdg)), computeEstimatedCost(computeDistance(mad, cdg)), airFrance);
    }


    // Guarda el grafo y aerolíneas en binario
    public boolean saveToDisk(Context ctx) {
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


    // Carga desde binario (si no existe, inicializa datos de ejemplo y reconstruye índices/AVL)
    public static boolean loadFromDisk(Context ctx) {
        if (instance == null) instance = new FlightManager();
        if (ctx == null) return false;

        try (FileInputStream fis = ctx.openFileInput(FILE_NAME);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            Object obj = ois.readObject();
            if (obj instanceof Store) {
                Store store = (Store) obj;
                instance.flightGraph = store.graph;
                instance.airlines = store.airlines;
                instance.rebuildAirlineIndexes();
                instance.initAvl(); // reconstituir AVL tras deserializar
                return true;
            }
            return false;

        } catch (FileNotFoundException e) {
            // Primera ejecución: llenar datos iniciales y reconstruir índices
            instance.loadInitialData();
            instance.rebuildAirlineIndexes();
            instance.initAvl();
            return false;
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

    // Overload nuevo con duración/costo
    public boolean addFlight(String originIata, String destinationIata, double distance, double durationMin, double cost, Airline airline) {
        if (originIata == null || destinationIata == null) return false;

        boolean added = flightGraph.addFlight(originIata, destinationIata, distance, durationMin, cost, airline);
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

    public boolean removeFlight(String originIata, String destinationIata) {
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
        flightAvl = new AvlTree<>(FlightComparators.POR_ORIGEN_DESTINO_DISTANCIA_AEROLINEA_ASC);
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
        flightAvl.setComparator(FlightComparators.POR_ORIGEN_DESTINO_DISTANCIA_AEROLINEA_ASC);
        for (Flight f : getAllFlights()) {
            if (f != null) flightAvl.insert(f);
        }
    }

    private void ensureAvl() {
        if (flightAvl == null) {
            initAvl();
        } else {
            flightAvl.setComparator(FlightComparators.POR_ORIGEN_DESTINO_DISTANCIA_AEROLINEA_ASC);
        }
    }

    public List<Flight> getFlightsSorted() {
        ensureAvl();
        return flightAvl.toSortedList();
    }


    public boolean removeAirport(String iata) {
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
