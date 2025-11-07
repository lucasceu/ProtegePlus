package com.uescbd2.protegeplus

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados_enfermidades)

        // Recebe os códigos da tela anterior
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

        // 1. PASSO 1: A "REDE DE PESCA" (Busca no DB)
        // (Isso pode ser lento, o ideal no futuro seria usar AsyncTask ou Coroutines)
        val listaSuspeitos = dbHelper.getEnfermidadesPorSintomas(codigosSintomas)

        // 2. PASSO 2: A LÓGICA DO RANKING (em Kotlin)
        val listaRanking = mutableListOf<EnfermidadeResultado>()

        for (item in listaSuspeitos) {
            val resultado = EnfermidadeResultado(item, 0)

            // "Entrevista" o item: conta quantos sintomas ele tem
            for (codigo in codigosSintomas) {
                val codigoFormatado = " ${codigo}" // Busca por " A03"

                val achouEmInclusos = item.textoSintomasInclusos?.contains(codigoFormatado) ?: false
                val achouEmOutros = item.textoOutrosSintomas?.contains(codigoFormatado) ?: false

                if (achouEmInclusos || achouEmOutros) {
                    resultado.pontuacao++
                }
            }
            listaRanking.add(resultado)
        }

        // 3. PASSO 3: ORDENAR (Mostrar os melhores primeiro)
        // Ordena pela pontuação, do maior para o menor
        listaRanking.sortByDescending { it.pontuacao }


        // 4. PASSO 4: EXIBIR
        if (listaRanking.isEmpty()) {
            tvEmptyResultados.visibility = View.VISIBLE
            rvResultados.visibility = View.GONE
        } else {
            tvEmptyResultados.visibility = View.GONE
            rvResultados.visibility = View.VISIBLE

            adapter = ResultadoEnfermidadeAdapter(listaRanking, codigosSintomas.size) { enfermidadeClicada ->
                // Ação de clique: Abre a Tela de Detalhes (Tela 3 original)
                val intent = Intent(this, DetalheItemActivity::class.java)
                intent.putExtra("ITEM_CODIGO", enfermidadeClicada.codigo)
                intent.putExtra("ITEM_NOME", enfermidadeClicada.nome)
                startActivity(intent)
            }
            rvResultados.adapter = adapter
        }

        // Configura o logout
        findViewById<LinearLayout>(R.id.llLogout).setOnClickListener {
            Toast.makeText(this, "Logout clicado", Toast.LENGTH_SHORT).show()
        }
    }
}