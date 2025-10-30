package com.uescbd2.protegeplus

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Encontra os elementos clicáveis
        val cardSintomas = findViewById<CardView>(R.id.cardSintomas)
        val cardEmergencia = findViewById<CardView>(R.id.cardEmergencia)
        val cardTeste = findViewById<CardView>(R.id.cardTeste)
        val buttonLogout = findViewById<LinearLayout>(R.id.buttonLogout) // LinearLayout clicável

        // ... (clique do cardSintomas fica igual) ...
        cardSintomas.setOnClickListener {
            Toast.makeText(this, "Abrir Sintomas & Enfermidades", Toast.LENGTH_SHORT).show()
        }


        // --- INÍCIO DA ALTERAÇÃO ---

        cardEmergencia.setOnClickListener {
            // A linha do Toast abaixo foi substituída:
            // Toast.makeText(this, "Abrir Serviços de Emergência", Toast.LENGTH_SHORT).show()

            // Esta é a nova linha que abre a tela de emergência:
            val intent = Intent(this, ServicosEmergenciaActivity::class.java)
            startActivity(intent)
        }

        // --- FIM DA ALTERAÇÃO ---


        // ... (clique do cardTeste fica igual) ...
        cardTeste.setOnClickListener {
            Toast.makeText(this, "Abrir Teste de Conhecimento", Toast.LENGTH_SHORT).show()
        }

        // ... (clique do buttonLogout fica igual) ...
        buttonLogout.setOnClickListener {
            Toast.makeText(this, "Logout realizado", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}