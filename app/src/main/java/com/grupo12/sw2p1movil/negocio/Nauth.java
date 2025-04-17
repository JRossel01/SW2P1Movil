package com.grupo12.sw2p1movil.negocio;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.grupo12.sw2p1movil.datos.Dauth;
import com.grupo12.sw2p1movil.presentacion.MainActivity;

public class Nauth {
    public static void login(String email, String password, Context context) {
        Dauth.login(email, password, new Dauth.LoginCallback() {
            @Override
            public void onSuccess(String token) {
                // Guardar token
                NsessionManager session = new NsessionManager(context);
                session.saveToken(token);

                // Mostrar mensaje
                Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                // Ir a pantalla principal
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(context, "Error: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void register(String name, String email, String password, Context context) {
        Dauth.register(name, email, password, new Dauth.LoginCallback() {
            @Override
            public void onSuccess(String token) {
                NsessionManager session = new NsessionManager(context);
                session.saveToken(token);

                Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(context, "Error: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
