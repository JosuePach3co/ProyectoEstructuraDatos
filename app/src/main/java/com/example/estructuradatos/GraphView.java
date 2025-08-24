package com.example.estructuradatos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Collection;

public class GraphView extends View {

    private GraphAL flightGraph;
    private Paint flightPaint, textPaint;
    private float startX, startY;
    private long startTime;
    private static final int MAX_CLICK_DURATION = 200;
    private static final int MAX_CLICK_DISTANCE = 20;
    private Bitmap airportPrincipalBitmap, airportSecundarioBitmap;
    public static final int AIRPORT_SIZE = 90;

    // Preview de intento de aeropuerto
    private Float previewX = null, previewY = null;
    private boolean previewValido = true;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        flightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        flightPaint.setColor(Color.GRAY);
        flightPaint.setStrokeWidth(5f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40f);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        airportPrincipalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.aeropuerto_principal);
        if (airportPrincipalBitmap != null) {
            airportPrincipalBitmap = Bitmap.createScaledBitmap(airportPrincipalBitmap, AIRPORT_SIZE, AIRPORT_SIZE, false);
        }

        airportSecundarioBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.aeropuerto_secundarios);
        if (airportSecundarioBitmap != null) {
            airportSecundarioBitmap = Bitmap.createScaledBitmap(airportSecundarioBitmap, AIRPORT_SIZE, AIRPORT_SIZE, false);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (flightGraph == null || flightGraph.getVertices() == null || flightGraph.getVertices().isEmpty())
            return;

        // Dibujar vuelos
        for (Airport origin : flightGraph.getVertices()) {
            for (Flight flight : origin.getFlightList()) {
                Airport dest = flight.getDestination();

                float startX = origin.getX();
                float startY = origin.getY();
                float endX = dest.getX();
                float endY = dest.getY();

                // Línea principal
                canvas.drawLine(startX, startY, endX, endY, flightPaint);

                // Calcular ángulo y flecha
                float dx = endX - startX;
                float dy = endY - startY;
                double angle = Math.atan2(dy, dx);
                float arrowLen = 60f; // largo de la flecha
                float arrowAngle = (float) Math.toRadians(40); // apertura
                float tipX = endX - (float) Math.cos(angle) * (AIRPORT_SIZE / 1.5f);
                float tipY = endY - (float) Math.sin(angle) * (AIRPORT_SIZE / 1.5f);

                // Coordenadas de las alas de la flecha
                float x1 = tipX - (float) Math.cos(angle - arrowAngle) * arrowLen;
                float y1 = tipY - (float) Math.sin(angle - arrowAngle) * arrowLen;
                float x2 = tipX - (float) Math.cos(angle + arrowAngle) * arrowLen;
                float y2 = tipY - (float) Math.sin(angle + arrowAngle) * arrowLen;

                // Dibujar las dos líneas de la flecha
                canvas.drawLine(tipX, tipY, x1, y1, flightPaint);
                canvas.drawLine(tipX, tipY, x2, y2, flightPaint);
            }
        }

        // Dibujar aeropuertos
        for (Airport airport : flightGraph.getVertices()) {
            float cx = airport.getX();
            float cy = airport.getY();
            float left = cx - AIRPORT_SIZE / 2f;
            float top = cy - AIRPORT_SIZE / 2f;

            Bitmap icon = null;
            if (airport.getIataCode().equalsIgnoreCase("PKX")) {
                icon = airportPrincipalBitmap;
            }
            else{
                icon = airportSecundarioBitmap;
            }


            if (icon != null) {
                canvas.drawBitmap(icon, left, top, null);
            } else {
                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setColor(Color.BLUE);
                canvas.drawCircle(cx, cy, AIRPORT_SIZE / 2f, p);
            }

            canvas.drawText(airport.getIataCode(), left - 10, top + AIRPORT_SIZE + 20, textPaint);
        }

        // Dibujar preview
        if (previewX != null && previewY != null) {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5f);
            p.setColor(previewValido ? Color.GREEN : Color.RED);
            canvas.drawCircle(previewX, previewY, AIRPORT_SIZE, p);
        }
    }

    public void setGraph(GraphAL graph) {
        this.flightGraph = graph;
        invalidate();
    }

    public void setPreview(Float x, Float y, boolean valido) {
        this.previewX = x;
        this.previewY = y;
        this.previewValido = valido;
        invalidate();
    }

    // Validación de distancia mínima
    public boolean isPositionValid(float x, float y, Collection<Airport> airports, float minDistance) {
        for (Airport existente : airports) {
            float dx = existente.getX() - x;
            float dy = existente.getY() - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < minDistance) return false;
        }
        return true;
    }

    // Listeners
    public interface OnAirportClickListener {
        void onAirportClick(Airport airport);
    }

    public interface OnEmptySpaceClickListener {
        void onEmptySpaceClick(float x, float y);
    }

    private OnAirportClickListener onAirportClickListener;
    private OnEmptySpaceClickListener onEmptySpaceClickListener;

    public void setOnAirportClickListener(OnAirportClickListener listener) {
        this.onAirportClickListener = listener;
    }

    public void setOnEmptySpaceClickListener(OnEmptySpaceClickListener listener) {
        this.onEmptySpaceClickListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                startTime = System.currentTimeMillis();
                return true;

            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                if (flightGraph != null) {
                    previewValido = isPositionValid(moveX, moveY, flightGraph.getVertices(), MainActivity.DISTANCIA_MINIMA);
                    setPreview(moveX, moveY, previewValido);
                }
                return true;

            case MotionEvent.ACTION_UP:
                long clickDuration = System.currentTimeMillis() - startTime;
                float endX = event.getX();
                float endY = event.getY();
                float dist = distance(startX, startY, endX, endY);

                if (clickDuration < MAX_CLICK_DURATION && dist < MAX_CLICK_DISTANCE) {
                    if (flightGraph != null) {
                        for (Airport airport : flightGraph.getVertices()) {
                            float ax = airport.getX();
                            float ay = airport.getY();
                            float radius = AIRPORT_SIZE / 2f;

                            if ((endX - ax) * (endX - ax) + (endY - ay) * (endY - ay) <= radius * radius) {
                                if (onAirportClickListener != null) {
                                    onAirportClickListener.onAirportClick(airport);
                                }
                                return true;
                            }
                        }
                    }
                    if (onEmptySpaceClickListener != null) {
                        onEmptySpaceClickListener.onEmptySpaceClick(endX, endY);
                    }
                    return true;
                }
                return true;
        }
        return false;
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}