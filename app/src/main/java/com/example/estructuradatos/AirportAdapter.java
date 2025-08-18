package com.example.estructuradatos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.estructuradatos.Airport;

import java.util.List;

public class AirportAdapter extends ArrayAdapter<Airport> {

    public AirportAdapter(Context context, List<Airport> airports) {
        super(context, 0, airports);
    }


    public View getView(int position, View convertView,  ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_airport_item, parent, false);
        }

        Airport airport = getItem(position);
        TextView tvInfo = convertView.findViewById(R.id.tvAirportInfo);
        Button btnDelete = convertView.findViewById(R.id.btnDeleteAirport);

        if (airport != null) {
            tvInfo.setText(airport.getIataCode() + " - " + airport.getName());

            btnDelete.setOnClickListener(v -> {
                // Notificar a la actividad que se debe eliminar este aeropuerto
                if (getContext() instanceof DeleteAirportListActivity) {
                    ((DeleteAirportListActivity) getContext()).onDeleteAirportClick(airport);
                }
            });
        }
        return convertView;
    }
}
