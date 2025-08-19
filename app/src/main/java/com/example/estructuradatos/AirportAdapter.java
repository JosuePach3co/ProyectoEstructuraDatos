package com.example.estructuradatos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.List;

public class AirportAdapter extends ArrayAdapter<Airport> {

    public AirportAdapter(Context context, List<Airport> airports) {
        super(context, 0, airports);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_airport_item, parent, false);
        }

        Airport airport = getItem(position);
        TextView tvInfo = convertView.findViewById(R.id.tvAirportInfo);
        ImageButton btnDelete = convertView.findViewById(R.id.btnDeleteAirport);
        ImageButton btnEdit = convertView.findViewById(R.id.btnEditAirport);

        if (airport != null) {
            tvInfo.setText(airport.getIataCode() + " - " + airport.getName());

            btnDelete.setOnClickListener(v -> {
                if (getContext() instanceof AirportListActivity) {
                    ((AirportListActivity) getContext()).onDeleteAirportClick(airport);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (getContext() instanceof AirportListActivity) {
                    ((AirportListActivity) getContext()).onEditAirportClick(airport);
                }
            });
        }
        return convertView;
    }
}
