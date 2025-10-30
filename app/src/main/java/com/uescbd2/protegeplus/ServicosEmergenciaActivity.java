package com.uescbd2.protegeplus;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ServicosEmergenciaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicos_emergencia);

        // Configura o "botão" de logout
        LinearLayout logoutButton = findViewById(R.id.llLogout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Adicione sua lógica de logout aqui
                Toast.makeText(ServicosEmergenciaActivity.this, "Logout clicado", Toast.LENGTH_SHORT).show();
                // Ex: Intent para a tela de Login
            }
        });

        // O card de exemplo já está no layout, não precisa de referência aqui para o exemplo estático.
        // Se fosse dinâmico (com RecyclerView), precisaríamos de um Adapter e de dados.
    }
}