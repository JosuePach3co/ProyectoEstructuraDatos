package com.example.estructuradatos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;

public class DeleteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_activity);

        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        RadioButton radioAirport = findViewById(R.id.radioAirport);
        RadioButton radioFlight = findViewById(R.id.radioFlight);
        Button btnAceptar = findViewById(R.id.btnAceptar);

        btnAceptar.setOnClickListener(v -> {
            Intent intent = null;
            if (radioAirport.isChecked()) {
                intent = new Intent(this, DeleteAirportListActivity.class);
            } else if (radioFlight.isChecked()) {
                intent = new Intent(this, DeleteFlightListActivity.class);
            }
            if (intent != null) {
                startActivity(intent);
            }
        });
    }
}