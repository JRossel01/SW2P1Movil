package com.grupo12.sw2p1movil.negocio;

import android.content.Context;
import android.content.SharedPreferences;

public class NsessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_TOKEN = "auth_token";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public NsessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Guardar token
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    // Obtener token
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // Eliminar token (logout)
    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    // Verificar si hay token (usuario autenticado)
    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public int getUserId() {
        return sharedPreferences.getInt("user_id", -1); // o el nombre de clave que uses
    }
}
