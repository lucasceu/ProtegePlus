package com.uescbd2.protegeplus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OrgaoActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvListaItens: RecyclerView
    private lateinit var tvHeader: TextView

    private lateinit var buttonLogin: LinearLayout
    private lateinit var buttonLogout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Reutiliza o layout activity_lista_itens para não criar XML repetido
        setContentView(R.layout.activity_lista_itens)

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
        rvListaItens = findViewById(R.id.rvListaItens)
        tvHeader = findViewById(R.id.tvHeader)

        tvHeader.text = "Diagnósticos por Sistema"

        // Busca as letras/órgãos
        val listaOrgaos = dbHelper.getOrgaos()

        rvListaItens.layoutManager = LinearLayoutManager(this)
        rvListaItens.adapter = OrgaoAdapter(listaOrgaos) { orgao ->
            // Ao clicar na letra, abre a lista de doenças filtrada
            val intent = Intent(this, ListaItensActivity::class.java)
            intent.putExtra("GRUPO_ID", 7) // 7 = Doenças
            intent.putExtra("GRUPO_NOME", orgao.nome) // Ex: "Olhos"
            intent.putExtra("LETRA_FILTRO", orgao.codigo) // Ex: "F"
            startActivity(intent)
        }
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