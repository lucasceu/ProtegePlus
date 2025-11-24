package com.uescbd2.protegeplus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GrupoCiapActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvGrupoCiap: RecyclerView
    private lateinit var adapter: GrupoCiapAdapter

    private lateinit var buttonLogin: LinearLayout
    private lateinit var buttonLogout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grupo_ciap)

        buttonLogin = findViewById(R.id.buttonLogin)
        buttonLogout = findViewById(R.id.buttonLogout)

        buttonLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        buttonLogout.setOnClickListener {
            val prefs = getSharedPreferences("protegeplus_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("isLoggedIn", false).apply()
            Toast.makeText(this, "Logout realizado", Toast.LENGTH_SHORT).show()
            updateButtonVisibility()
        }

        dbHelper = DatabaseHelper(this)
        rvGrupoCiap = findViewById(R.id.rvGrupoCiap)

        val listaDeGrupos = dbHelper.getGruposCiap()

        rvGrupoCiap.layoutManager = LinearLayoutManager(this)
        adapter = GrupoCiapAdapter(this, listaDeGrupos) { grupoClicado ->
            if (grupoClicado.id == 1) {
                // Grupo 1: Sintomas -> Verificador
                val intent = Intent(this, VerificadorSintomasActivity::class.java)
                startActivity(intent)
            } else if (grupoClicado.id == 7) {
                // --- MUDANÇA: Grupo 7: Diagnósticos -> Tela de Órgãos ---
                val intent = Intent(this, OrgaoActivity::class.java)
                startActivity(intent)
            } else if (grupoClicado.id == 2) {
                // Grupo 2: Procedimentos -> Lista Direta
                val intent = Intent(this, ListaItensActivity::class.java)
                intent.putExtra("GRUPO_ID", grupoClicado.id)
                intent.putExtra("GRUPO_NOME", grupoClicado.componente)
                startActivity(intent)
            }
        }
        rvGrupoCiap.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        updateButtonVisibility()
    }

    private fun updateButtonVisibility() {
        val prefs = getSharedPreferences("protegeplus_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            buttonLogin.visibility = View.GONE
            buttonLogout.visibility = View.VISIBLE
        } else {
            buttonLogin.visibility = View.VISIBLE
            buttonLogout.visibility = View.GONE
        }
    }
}