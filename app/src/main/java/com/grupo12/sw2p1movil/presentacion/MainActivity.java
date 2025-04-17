package com.grupo12.sw2p1movil.presentacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.grupo12.sw2p1movil.R;
import com.grupo12.sw2p1movil.negocio.NsessionManager;

public class MainActivity extends AppCompatActivity {

    private Button btnIrBotones, btnIrProcesos, btnLogout, btnIrRegistrarDispositivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnIrBotones = findViewById(R.id.btnIrBotones);
        btnIrProcesos = findViewById(R.id.btnIrProcesos);
        btnLogout = findViewById(R.id.btnLogout);


        btnIrBotones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirigir a la pantalla de botones
                Intent intent = new Intent(MainActivity.this, ButtonListActivity.class);
                startActivity(intent);
                //Toast.makeText(MainActivity.this, "Ir a Botones (próximamente)", Toast.LENGTH_SHORT).show();
            }
        });

        btnIrProcesos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirigir a la pantalla de dispositivos → luego procesos
                Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivity(intent);
                //Toast.makeText(MainActivity.this, "Ir a Procesos (próximamente)", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cerrar sesión y volver al login
                NsessionManager session = new NsessionManager(MainActivity.this);
                session.clearSession();

                Intent intent = new Intent(MainActivity.this, PloginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        btnIrRegistrarDispositivo = findViewById(R.id.btnIrRegistrarDispositivo);
        btnIrRegistrarDispositivo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterDeviceActivity.class);
            startActivity(intent);
        });

    }
}