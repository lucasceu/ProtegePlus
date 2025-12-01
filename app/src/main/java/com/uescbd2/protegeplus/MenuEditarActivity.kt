package com.uescbd2.protegeplus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MenuEditarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_editar)

        val btnPessoas = findViewById<Button>(R.id.btnGerenciarPessoas)
        val btnTelefones = findViewById<Button>(R.id.btnGerenciarTelefones)
        val btnQuestionarios = findViewById<Button>(R.id.btnGerenciarQuestionarios)

        btnPessoas.setOnClickListener {
            startActivity(Intent(this, ListaPessoasActivity::class.java))
        }

        btnTelefones.setOnClickListener {
            // Mudou de NovoTelefoneUtilActivity para ListaTelefonesActivity
            startActivity(Intent(this, ListaTelefonesActivity::class.java))
        }

        btnQuestionarios.setOnClickListener {
            startActivity(Intent(this, ListaQuestoesActivity::class.java))
        }
    }
}