package com.uescbd2.protegeplus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class HomeActivity : AppCompatActivity() {

    private lateinit var buttonLogin: LinearLayout
    private lateinit var buttonLogout: LinearLayout
    private lateinit var btnEditarDados: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        buttonLogin = findViewById(R.id.buttonLogin)
        buttonLogout = findViewById(R.id.buttonLogout)
        btnEditarDados = findViewById(R.id.btnEditarDados)

        // Botão Editar Dados
        btnEditarDados.setOnClickListener {
            val intent = Intent(this, MenuEditarActivity::class.java)
            startActivity(intent)
        }

        buttonLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        buttonLogout.setOnClickListener {
            val sharedPreferences = getSharedPreferences("protegeplus_prefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean("isLoggedIn", false)
                apply()
            }
            Toast.makeText(this, "Logout realizado com sucesso", Toast.LENGTH_SHORT).show()
            updateButtonVisibility()
        }

        // --- Lógica de Navegação dos Cards ---
        val cardSintomas = findViewById<CardView>(R.id.cardSintomas)
        val cardEmergencia = findViewById<CardView>(R.id.cardEmergencia)
        val cardTeste = findViewById<CardView>(R.id.cardTeste)

        cardSintomas.setOnClickListener {
            val intent = Intent(this, GrupoCiapActivity::class.java)
            startActivity(intent)
        }

        cardEmergencia.setOnClickListener {
            val intent = Intent(this, ServicosEmergenciaActivity::class.java)
            startActivity(intent)
        }

        cardTeste.setOnClickListener {
            // CORREÇÃO: Abre a tela do Quiz
            val intent = Intent(this, TesteConhecimentoActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateButtonVisibility()
    }

    private fun updateButtonVisibility() {
        val sharedPreferences = getSharedPreferences("protegeplus_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            buttonLogin.visibility = View.GONE
            buttonLogout.visibility = View.VISIBLE
        } else {
            buttonLogin.visibility = View.VISIBLE
            buttonLogout.visibility = View.GONE
        }
    }
}