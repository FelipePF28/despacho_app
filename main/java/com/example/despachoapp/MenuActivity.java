package com.example.despachoapp;

import android.os.Bundle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import android.app.NotificationChannel;
import android.app.NotificationManager;

import java.util.HashMap;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {
    private TextView tvStatus, tvResultado;
    private EditText etTotal, etDist;
    private FusedLocationProviderClient fused;
    private DatabaseReference db;
    private FirebaseAuth auth;
    private ValueEventListener tempListener;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_menu);

        tvStatus = findViewById(R.id.tvStatus);
        tvResultado = findViewById(R.id.tvResultado);
        etTotal = findViewById(R.id.etTotal);
        etDist  = findViewById(R.id.etDist);
        Button btnUbic = findViewById(R.id.btnUbicacion);
        Button btnCalc = findViewById(R.id.btnCalcular);

        fused = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        createNotificationChannel(); // para Oreo+

        btnUbic.setOnClickListener(v -> saveLocation());
        btnCalc.setOnClickListener(v -> {
            try {
                long total = Long.parseLong(etTotal.getText().toString());
                double dist = Double.parseDouble(etDist.getText().toString());
                long costo = calcularDespacho(total, dist);
                tvResultado.setText("Costo: $" + costo);
                // Guardar un ejemplo de orden (opcional)
                String orderId = db.child("orders").push().getKey();
                Map<String,Object> data = new HashMap<>();
                data.put("userId", auth.getUid());
                data.put("total", total);
                data.put("distanceKm", dist);
                data.put("shippingCost", costo);
                db.child("orders").child(orderId).setValue(data);
            } catch (Exception e) { Toast.makeText(this,"Datos inválidos",Toast.LENGTH_SHORT).show(); }
        });

        // Escuchar temperatura del camión (simulada)
        String truckId = "truck-001";
        tempListener = db.child("fleet").child(truckId).child("freezerTemp")
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        if(snap.exists()){
                            double temp = snap.getValue(Double.class);
                            tvStatus.setText("Temp camión: " + temp + "°C");
                            if(temp > -10.0) notifyAlarm("¡Alerta cadena de frío!",
                                    "Temperatura del camión supera el límite: " + temp + "°C");
                        }
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });
    }

    private long calcularDespacho(long total, double km){
        if(total >= 50000 && km <= 20) return 0;
        if(total >= 25000 && total <= 49999) return Math.round(150 * km);
        return Math.round(300 * km);
    }

    private void saveLocation(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED))
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }
        fused.getLastLocation().addOnSuccessListener(loc -> {
            if(loc!=null){
                String uid = auth.getUid();
                String key = db.child("users").child(uid).child("locations").push().getKey();
                Map<String, Object> m = new HashMap<>();
                m.put("lat", loc.getLatitude());
                m.put("lng", loc.getLongitude());
                m.put("provider", loc.getProvider() != null ? loc.getProvider() : "unknown");
                db.child("users").child(uid).child("locations").child(key).setValue(m);
                Toast.makeText(this,"Ubicación guardada",Toast.LENGTH_SHORT).show();
            } else {
                tvStatus.setText("Sin ubicación disponible. Intente nuevamente.");
            }
        });
    }

    // --- Notificación local para Oreo+ ---
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel ch = new NotificationChannel(
                    "ALERTAS", "Alertas de Cadena de Frío", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(ch);
        }
    }
    private void notifyAlarm(String title, String body){
        NotificationCompat.Builder nb = new NotificationCompat.Builder(this, "ALERTAS")
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat.from(this).notify(999, nb.build());
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if(tempListener!=null) db.removeEventListener(tempListener);
    }
}
