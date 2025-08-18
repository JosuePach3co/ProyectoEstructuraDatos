package com.example.estructuradatos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;

public class FlightAdapter extends ArrayAdapter<Flight> {

    public FlightAdapter(Context context, List<Flight> flights) {
        super(context, 0, flights);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_flight_item, parent, false);
        }

        Flight flight = getItem(position);
        TextView tvInfo = convertView.findViewById(R.id.tvFlightInfo);
        Button btnDelete = convertView.findViewById(R.id.btnDeleteFlight);

        if (flight != null) {
            tvInfo.setText(flight.getOrigin().getIataCode() + " -> " + flight.getDestination().getIataCode() + " (" + flight.getDistance() + " km)");

            btnDelete.setOnClickListener(v -> {
                // Notificar a la actividad que se debe eliminar este vuelo
                if (getContext() instanceof DeleteFlightListActivity) {
                    ((DeleteFlightListActivity) getContext()).onDeleteFlightClick(flight);
                }
            });
        }
        return convertView;
    }
}