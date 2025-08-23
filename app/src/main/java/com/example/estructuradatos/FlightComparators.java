package com.example.estructuradatos;

import java.util.Comparator;

/**
 * Comparadores reutilizables para ordenar instancias de Flight.
 *
 * Uso típico con AvlTree:
 *   AvlTree<Flight> avl = new AvlTree<>(FlightComparators.BY_DISTANCE_ASC);
 *   // Si avl se deserializa:
 *   avl.setComparator(FlightComparators.BY_DISTANCE_ASC);
 */
public final class FlightComparators {

    private FlightComparators() { /* utility class */ }


    public static final Comparator<Flight> BY_DISTANCE_ASC = (a, b) -> {
        if (a == b) return 0;
        if (a == null) return 1;
        if (b == null) return -1;

        int c = Double.compare(safeDistance(a), safeDistance(b));
        if (c != 0) return c;

        c = safeIata(a.getOrigin()).compareTo(safeIata(b.getOrigin()));
        if (c != 0) return c;

        c = safeIata(a.getDestination()).compareTo(safeIata(b.getDestination()));
        if (c != 0) return c;

        return safeAirline(a).compareTo(safeAirline(b));
    };


    public static final Comparator<Flight> BY_AIRLINE_THEN_DISTANCE = (a, b) -> {
        if (a == b) return 0;
        if (a == null) return 1;
        if (b == null) return -1;

        int c = safeAirline(a).compareTo(safeAirline(b));
        if (c != 0) return c;

        c = Double.compare(safeDistance(a), safeDistance(b));
        if (c != 0) return c;

        c = safeIata(a.getOrigin()).compareTo(safeIata(b.getOrigin()));
        if (c != 0) return c;

        return safeIata(a.getDestination()).compareTo(safeIata(b.getDestination()));
    };


    public static final Comparator<Flight> BY_ORIGIN_THEN_DEST_THEN_DISTANCE = (a, b) -> {
        if (a == b) return 0;
        if (a == null) return 1;
        if (b == null) return -1;

        int c = safeIata(a.getOrigin()).compareTo(safeIata(b.getOrigin()));
        if (c != 0) return c;

        c = safeIata(a.getDestination()).compareTo(safeIata(b.getDestination()));
        if (c != 0) return c;

        c = Double.compare(safeDistance(a), safeDistance(b));
        if (c != 0) return c;

        return safeAirline(a).compareTo(safeAirline(b));
    };

    public static final Comparator<Flight> BY_DURATION_ASC = (a,b) -> {
        int c = Double.compare(a!=null?a.getDurationMin():Double.POSITIVE_INFINITY,
                               b!=null?b.getDurationMin():Double.POSITIVE_INFINITY);
        if (c != 0) return c;
        
        c = safeIata(a.getOrigin()).compareTo(safeIata(b.getOrigin()));
        if (c != 0) return c;
        
        return safeIata(a.getDestination()).compareTo(safeIata(b.getDestination()));
    };

    public static final Comparator<Flight> BY_COST_ASC = (a,b) -> {
        int c = Double.compare(a!=null?a.getCost():Double.POSITIVE_INFINITY,
                               b!=null?b.getCost():Double.POSITIVE_INFINITY);
        if (c != 0) return c;
        
        c = safeIata(a.getOrigin()).compareTo(safeIata(b.getOrigin()));
        if (c != 0) return c;
        
        return safeIata(a.getDestination()).compareTo(safeIata(b.getDestination()));
    };

    private static String safeIata(Airport ap) {
        return (ap != null && ap.getIataCode() != null) ? ap.getIataCode() : "";
    }

    private static String safeAirline(Flight f) {
        Airline al = (f != null) ? f.getAirline() : null;
        return (al != null && al.getName() != null) ? al.getName() : "";
    }

    private static double safeDistance(Flight f) {
        return (f != null) ? f.getDistance() : Double.POSITIVE_INFINITY;
    }
}
