package com.grupo12.sw2p1movil.presentacion;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.grupo12.sw2p1movil.datos.ApiClient;
import com.grupo12.sw2p1movil.datos.ApiService;
import com.grupo12.sw2p1movil.negocio.NsessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.grupo12.sw2p1movil.R;

import java.util.List;

public class ButtonListActivity extends AppCompatActivity {

    private GridLayout layoutBotones;

    // Colores rotativos para los botones
    private final int[] colores = {
            0xFFF08A5D, // naranja suave
            0xFF00ADB5, // cian vibrante
            0xFF4CAF50, // verde
            0xFF2196F3, // azul
            0xFF9C27B0  // púrpura
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_list);

        layoutBotones = findViewById(R.id.layoutBotones);

        NsessionManager session = new NsessionManager(this);
        String token = session.getToken();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getButtons("Bearer " + token).enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<JsonObject> botones = response.body();

                    for (int i = 0; i < botones.size(); i++) {
                        JsonObject btn = botones.get(i);

                        int deviceId = btn.get("device_id").getAsInt();
                        String buttonId = btn.get("button_id").getAsString();

                        Button b = new Button(ButtonListActivity.this);
                        String processName = btn.has("process_name") ? btn.get("process_name").getAsString() : ("Botón " + buttonId);
                        b.setText(processName);
                        b.setAllCaps(false);
                        b.setTextColor(0xFFEEEEEE);
                        b.setTextSize(16);
                        b.setPadding(12, 12, 12, 12);
                        b.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        b.setTypeface(null, android.graphics.Typeface.BOLD);

                        GradientDrawable background = new GradientDrawable();
                        background.setColor(colores[i % colores.length]);
                        background.setCornerRadius(dpToPx(20));
                        b.setBackground(background);

                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = 0;
                        params.height = dpToPx(90);
                        params.columnSpec = (botones.size() == 1) ? GridLayout.spec(0, 2, 1f) : GridLayout.spec(GridLayout.UNDEFINED, 1f);
                        params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
                        b.setLayoutParams(params);

                        b.setOnClickListener(v -> {
                            JsonObject json = new JsonObject();
                            json.addProperty("trigger_id", 6);
                            json.addProperty("device_id", deviceId);
                            json.addProperty("value", buttonId);

                            Log.d("JSONButton", json.toString());

                            apiService.sendButtonTrigger("Bearer " + token, json).enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(ButtonListActivity.this, "Botón enviado correctamente", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ButtonListActivity.this, "Error al enviar botón", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    Toast.makeText(ButtonListActivity.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        });

                        layoutBotones.addView(b);
                    }

                } else {
                    Toast.makeText(ButtonListActivity.this, "No se pudieron cargar los botones", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                Toast.makeText(ButtonListActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    // Conversión de dp a px
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}