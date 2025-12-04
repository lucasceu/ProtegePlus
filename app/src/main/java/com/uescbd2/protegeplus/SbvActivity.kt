package com.uescbd2.protegeplus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class SbvActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvSbv: RecyclerView
    private lateinit var etBusca: EditText
    private lateinit var adapter: SbvAdapter

    private val listaMestra = mutableListOf<ItemSbv>()
    private val categoriasExpandidas = mutableSetOf<String>()

    private lateinit var buttonLogin: LinearLayout
    private lateinit var buttonLogout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sbv_lista)

        buttonLogin = findViewById(R.id.buttonLogin)
        buttonLogout = findViewById(R.id.buttonLogout)
        configurarLoginLogout()

        dbHelper = DatabaseHelper(this)
        rvSbv = findViewById(R.id.rvSbv)
        etBusca = findViewById(R.id.etBuscarSbv)

        // 1. Carrega dados
        listaMestra.addAll(dbHelper.getItensSBV())

        // 2. Configura Adapter
        adapter = SbvAdapter(emptyList(),
            onHeaderClick = { header -> toggleSection(header.categoria) },
            onItemClick = { item ->
                // Abre Detalhes
                val intent = Intent(this, DetalheSbvActivity::class.java)
                intent.putExtra("ITEM_SBV", item)
                startActivity(intent)
            }
        )

        rvSbv.layoutManager = LinearLayoutManager(this)
        rvSbv.adapter = adapter

        // 3. Exibe lista inicial
        atualizarListaVisual("")

        // 4. Busca
        etBusca.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                atualizarListaVisual(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun toggleSection(categoria: String) {
        if (categoriasExpandidas.contains(categoria)) {
            categoriasExpandidas.remove(categoria)
        } else {
            categoriasExpandidas.add(categoria)
        }
        atualizarListaVisual(etBusca.text.toString())
    }

    private fun atualizarListaVisual(query: String) {
        val termo = query.lowercase(Locale.getDefault())
        val isBuscando = termo.isNotEmpty()

        val itensFiltrados = if (isBuscando) {
            listaMestra.filter { it.enfermidade.lowercase(Locale.getDefault()).contains(termo) }
        } else {
            listaMestra
        }

        // Agrupa por CATEGORIA
        val mapaAgrupado = itensFiltrados.groupBy { it.categoria.trim() }.toSortedMap()

        val listaDisplay = mutableListOf<SbvListItem>()

        for ((cat, itens) in mapaAgrupado) {
            val isExpandido = isBuscando || categoriasExpandidas.contains(cat)

            listaDisplay.add(SbvListItem.Separator(cat, isExpandido))

            if (isExpandido) {
                itens.forEach { listaDisplay.add(SbvListItem.Item(it)) }
            }
        }
        adapter.updateList(listaDisplay)
    }

    private fun configurarLoginLogout() {
        buttonLogin.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
        buttonLogout.setOnClickListener {
            getSharedPreferences("protegeplus_prefs", Context.MODE_PRIVATE).edit().putBoolean("isLoggedIn", false).apply()
            updateButtonVisibility()
            Toast.makeText(this, "Logout realizado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        updateButtonVisibility()
    }

    private fun updateButtonVisibility() {
        val prefs = getSharedPreferences("protegeplus_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("isLoggedIn", false)) {
            buttonLogin.visibility = View.GONE
            buttonLogout.visibility = View.VISIBLE
        } else {
            buttonLogin.visibility = View.VISIBLE
            buttonLogout.visibility = View.GONE
        }
    }
}