package com.example.despachoapp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import android.content.Intent;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPass;
    private FirebaseAuth auth;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);
        etEmail = findViewById(R.id.etEmail);
        etPass  = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        auth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPass.getText().toString().trim();
            if(email.isEmpty() || pass.isEmpty()){ toast("Complete los campos"); return; }
            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(auth.getUid()).child("email").setValue(email);
                            toast("Usuario creado");
                        } else toast(error(task.getException()));
                    });
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPass.getText().toString().trim();
            auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(auth.getUid()).child("lastLogin").setValue(ServerValue.TIMESTAMP);
                            startActivity(new Intent(this, MenuActivity.class));
                            finish();
                        } else toast(error(task.getException()));
                    });
        });
    }

    private void toast(String m){ Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
    private String error(Exception e){ return e!=null? e.getMessage() : "Error"; }
}
