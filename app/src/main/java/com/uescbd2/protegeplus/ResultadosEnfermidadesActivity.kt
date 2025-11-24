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

        // Setup Toolbar
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonLogout = findViewById(R.id.buttonLogout)
        buttonLogin.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
        buttonLogout.setOnClickListener {
            getSharedPreferences("protegeplus_prefs", Context.MODE_PRIVATE).edit().putBoolean("isLoggedIn", false).apply()
            updateButtonVisibility()
        }

        rvResultados = findViewById(R.id.rvResultadosEnfermidades)
        tvEmptyResultados = findViewById(R.id.tvEmptyResultados)
        dbHelper = DatabaseHelper(this)

        // 1. Recebe os sintomas selecionados
        val codigosSintomas = intent.getStringArrayListExtra("CODIGOS_SINTOMAS") ?: arrayListOf()

        if (codigosSintomas.isEmpty()) {
            tvEmptyResultados.visibility = View.VISIBLE
            tvEmptyResultados.text = "Nenhum sintoma recebido para análise."
            return
        }

        // 2. Busca Ampla (Rede de Pesca no SQL)
        val candidatos = dbHelper.getEnfermidadesPorSintomas(codigosSintomas)

        // 3. Algoritmo de Ranking (Kotlin)
        val listaRankeada = calcularRanking(candidatos, codigosSintomas)

        // 4. Exibe
        if (listaRankeada.isEmpty()) {
            tvEmptyResultados.visibility = View.VISIBLE
            rvResultados.visibility = View.GONE
        } else {
            tvEmptyResultados.visibility = View.GONE
            rvResultados.visibility = View.VISIBLE

            rvResultados.layoutManager = LinearLayoutManager(this)
            // Passamos o total de sintomas selecionados para mostrar "2 de 5" na tela
            adapter = ResultadoEnfermidadeAdapter(listaRankeada, codigosSintomas.size) { resultado ->
                // Ao clicar, abre detalhes
                val intent = Intent(this, DetalheItemActivity::class.java)
                intent.putExtra("ITEM_CODIGO", resultado.enfermidade.codigo)
                intent.putExtra("ITEM_NOME", resultado.enfermidade.nome)
                startActivity(intent)
            }
            rvResultados.adapter = adapter
        }
    }

    private fun calcularRanking(
        candidatos: List<ItemCiap>,
        codigosSelecionados: List<String>
    ): List<EnfermidadeResultado> {
        val resultados = mutableListOf<EnfermidadeResultado>()

        for (doenca in candidatos) {
            var pontos = 0
            val matches = mutableListOf<String>()

            // Prepara texto para busca (lowercase para ignorar maiúsculas/minúsculas)
            val textoBusca = "${doenca.textoSintomasInclusos} ${doenca.textoOutrosSintomas}".lowercase()
            val codigoDoenca = doenca.codigo.lowercase()

            for (sintomaCod in codigosSelecionados) {
                val sCod = sintomaCod.lowercase()

                // Critérios de Pontuação:
                // 1. O código do sintoma é IGUAL ao código da doença? (Peso forte)
                // 2. O código do sintoma aparece no texto descritivo?
                if (codigoDoenca == sCod || textoBusca.contains(sCod)) {
                    pontos++
                    matches.add(sintomaCod)
                }
            }

            if (pontos > 0) {
                resultados.add(EnfermidadeResultado(doenca, pontos, matches))
            }
        }

        // Ordena: Quem tem mais pontos aparece primeiro
        return resultados.sortedByDescending { it.pontuacao }
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