package com.grupo12.sw2p1movil.presentacion;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.grupo12.sw2p1movil.R;
import com.grupo12.sw2p1movil.datos.ApiClient;
import com.grupo12.sw2p1movil.datos.ApiService;
import com.grupo12.sw2p1movil.negocio.NsessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.HashSet;

public class RegisterDeviceActivity extends AppCompatActivity {

    private EditText etName, etEsp32Id;
    private Button btnGuardar;
    private ProgressBar progressBar;

    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_device);

        etName = findViewById(R.id.etName);
        etEsp32Id = findViewById(R.id.etEsp32Id);
        btnGuardar = findViewById(R.id.btnGuardar);
        progressBar = findViewById(R.id.progressBar);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = new NsessionManager(this).getToken();

        btnGuardar.setOnClickListener(v -> validarYGuardar());
    }

    private void validarYGuardar() {
        String name = etName.getText().toString().trim();
        String esp32Id = etEsp32Id.getText().toString().trim();

        if (name.isEmpty() || esp32Id.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        apiService.getAllDevices("Bearer " + token).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HashSet<String> existentes = new HashSet<>();
                    for (int i = 0; i < response.body().size(); i++) {
                        JsonObject obj = response.body().get(i).getAsJsonObject();
                        existentes.add(obj.get("esp32_id").getAsString());
                    }

                    if (existentes.contains(esp32Id)) {
                        ocultarProgress();
                        Toast.makeText(RegisterDeviceActivity.this, "Ese ESP32 ID ya está registrado", Toast.LENGTH_SHORT).show();
                    } else {
                        crearDevice(name, esp32Id);
                    }

                } else {
                    ocultarProgress();
                    Toast.makeText(RegisterDeviceActivity.this, "Error al verificar dispositivos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                ocultarProgress();
                Toast.makeText(RegisterDeviceActivity.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void crearDevice(String name, String esp32Id) {
        JsonObject nuevo = new JsonObject();
        nuevo.addProperty("name", name);
        nuevo.addProperty("esp32_id", esp32Id);

        apiService.createDevice("Bearer " + token, nuevo).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                ocultarProgress();
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterDeviceActivity.this, "Dispositivo registrado", Toast.LENGTH_SHORT).show();
                    finish(); // vuelve al Main
                } else {
                    Toast.makeText(RegisterDeviceActivity.this, "Error al registrar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                ocultarProgress();
                Toast.makeText(RegisterDeviceActivity.this, "Fallo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void ocultarProgress() {
        progressBar.setVisibility(View.GONE);
    }
}