package com.uescbd2.protegeplus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import kotlin.Unit;

public class ServicosEmergenciaActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView rvEmergencia;
    private TelefoneAdapter adapter;
    private TextView tvEmptyState;
    private EditText etPesquisar;

    private LinearLayout buttonLogin;
    private LinearLayout buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicos_emergencia);

        // --- Inicialização dos Botões de Login/Logout ---
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

        // --- Inicialização da Lista e Banco ---
        dbHelper = new DatabaseHelper(this);
        rvEmergencia = findViewById(R.id.rvEmergencia);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        etPesquisar = findViewById(R.id.etPesquisar);

        // Busca todos os telefones (sem filtro de ID, pois a coluna foi removida)
        List<TelefoneUtil> listaDeTelefones = dbHelper.getTelefonesUteis();

        if (listaDeTelefones.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvEmergencia.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvEmergencia.setVisibility(View.VISIBLE);

            // Instancia o Adapter Kotlin
            adapter = new TelefoneAdapter(this, listaDeTelefones,
                    // 1. Clique Simples: Ligar para o número
                    telefone -> {
                        if (telefone.getNumero() != null && !telefone.getNumero().isEmpty()) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + telefone.getNumero()));
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Número não disponível", Toast.LENGTH_SHORT).show();
                        }
                        return Unit.INSTANCE; // Retorno obrigatório para interoperabilidade com Kotlin
                    },
                    // 2. Clique Longo: Não faz nada nesta tela pública
                    telefone -> {
                        return Unit.INSTANCE;
                    }
            );

            rvEmergencia.setLayoutManager(new LinearLayoutManager(this));
            rvEmergencia.setAdapter(adapter);

            // --- Lógica de Pesquisa (Filtro) ---
            etPesquisar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        adapter.filtrar(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateButtonVisibility();
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