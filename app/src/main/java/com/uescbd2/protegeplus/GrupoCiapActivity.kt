package com.uescbd2.protegeplus

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GrupoCiapActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvGrupoCiap: RecyclerView
    private lateinit var adapter: GrupoCiapAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grupo_ciap)

        dbHelper = DatabaseHelper(this)
        rvGrupoCiap = findViewById(R.id.rvGrupoCiap)

        // 1. Buscar os dados do banco
        val listaDeGrupos = dbHelper.getGruposCiap()

        // 2. Configurar o Adapter e a RecyclerView
        rvGrupoCiap.layoutManager = LinearLayoutManager(this)
        adapter = GrupoCiapAdapter(this, listaDeGrupos) { grupoClicado ->

            // Se o usuário clicar em "Sintomas e queixas" (ID 1)
            if (grupoClicado.id == 1) {
                // Abre o Verificador de Sintomas (Plano B)
                val intent = Intent(this, VerificadorSintomasActivity::class.java)
                startActivity(intent)
            }
            // Se o usuário clicar em "Procedimentos" (ID 2) ou "Doenças" (ID 7)
            else if (grupoClicado.id == 2 || grupoClicado.id == 7) {
                // Abre o Dicionário (Plano A)
                val intent = Intent(this, ListaItensActivity::class.java)
                intent.putExtra("GRUPO_ID", grupoClicado.id)
                intent.putExtra("GRUPO_NOME", grupoClicado.componente)
                startActivity(intent)
            }
        }
        rvGrupoCiap.adapter = adapter

        // Configura o logout (igual às outras telas)
        findViewById<LinearLayout>(R.id.llLogout).setOnClickListener {
            // TODO: Adicionar lógica real de logout (voltar pra MainActivity)
            Toast.makeText(this, "Logout clicado", Toast.LENGTH_SHORT).show()
        }
    }
}