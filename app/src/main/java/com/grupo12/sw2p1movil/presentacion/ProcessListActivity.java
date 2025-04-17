package com.grupo12.sw2p1movil.presentacion;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.grupo12.sw2p1movil.R;
import com.grupo12.sw2p1movil.datos.ApiClient;
import com.grupo12.sw2p1movil.datos.ApiService;
import com.grupo12.sw2p1movil.negocio.NsessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessListActivity extends AppCompatActivity {

    private LinearLayout layoutProcesos;
    private int deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_list);

        layoutProcesos = findViewById(R.id.layoutProcesos);
        Intent intent = getIntent();
        deviceId = intent.getIntExtra("device_id", -1);
        Log.d("ProcessDebug", "Recibido device_id = " + deviceId);

        if (deviceId == -1) {
            Toast.makeText(this, "Dispositivo inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarProcesos();

        Button btnCrearProceso = findViewById(R.id.btnCrearProceso);
        btnCrearProceso.setOnClickListener(v -> {
            Intent nuevoIntent  = new Intent(ProcessListActivity.this, CreateProcessActivity.class);
            nuevoIntent .putExtra("device_id", deviceId); // muy importante: enviar el ID del dispositivo
            startActivity(nuevoIntent );

            //Toast.makeText(ProcessListActivity.this, "Función para crear proceso (próximamente)", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarProcesos() {
        layoutProcesos.removeAllViews();

        NsessionManager session = new NsessionManager(this);
        String token = session.getToken();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<JsonArray> call = apiService.getProcesses("Bearer " + token, deviceId);

        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (JsonElement element : response.body()) {
                        JsonObject proceso = element.getAsJsonObject();

                        int processId = proceso.get("id").getAsInt();
                        String name = proceso.get("name").getAsString();
                        String valueTrigger = proceso.has("value_trigger") ? proceso.get("value_trigger").getAsString() : "";

                        // Trigger info
                        String trigger = "Desconocido";
                        if (proceso.has("trigger") && proceso.get("trigger").isJsonObject()) {
                            JsonObject triggerObj = proceso.getAsJsonObject("trigger");
                            if (triggerObj.has("name")) {
                                trigger = triggerObj.get("name").getAsString();
                            }
                        }

                        // Lista de acciones
                        StringBuilder acciones = new StringBuilder();
                        if (proceso.has("process_actions") && proceso.get("process_actions").isJsonArray()) {
                            JsonArray actionsArray = proceso.getAsJsonArray("process_actions");
                            for (JsonElement actEl : actionsArray) {
                                JsonObject actObj = actEl.getAsJsonObject();
                                if (actObj.has("action") && actObj.get("action").isJsonObject()) {
                                    JsonObject actionInner = actObj.getAsJsonObject("action");
                                    if (actionInner.has("display_name")) {
                                        acciones.append("• ").append(actionInner.get("display_name").getAsString()).append("\n");
                                    }
                                }
                            }
                        }

                        // --- TARJETA PRINCIPAL ---
                        LinearLayout tarjeta = new LinearLayout(ProcessListActivity.this);
                        tarjeta.setOrientation(LinearLayout.VERTICAL);
                        tarjeta.setPadding(dpToPx(0), dpToPx(0), dpToPx(0), dpToPx(0));
                        GradientDrawable fondo = new GradientDrawable();
                        fondo.setColor(Color.parseColor("#393E46")); // gris fondo
                        fondo.setCornerRadius(dpToPx(18));
                        tarjeta.setBackground(fondo);
                        LinearLayout.LayoutParams tarjetaParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        tarjetaParams.setMargins(0, dpToPx(10), 0, dpToPx(10));
                        tarjeta.setLayoutParams(tarjetaParams);

                        // --- TÍTULO: Nombre del proceso ---
                        TextView tvNombre = new TextView(ProcessListActivity.this);
                        tvNombre.setText(name);
                        tvNombre.setBackgroundColor(Color.parseColor("#417685")); // azul medio verdoso
                        tvNombre.setTextColor(Color.WHITE);
                        tvNombre.setTypeface(null, Typeface.BOLD);
                        tvNombre.setTextSize(18);
                        tvNombre.setGravity(Gravity.CENTER);
                        tvNombre.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));

                        // --- Trigger + valor ---
                        TextView tvTrigger = new TextView(ProcessListActivity.this);
                        tvTrigger.setText(trigger + " " + valueTrigger);
                        tvTrigger.setTextColor(Color.parseColor("#EEEEEE")); // Cambiado desde LTGRAY
                        tvTrigger.setTextSize(15); // Subido de 14 a 15
                        tvTrigger.setTypeface(null, Typeface.NORMAL);
                        tvTrigger.setPadding(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(10));

                        // --- Sección acciones label ---
                        TextView tvAccLabel = new TextView(ProcessListActivity.this);
                        tvAccLabel.setText("ACCIONES"); // MAYÚSCULAS
                        tvAccLabel.setBackgroundColor(Color.parseColor("#00ADB5"));
                        tvAccLabel.setTextColor(Color.WHITE);
                        tvAccLabel.setTypeface(null, Typeface.BOLD);
                        tvAccLabel.setTextSize(17); // Subido de 16 a 17
                        tvAccLabel.setGravity(Gravity.CENTER);
                        tvAccLabel.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10)); // Ajustado

                        // --- Lista de acciones ---
                        TextView tvAcciones = new TextView(ProcessListActivity.this);
                        tvAcciones.setText(acciones.toString().trim());
                        tvAcciones.setTextColor(Color.WHITE);
                        tvAcciones.setTextSize(15); // Subido de 14 a 15
                        tvAcciones.setTypeface(Typeface.MONOSPACE); // Fuente tipo consola
                        tvAcciones.setPadding(dpToPx(20), dpToPx(10), dpToPx(20), dpToPx(10)); // Espaciado interior


                        // --- Botón eliminar ---
                        Button btnEliminar = new Button(ProcessListActivity.this);
                        btnEliminar.setText("Eliminar");
                        btnEliminar.setTextColor(Color.WHITE);
                        btnEliminar.setTypeface(null, Typeface.BOLD);
                        btnEliminar.setBackgroundColor(Color.parseColor("#F08A5D")); // naranja
                        GradientDrawable eliminarFondo = new GradientDrawable();
                        eliminarFondo.setCornerRadius(dpToPx(20));
                        eliminarFondo.setColor(Color.parseColor("#F08A5D"));
                        btnEliminar.setBackground(eliminarFondo);
                        btnEliminar.setTextSize(15);
                        btnEliminar.setPadding(dpToPx(12), dpToPx(14), dpToPx(12), dpToPx(14));
                        LinearLayout.LayoutParams eliminarParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        eliminarParams.setMargins(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(16));
                        btnEliminar.setLayoutParams(eliminarParams);
                        int triggerId = proceso.has("trigger_id") ? proceso.get("trigger_id").getAsInt() : -1;
                        btnEliminar.setOnClickListener(v -> eliminarProceso(processId, triggerId, valueTrigger));

                        // --- Agregar todos los elementos a la tarjeta ---
                        tarjeta.addView(tvNombre);
                        tarjeta.addView(tvTrigger);
                        tarjeta.addView(tvAccLabel);
                        tarjeta.addView(tvAcciones);
                        tarjeta.addView(btnEliminar);

                        layoutProcesos.addView(tarjeta);
                    }
                } else {
                    Toast.makeText(ProcessListActivity.this, "No se pudieron cargar los procesos", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                Toast.makeText(ProcessListActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void eliminarProceso(int id, int triggerId, String valueTrigger) {
        NsessionManager session = new NsessionManager(this);
        String token = session.getToken();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Void> call = apiService.deleteProcess("Bearer " + token, id);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProcessListActivity.this, "Proceso eliminado", Toast.LENGTH_SHORT).show();

                    // Eliminar botón si el trigger es tipo botón (id = 8)
                    if (triggerId == 8) {
                        apiService.deleteButton("Bearer " + token, deviceId, valueTrigger)
                                .enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        if (response.isSuccessful()) {
                                            Log.d("Boton", "Botón eliminado del backend");
                                        } else {
                                            Log.e("Boton", "Error al eliminar botón del backend");
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                        Log.e("Boton", "Fallo de red al eliminar botón: " + t.getMessage());
                                    }
                                });
                    }


                    cargarProcesos();
                } else {
                    Toast.makeText(ProcessListActivity.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ProcessListActivity.this, "Fallo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Conversión de dp a px
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}