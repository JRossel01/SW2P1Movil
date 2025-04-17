package com.grupo12.sw2p1movil.presentacion;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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

public class DeviceListActivity extends AppCompatActivity {

    private LinearLayout layoutDispositivos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        layoutDispositivos = findViewById(R.id.layoutDispositivos);

        NsessionManager session = new NsessionManager(this);
        String token = session.getToken();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<JsonArray> call = apiService.getDevices("Bearer " + token);

        call.enqueue(new Callback<JsonArray>() {

            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                Log.d("TokenDebug", "Token: " + token);
                Log.d("DevicesDebug", "Respuesta: " + response.body());
                Log.e("DevicesError", "Código: " + response.code());
                if (response.isSuccessful() && response.body() != null) {

                    boolean[] intentLanzado = {false};

                    for (JsonElement element : response.body()) {
                        JsonObject obj = element.getAsJsonObject();
                        int deviceId = obj.get("id").getAsInt();
                        String deviceName = obj.get("name").getAsString();

                        Button b = new Button(DeviceListActivity.this);
                        b.setText("Dispositivo " + deviceName);
                        b.setTextColor(Color.WHITE);
                        b.setTextSize(16);
                        b.setTypeface(null, Typeface.BOLD);
                        b.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                        b.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        b.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));

                        // Fondo con esquinas redondeadas y color oscuro
                        GradientDrawable background = new GradientDrawable();
                        background.setColor(Color.parseColor("#393E46")); // plomo oscuro
                        background.setCornerRadius(dpToPx(16));
                        b.setBackground(background);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                dpToPx(70)
                        );
                        params.setMargins(0, dpToPx(6), 0, dpToPx(6));
                        b.setLayoutParams(params);

                        b.setOnClickListener(v -> {
                            Intent intent = new Intent(DeviceListActivity.this, ProcessListActivity.class);
                            intent.putExtra("device_id", deviceId);
                            startActivity(intent);
                        });


                        layoutDispositivos.addView(b);
                    }
                } else {
                    Toast.makeText(DeviceListActivity.this, "No se pudieron obtener los dispositivos", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                Toast.makeText(DeviceListActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Conversión de dp a px
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}