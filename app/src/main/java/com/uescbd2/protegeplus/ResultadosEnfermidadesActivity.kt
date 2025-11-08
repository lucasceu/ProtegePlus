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

class ResultadosEnfermidadesActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvResultados: RecyclerView
    private lateinit var adapter: ResultadoEnfermidadeAdapter
    private lateinit var tvEmptyResultados: TextView

    private lateinit var buttonLogin: LinearLayout
    private lateinit var buttonLogout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados_enfermidades)

        buttonLogin = findViewById(R.id.buttonLogin)
        buttonLogout = findViewById(R.id.buttonLogout)

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

        val codigosSintomas = intent.getStringArrayListExtra("CODIGOS_SINTOMAS")
        if (codigosSintomas == null || codigosSintomas.isEmpty()) {
            Toast.makeText(this, "Erro: Nenhum sintoma selecionado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        dbHelper = DatabaseHelper(this)
        rvResultados = findViewById(R.id.rvResultadosEnfermidades)
        tvEmptyResultados = findViewById(R.id.tvEmptyResultados)
        rvResultados.layoutManager = LinearLayoutManager(this)

        val listaSuspeitos = dbHelper.getEnfermidadesPorSintomas(codigosSintomas)
        val listaRanking = mutableListOf<EnfermidadeResultado>()

        for (item in listaSuspeitos) {
            val resultado = EnfermidadeResultado(item, 0)
            for (codigo in codigosSintomas) {
                val codigoFormatado = " ${codigo}"
                val achouEmInclusos = item.textoSintomasInclusos?.contains(codigoFormatado) ?: false
                val achouEmOutros = item.textoOutrosSintomas?.contains(codigoFormatado) ?: false
                if (achouEmInclusos || achouEmOutros) {
                    resultado.pontuacao++
                }
            }
            listaRanking.add(resultado)
        }

        listaRanking.sortByDescending { it.pontuacao }

        if (listaRanking.isEmpty()) {
            tvEmptyResultados.visibility = View.VISIBLE
            rvResultados.visibility = View.GONE
        } else {
            tvEmptyResultados.visibility = View.GONE
            rvResultados.visibility = View.VISIBLE
            adapter = ResultadoEnfermidadeAdapter(listaRanking, codigosSintomas.size) { enfermidadeClicada ->
                val intent = Intent(this, DetalheItemActivity::class.java)
                intent.putExtra("ITEM_CODIGO", enfermidadeClicada.codigo)
                intent.putExtra("ITEM_NOME", enfermidadeClicada.nome)
                startActivity(intent)
            }
            rvResultados.adapter = adapter
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