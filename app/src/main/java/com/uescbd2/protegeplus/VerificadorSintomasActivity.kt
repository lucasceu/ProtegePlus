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

class VerificadorSintomasActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvSintomas: RecyclerView
    private lateinit var adapter: VerificadorSintomasAdapter
    private lateinit var etBusca: EditText

    private val listaSintomasOriginal = mutableListOf<SintomaCheckbox>()
    // Mapa de capítulos: "L" -> "Sistema musculoesquelético"
    private var mapCapitulos: Map<String, String> = emptyMap()

    private val letrasExpandidas = mutableSetOf<String>()

    private lateinit var buttonLogin: LinearLayout
    private lateinit var buttonLogout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verificador_sintomas)

        buttonLogin = findViewById(R.id.buttonLogin)
        buttonLogout = findViewById(R.id.buttonLogout)
        buttonLogin.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
        buttonLogout.setOnClickListener {
            getSharedPreferences("protegeplus_prefs", Context.MODE_PRIVATE).edit().putBoolean("isLoggedIn", false).apply()
            updateButtonVisibility()
            Toast.makeText(this, "Logout realizado", Toast.LENGTH_SHORT).show()
        }

        dbHelper = DatabaseHelper(this)
        rvSintomas = findViewById(R.id.rvSintomasCheckbox)
        etBusca = findViewById(R.id.etBuscarSintoma)

        // 1. Carrega o mapa de capítulos (A -> Geral, L -> Músculo...)
        mapCapitulos = dbHelper.getMapLetrasCiap2()

        // 2. Carrega sintomas
        val dadosBanco = dbHelper.getSintomasPuros()

        // 3. Filtra "Lixo" imediatamente: Só aceita se o código começar com uma letra válida do mapa
        val dadosValidos = dadosBanco.filter { item ->
            val letraCodigo = item.codigo.firstOrNull()?.toString()?.uppercase()
            mapCapitulos.containsKey(letraCodigo)
        }

        listaSintomasOriginal.addAll(
            dadosValidos.map { SintomaCheckbox(it) }
                .sortedBy { it.item.nome } // Ordena alfabeticamente pelo nome dentro do grupo
        )

        // 4. Configura Adapter
        adapter = VerificadorSintomasAdapter(emptyList()) { headerClicado ->
            toggleSection(headerClicado.letter) // Aqui 'letter' será o Nome Completo do Capítulo
        }

        rvSintomas.layoutManager = LinearLayoutManager(this)
        rvSintomas.adapter = adapter

        // 5. Exibe lista inicial
        atualizarListaVisual("")

        // Botões e Listeners
        findViewById<View>(R.id.btnBuscarEnfermidades).setOnClickListener {
            val selecionados = buscarCodigosSelecionadosGlobais()
            if (selecionados.isEmpty()) {
                Toast.makeText(this, "Selecione ao menos um sintoma.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, ResultadosEnfermidadesActivity::class.java)
                intent.putStringArrayListExtra("CODIGOS_SINTOMAS", selecionados)
                startActivity(intent)
            }
        }

        findViewById<View>(R.id.btnLimparSelecao).setOnClickListener {
            listaSintomasOriginal.forEach { it.isChecked = false }
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Seleção limpa", Toast.LENGTH_SHORT).show()
        }

        etBusca.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                atualizarListaVisual(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun toggleSection(tituloCapitulo: String) {
        if (letrasExpandidas.contains(tituloCapitulo)) {
            letrasExpandidas.remove(tituloCapitulo)
        } else {
            letrasExpandidas.add(tituloCapitulo)
        }
        atualizarListaVisual(etBusca.text.toString())
    }

    private fun atualizarListaVisual(query: String) {
        val termo = query.lowercase(Locale.getDefault())
        val isBuscando = termo.isNotEmpty()

        // 1. Filtra itens pelo nome
        val itensFiltrados = if (isBuscando) {
            listaSintomasOriginal.filter {
                it.item.nome?.lowercase(Locale.getDefault())?.contains(termo) == true
            }
        } else {
            listaSintomasOriginal
        }

        // 2. Agrupa pelo CAPÍTULO (baseado na primeira letra do CÓDIGO)
        // Ex: L81 -> Pega 'L' -> Busca no Map -> Agrupa sob "Sistema musculoesquelético"
        val mapaAgrupado = itensFiltrados.groupBy { sintoma ->
            val letraCodigo = sintoma.item.codigo.firstOrNull()?.toString()?.uppercase() ?: ""
            // Se tiver no mapa, usa a descrição. Se não (teoricamente já filtramos), usa "Outros"
            mapCapitulos[letraCodigo] ?: "Outros"
        }.toSortedMap()

        // 3. Constrói a lista visual
        val listaDisplay = mutableListOf<SintomaListItem>()

        for ((nomeCapitulo, listaItens) in mapaAgrupado) {
            // Se buscando, expande tudo. Se não, respeita o clique.
            val isExpandido = isBuscando || letrasExpandidas.contains(nomeCapitulo)

            // Adiciona Cabeçalho com o Nome Bonito (ex: "Olhos")
            listaDisplay.add(SintomaListItem.Separator(nomeCapitulo, isExpandido, listaItens.size))

            if (isExpandido) {
                listaItens.forEach {
                    listaDisplay.add(SintomaListItem.Item(it))
                }
            }
        }

        adapter.updateList(listaDisplay)
    }

    private fun buscarCodigosSelecionadosGlobais(): ArrayList<String> {
        val codigos = ArrayList<String>()
        for (item in listaSintomasOriginal) {
            if (item.isChecked) {
                codigos.add(item.item.codigo)
            }
        }
        return codigos
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