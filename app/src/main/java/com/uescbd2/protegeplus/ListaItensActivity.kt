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

class ListaItensActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvListaItens: RecyclerView
    private lateinit var adapter: ItemCiapAdapter
    private lateinit var tvHeader: TextView

    private lateinit var buttonLogin: LinearLayout
    private lateinit var buttonLogout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        val grupoId = intent.getIntExtra("GRUPO_ID", -1)
        val grupoNome = intent.getStringExtra("GRUPO_NOME") ?: "Itens"
        // --- NOVO: Recebe a letra filtro (pode ser null se vier de outro lugar) ---
        val letraFiltro = intent.getStringExtra("LETRA_FILTRO")

        dbHelper = DatabaseHelper(this)
        rvListaItens = findViewById(R.id.rvListaItens)
        tvHeader = findViewById(R.id.tvHeader)

        tvHeader.text = grupoNome

        val listaDeItens: List<ItemCiap> = if (grupoId == 1 || grupoId == 7) {
            // --- NOVO: Passa a letra filtro para a query ---
            dbHelper.getItensFromTbCiap(grupoId, letraFiltro)
        } else if (grupoId == 2) {
            dbHelper.getItensFromProcedimentoClinico(grupoId)
        } else {
            emptyList()
        }

        rvListaItens.layoutManager = LinearLayoutManager(this)
        adapter = ItemCiapAdapter(this, listaDeItens) { itemClicado ->
            if (itemClicado.idGrupo == 1 || itemClicado.idGrupo == 7) {
                val intent = Intent(this, DetalheItemActivity::class.java)
                intent.putExtra("ITEM_CODIGO", itemClicado.codigo)
                intent.putExtra("ITEM_NOME", itemClicado.nome)
                startActivity(intent)
            } else if (itemClicado.idGrupo == 2) {
                Toast.makeText(this, "Nenhum detalhe adicional para procedimentos.", Toast.LENGTH_SHORT).show()
            }
        }
        rvListaItens.adapter = adapter
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