package com.example.estructuradatos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AddAirportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_airport);

        EditText inputNombre = findViewById(R.id.inputNombre);
        EditText inputIata = findViewById(R.id.inputIata);

        ArrayList<String> existingIatas = getIntent().getStringArrayListExtra("existing_iatas");
        float x = getIntent().getFloatExtra("x", 0);
        float y = getIntent().getFloatExtra("y", 0);

        findViewById(R.id.btnAgregar).setOnClickListener(v -> {
            String nombre = inputNombre.getText().toString().trim();
            String iata = inputIata.getText().toString().trim().toUpperCase();

            if(nombre.isEmpty() || iata.isEmpty()){
                Toast.makeText(this, "Completa ambos campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if(existingIatas != null && existingIatas.contains(iata)){
                Toast.makeText(this, "IATA ya existe", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent result = new Intent();
            result.putExtra("iata", iata);
            result.putExtra("nombre", nombre);
            result.putExtra("x", x);
            result.putExtra("y", y);
            setResult(RESULT_OK, result);
            finish();
        });

        findViewById(R.id.btnCancelar).setOnClickListener(v -> finish());
    }
}