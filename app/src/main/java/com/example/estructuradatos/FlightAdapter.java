package com.example.estructuradatos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.List;

public class FlightAdapter extends ArrayAdapter<Flight> {

    public FlightAdapter(Context context, List<Flight> flights) {
        super(context, 0, flights);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_flight_item, parent, false);
        }

        Flight flight = getItem(position);
        TextView tvInfo = convertView.findViewById(R.id.tvFlightInfo);
        ImageButton btnDelete = convertView.findViewById(R.id.btnDeleteFlight);
        ImageButton btnEdit = convertView.findViewById(R.id.btnEditFlight);

        if (flight != null) {
            String extra = "";
            if (flight.getDurationMin() > 0) extra += " • " + flight.getDurationMin() + " min";
            if (flight.getCost() > 0) extra += " • $" + flight.getCost();
            
            tvInfo.setText(flight.getOrigin().getIataCode() + " -> " +
                    flight.getDestination().getIataCode() + " (" + flight.getDistance() + " km" + extra + ")");

            btnDelete.setOnClickListener(v -> {
                if (getContext() instanceof FlightListActivity) {
                    ((FlightListActivity) getContext()).onDeleteFlightClick(flight);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (getContext() instanceof FlightListActivity) {
                    ((FlightListActivity) getContext()).onEditFlightClick(flight);
                }
            });
        }
        return convertView;
    }
}