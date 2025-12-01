package com.uescbd2.protegeplus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import kotlin.Unit; // Importante para o retorno do lambda Kotlin

public class ServicosEmergenciaActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView rvEmergencia;
    private TelefoneAdapter adapter;
    private TextView tvEmptyState;

    private LinearLayout buttonLogin;
    private LinearLayout buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicos_emergencia);

        buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogout = findViewById(R.id.buttonLogout);

        buttonLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ServicosEmergenciaActivity.this, MainActivity.class);
            startActivity(intent);
        });

        buttonLogout.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("protegeplus_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.apply();

            Toast.makeText(ServicosEmergenciaActivity.this, "Logout realizado com sucesso", Toast.LENGTH_SHORT).show();
            updateButtonVisibility();
        });

        dbHelper = new DatabaseHelper(this);
        rvEmergencia = findViewById(R.id.rvEmergencia);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Chama o método corrigido (que agora traz todos os telefones)
        List<TelefoneUtil> listaDeTelefones = dbHelper.getTelefonesUteis();

        if (listaDeTelefones.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvEmergencia.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvEmergencia.setVisibility(View.VISIBLE);

            // ADAPTADO PARA O NOVO CONSTRUTOR DO ADAPTER KOTLIN
            adapter = new TelefoneAdapter(this, listaDeTelefones,
                    // 1. Clique Simples: Ligar
                    telefone -> {
                        if (telefone.getNumero() != null && !telefone.getNumero().isEmpty()) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + telefone.getNumero()));
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Número não disponível", Toast.LENGTH_SHORT).show();
                        }
                        return Unit.INSTANCE; // Retorno obrigatório para Kotlin
                    },
                    // 2. Clique Longo: Vazio (Não faz nada na tela pública)
                    telefone -> {
                        return Unit.INSTANCE;
                    }
            );

            rvEmergencia.setLayoutManager(new LinearLayoutManager(this));
            rvEmergencia.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateButtonVisibility();
        // Recarregar lista aqui se quiser que atualize ao voltar
    }

    private void updateButtonVisibility() {
        SharedPreferences sharedPreferences = getSharedPreferences("protegeplus_prefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            buttonLogin.setVisibility(View.GONE);
            buttonLogout.setVisibility(View.VISIBLE);
        } else {
            buttonLogin.setVisibility(View.VISIBLE);
            buttonLogout.setVisibility(View.GONE);
        }
    }
}