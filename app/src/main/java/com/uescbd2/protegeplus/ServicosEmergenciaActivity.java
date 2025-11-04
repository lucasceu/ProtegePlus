package com.uescbd2.protegeplus; //

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// Imports necessários para as novas classes
import java.util.List;

public class ServicosEmergenciaActivity extends AppCompatActivity {

    // Referências para o Helper e a RecyclerView
    private DatabaseHelper dbHelper;
    private RecyclerView rvEmergencia;
    private TelefoneAdapter adapter;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicos_emergencia);

        // Instancia o DatabaseHelper (Kotlin) a partir do Java
        dbHelper = new DatabaseHelper(this);

        // Referências da View
        rvEmergencia = findViewById(R.id.rvEmergencia);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        LinearLayout logoutButton = findViewById(R.id.llLogout);

        // Configura o "botão" de logout (lógica igual à anterior)
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ServicosEmergenciaActivity.this, "Logout clicado", Toast.LENGTH_SHORT).show();
                // TODO: Adicionar Intent para a tela de Login
            }
        });

        // --- LÓGICA DA LISTA DINÂMICA ---

        // 1. Buscar os dados do banco
        //    (O dbHelper.getTelefonesUteis() é uma função Kotlin, mas o Java a chama normalmente)
        List<TelefoneUtil> listaDeTelefones = dbHelper.getTelefonesUteis();

        // 2. Verificar se a lista está vazia
        if (listaDeTelefones.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvEmergencia.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvEmergencia.setVisibility(View.VISIBLE);

            // 3. Configurar o Adapter (Kotlin) a partir do Java
            //    Usamos uma expressão lambda para o clique
            adapter = new TelefoneAdapter(this, listaDeTelefones, telefone -> {
                // 4. Ação de clique: Abrir o discador
                if (telefone.getNumero() != null && !telefone.getNumero().isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + telefone.getNumero()));
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Número não disponível", Toast.LENGTH_SHORT).show();
                }
                return null; // Necessário para o lambda do Kotlin (que espera Unit)
            });

            // 5. Ligar o Adapter e o LayoutManager na RecyclerView
            rvEmergencia.setLayoutManager(new LinearLayoutManager(this));
            rvEmergencia.setAdapter(adapter);
        }
    }
}