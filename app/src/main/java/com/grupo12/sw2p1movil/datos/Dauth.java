package com.grupo12.sw2p1movil.datos;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Dauth {
    public interface LoginCallback {
        void onSuccess(String token);
        void onError(String message);
    }

    public static void login(String email, String password, LoginCallback callback) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Crear cuerpo JSON con email y password
        JsonObject json = new JsonObject();
        json.addProperty("email", email);
        json.addProperty("password", password);

        Call<JsonObject> call = apiService.login(json);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("token")) {
                        String fullToken = body.get("token").getAsString();
                        String token = fullToken.contains("|") ? fullToken.split("\\|")[1] : fullToken;
                        callback.onSuccess(token);
                    } else {
                        callback.onError("No se recibió el token");
                    }
                } else {
                    callback.onError("Credenciales incorrectas o error en el servidor");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                callback.onError("Fallo de red: " + t.getMessage());
            }
        });
    }

    public static void register(String name, String email, String password, LoginCallback callback) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("email", email);
        json.addProperty("password", password);

        Call<JsonObject> call = apiService.register(json);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null && response.body().has("token")) {
                    String fullToken = response.body().get("token").getAsString();
                    String token = fullToken.contains("|") ? fullToken.split("\\|")[1] : fullToken;
                    callback.onSuccess(token);
                } else {
                    callback.onError("Error al registrarse: campos inválidos o correo ya usado");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                callback.onError("Fallo de red: " + t.getMessage());
            }
        });
    }

}
