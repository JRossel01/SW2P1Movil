package com.grupo12.sw2p1movil.presentacion;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
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
import java.util.*;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;


public class CreateProcessActivity extends AppCompatActivity {

    private EditText etNombreProceso;
    private Spinner spinnerTrigger;
    private LinearLayout layoutValueTrigger;
    private Button btnCrearProceso;

    private Spinner spinnerActions;
    private Button btnAgregarAccion;
    private LinearLayout layoutAccionesSeleccionadas;

    private List<JsonObject> actionsList = new ArrayList<>();
    private List<JsonObject> accionesSeleccionadas = new ArrayList<>();
    private ArrayAdapter<String> actionAdapter;
    private Map<String, Integer> actionMap = new HashMap<>();

    private List<JsonObject> triggerList = new ArrayList<>();
    private int selectedTriggerId = -1;
    private String valueTriggerFinal = ""; // este será el que se enviará al backend
    private TextView tvReactivation;


    private Spinner spinnerReactivation;
    private String reactivationValue = "always";
    private final Map<String, String> reactivationMap = new HashMap<>();
    private final Map<String, String> operatorMap = new HashMap<>();

    private Spinner spinnerOperator;
    private String operatorValue = "="; // valor por defecto


    private int deviceId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_process);

        etNombreProceso = findViewById(R.id.etNombreProceso);
        tvReactivation = findViewById(R.id.tvReactivation);
        spinnerTrigger = findViewById(R.id.spinnerTrigger);
        layoutValueTrigger = findViewById(R.id.layoutValueTrigger);
        spinnerOperator = findViewById(R.id.spinnerOperator);
        spinnerOperator.setVisibility(View.GONE);


        operatorMap.put("Igual a", "=");
        operatorMap.put("Mayor que", ">");
        operatorMap.put("Menor que", "<");

        ArrayAdapter<String> operatorAdapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, new ArrayList<>(operatorMap.keySet())
        );
        operatorAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerOperator.setAdapter(operatorAdapter);
        spinnerOperator.setSelection(0); // "Igual a" por defecto
        operatorValue = operatorMap.get("Igual a");

        spinnerOperator.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String seleccion = parent.getItemAtPosition(position).toString();
                operatorValue = operatorMap.get(seleccion);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnCrearProceso = findViewById(R.id.btnCrearProceso);

        spinnerReactivation = findViewById(R.id.spinnerReactivation);

        reactivationMap.put("Siempre", "always");
        reactivationMap.put("Solo una vez", "never");
        reactivationMap.put("Una vez al día", "daily");
        reactivationMap.put("Dos veces al día", "twice");

        ArrayAdapter<String> reactivationAdapter = new ArrayAdapter<>(
                this, R.layout.spinner_item,
                new ArrayList<>(reactivationMap.keySet())
        );
        reactivationAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerReactivation.setAdapter(reactivationAdapter);
        spinnerReactivation.setSelection(0); // default: "Siempre"

        spinnerReactivation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String seleccion = parent.getItemAtPosition(position).toString();
                reactivationValue = reactivationMap.get(seleccion);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        spinnerActions = findViewById(R.id.spinnerActions);
        btnAgregarAccion = findViewById(R.id.btnAgregarAccion);
        layoutAccionesSeleccionadas = findViewById(R.id.layoutAccionesSeleccionadas);

        obtenerActions();

        btnAgregarAccion.setOnClickListener(v -> agregarAccionSeleccionada());


        obtenerTriggers();

        spinnerTrigger.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                JsonObject trigger = triggerList.get(position);
                selectedTriggerId = trigger.get("id").getAsInt();
                actualizarCampoValueTrigger();

                // Mostrar u ocultar spinner de operador (solo si trigger es "Luz exterior", id = 1)
                if (selectedTriggerId == 1) {
                    spinnerOperator.setVisibility(View.VISIBLE);
                } else {
                    spinnerOperator.setVisibility(View.GONE);
                    operatorValue = "="; // se fija automáticamente
                }

                // Mostrar u ocultar reactivación según tipo de trigger
                if (selectedTriggerId == 5 || selectedTriggerId == 6) {
                    spinnerReactivation.setVisibility(View.GONE);
                    tvReactivation.setVisibility(View.GONE);
                    reactivationValue = "always";
                } else {
                    spinnerReactivation.setVisibility(View.VISIBLE);
                    tvReactivation.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        btnCrearProceso.setOnClickListener(v -> {
            String nombre = etNombreProceso.getText().toString().trim();
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Escribe un nombre para el proceso", Toast.LENGTH_SHORT).show();
                return;
            }

            deviceId = getIntent().getIntExtra("device_id", -1);
            if (deviceId == -1) {
                Toast.makeText(this, "Error: dispositivo no recibido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedTriggerId == 6) {
                obtenerBnDesdeBackend(deviceId, bn -> {
                    if (bn == null) {
                        Toast.makeText(this, "No hay botones disponibles", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    valueTriggerFinal = bn;
                    crearProceso(nombre);  // ✅ función separada
                });
            } else {
                crearProceso(nombre);  // ✅ función directa
            }
        });

    }

    private void crearProceso(String nombre) {
        JsonObject data = new JsonObject();
        data.addProperty("name", nombre);
        data.addProperty("trigger_id", selectedTriggerId);
        data.addProperty("device_id", deviceId);
        data.addProperty("value_trigger", valueTriggerFinal);
        data.addProperty("operator", operatorValue);
        data.addProperty("reactivation", reactivationValue);

        JsonArray actionsArray = new JsonArray();
        for (JsonObject acc : accionesSeleccionadas) {
            JsonObject actionObj = new JsonObject();
            actionObj.addProperty("action_id", acc.get("action_id").getAsInt());
            actionObj.addProperty("order", acc.get("order").getAsInt());
            actionsArray.add(actionObj);
        }
        data.add("actions", actionsArray);

        Log.d("JSONaEnviar", data.toString());

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        NsessionManager session = new NsessionManager(this);
        String token = session.getToken();

        apiService.createProcess("Bearer " + token, data).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateProcessActivity.this, "Proceso creado exitosamente", Toast.LENGTH_SHORT).show();

                    if (selectedTriggerId == 6) {
                        JsonObject boton = new JsonObject();
                        boton.addProperty("device_id", deviceId);
                        boton.addProperty("button_id", valueTriggerFinal);
                        boton.addProperty("process_name", nombre);

                        apiService.createButton("Bearer " + token, boton).enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                if (response.isSuccessful()) {
                                    Log.d("Boton", "Botón guardado correctamente en el backend");
                                } else {
                                    Log.e("Boton", "Error al guardar botón en backend");
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                Log.e("Boton", "Fallo de red al guardar botón: " + t.getMessage());
                            }
                        });
                    }

                    startActivity(new Intent(CreateProcessActivity.this, MainActivity.class));
                    finish();

                } else {
                    Toast.makeText(CreateProcessActivity.this, "Error al crear proceso", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(CreateProcessActivity.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void obtenerTriggers() {
        NsessionManager session = new NsessionManager(this);
        String token = session.getToken();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<JsonArray> call = apiService.getTriggers("Bearer " + token);

        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> nombres = new ArrayList<>();
                    triggerList.clear();

                    for (JsonElement el : response.body()) {
                        JsonObject obj = el.getAsJsonObject();
                        triggerList.add(obj);
                        nombres.add(obj.get("name").getAsString());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            CreateProcessActivity.this,
                            R.layout.spinner_item,
                            nombres
                    );
                    adapter.setDropDownViewResource(R.layout.spinner_item);
                    spinnerTrigger.setAdapter(adapter);
                } else {
                    Toast.makeText(CreateProcessActivity.this, "No se pudieron cargar los triggers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                Toast.makeText(CreateProcessActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarCampoValueTrigger() {
        layoutValueTrigger.removeAllViews();
        valueTriggerFinal = "";

        if (selectedTriggerId == 1) {
            String[] opciones = {"0", "1", "2", "3", "4", "5"};
            Spinner s = crearSpinner(opciones, selected -> valueTriggerFinal = selected);
            layoutValueTrigger.addView(s);
            layoutValueTrigger.setVisibility(View.VISIBLE);

        } else if (selectedTriggerId == 2) {
            String[] opciones = {"Encendida", "Apagada"};
            Spinner s = crearSpinner(opciones, selected -> valueTriggerFinal = selected.equals("Encendida") ? "1" : "0");
            layoutValueTrigger.addView(s);
            layoutValueTrigger.setVisibility(View.VISIBLE);

        } else if (selectedTriggerId == 3 || selectedTriggerId == 4) {
            valueTriggerFinal = "1";
            layoutValueTrigger.setVisibility(View.GONE);

        } else if (selectedTriggerId == 5) {
            Button btnHora = new Button(this);
            btnHora.setText("Seleccionar hora");
            btnHora.setTextColor(Color.rgb(238, 238, 238)); // blanco
            btnHora.setTextSize(16);
            btnHora.setTypeface(null, Typeface.BOLD);
            btnHora.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));

            GradientDrawable fondo = new GradientDrawable();
            fondo.setColor(Color.rgb(0, 173, 181)); // celeste
            fondo.setCornerRadius(dpToPx(20));
            btnHora.setBackground(fondo);

            btnHora.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                int hora = c.get(Calendar.HOUR_OF_DAY);
                int minuto = c.get(Calendar.MINUTE);

                new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                    valueTriggerFinal = String.format("%02d:%02d", hourOfDay, minute);
                    btnHora.setText("Hora: " + valueTriggerFinal);
                }, hora, minuto, true).show();
            });

            layoutValueTrigger.addView(btnHora);
            layoutValueTrigger.setVisibility(View.VISIBLE);

        } else if (selectedTriggerId == 6) {
            obtenerBnDesdeBackend(deviceId, bn -> {
                valueTriggerFinal = bn;
                if (bn == null) {
                    Toast.makeText(CreateProcessActivity.this, "No hay botones disponibles para este dispositivo", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CreateProcessActivity.this, "Se asignará: " + bn, Toast.LENGTH_SHORT).show();
                }
            });
            layoutValueTrigger.setVisibility(View.GONE);
        }
    }

    private Spinner crearSpinner(String[] opciones, final OnOptionSelected listener) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, opciones);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);
        spinner.setPopupBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.rgb(57, 62, 70))); // gris oscuro
        spinner.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(0, 173, 181))); // celeste

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                ((TextView) view).setTextColor(Color.rgb(238, 238, 238)); // blanco
                ((TextView) view).setTextSize(16);
                ((TextView) view).setTypeface(null, Typeface.BOLD);
                listener.onSelected(opciones[pos]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return spinner;
    }

    private interface OnOptionSelected {
        void onSelected(String value);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void obtenerActions() {
        NsessionManager session = new NsessionManager(this);
        String token = session.getToken();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<JsonArray> call = apiService.getActions("Bearer " + token);

        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> nombres = new ArrayList<>();
                    actionsList.clear();
                    actionMap.clear();

                    for (JsonElement el : response.body()) {
                        JsonObject obj = el.getAsJsonObject();
                        actionsList.add(obj);
                        String nombre = obj.get("display_name").getAsString();
                        nombres.add(nombre);
                        actionMap.put(nombre, obj.get("id").getAsInt());
                    }

                    actionAdapter = new ArrayAdapter<>(CreateProcessActivity.this, R.layout.spinner_item, nombres);
                    actionAdapter.setDropDownViewResource(R.layout.spinner_item);
                    spinnerActions.setAdapter(actionAdapter);
                } else {
                    Toast.makeText(CreateProcessActivity.this, "No se pudieron cargar las acciones", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                Toast.makeText(CreateProcessActivity.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void agregarAccionSeleccionada() {
        if (spinnerActions.getSelectedItem() == null) return;

        String seleccion = spinnerActions.getSelectedItem().toString();
        int actionId = actionMap.get(seleccion);

        JsonObject accion = new JsonObject();
        accion.addProperty("action_id", actionId);
        accion.addProperty("order", accionesSeleccionadas.size() + 1);
        accion.addProperty("nombre", seleccion); // solo para mostrar, no se envía

        accionesSeleccionadas.add(accion);
        actionAdapter.remove(seleccion);
        actionAdapter.notifyDataSetChanged();

        mostrarAccionesSeleccionadas();
    }

    private void mostrarAccionesSeleccionadas() {
        layoutAccionesSeleccionadas.removeAllViews();

        for (int i = 0; i < accionesSeleccionadas.size(); i++) {
            JsonObject acc = accionesSeleccionadas.get(i);
            String nombre = acc.get("nombre").getAsString();
            int orden = i + 1;

            TextView tv = new TextView(this);
            tv.setText(orden + ". " + nombre);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(16);
            tv.setPadding(12, 10, 12, 10);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setBackgroundColor(Color.parseColor("#393E46"));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 8, 0, 8);
            tv.setLayoutParams(params);

            int finalI = i;
            tv.setOnClickListener(v -> {
                String volver = accionesSeleccionadas.get(finalI).get("nombre").getAsString();
                actionAdapter.add(volver);
                actionAdapter.notifyDataSetChanged();
                accionesSeleccionadas.remove(finalI);
                reordenar();
                mostrarAccionesSeleccionadas();
            });

            layoutAccionesSeleccionadas.addView(tv);
        }
    }

    private void reordenar() {
        for (int i = 0; i < accionesSeleccionadas.size(); i++) {
            accionesSeleccionadas.get(i).addProperty("order", i + 1);
        }
    }

    private void obtenerBnDesdeBackend(int deviceId, OnBnDisponibleListener listener) {
        NsessionManager session = new NsessionManager(this);
        String token = session.getToken();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getButtons("Bearer " + token).enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HashSet<String> usados = new HashSet<>();
                    for (JsonObject boton : response.body()) {
                        int devId = boton.get("device_id").getAsInt();
                        if (devId == deviceId) {
                            usados.add(boton.get("button_id").getAsString());
                        }
                    }

                    for (int i = 1; i <= 20; i++) {
                        String candidato = "B" + i;
                        if (!usados.contains(candidato)) {
                            listener.onDisponible(candidato);
                            return;
                        }
                    }

                    listener.onDisponible(null); // No disponible
                } else {
                    Toast.makeText(CreateProcessActivity.this, "Error al obtener botones", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                Toast.makeText(CreateProcessActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private interface OnBnDisponibleListener {
        void onDisponible(String bn);
    }


}