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

    // --- MAPA DE ÍCONES (Baseado no seu print) ---
    private val CIAP_ICON_MAP = mapOf(
        "A" to R.drawable.ic_categoria_gerais,
        "B" to R.drawable.ic_categoria_gerais,           // Sangue (sem ícone específico, usando gerais)
        "D" to R.drawable.ic_categoria_gastrointestinais,
        "F" to R.drawable.ic_categoria_oculares,
        "H" to R.drawable.ic_categoria_auditivos,
        "K" to R.drawable.ic_categoria_cardiovasculares,
        "L" to R.drawable.ic_categoria_musculoesqueleticos,
        "N" to R.drawable.ic_categoria_neurologicos,
        "P" to R.drawable.ic_categoria_gerais,           // Psicológico (sem ícone específico)
        "R" to R.drawable.ic_categoria_respiratorios,
        "S" to R.drawable.ic_categoria_dermatologicos,
        "T" to R.drawable.ic_categoria_gerais,           // Endócrino (sem ícone específico)
        "U" to R.drawable.ic_categoria_geniturinarios,
        "W" to R.drawable.ic_categoria_geniturinarios,   // Gravidez (usando geniturinário)
        "X" to R.drawable.ic_categoria_geniturinarios,   // Genital F
        "Y" to R.drawable.ic_categoria_geniturinarios,   // Genital M
        "Z" to R.drawable.ic_categoria_gerais            // Social
    )

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvSintomas: RecyclerView
    private lateinit var adapter: VerificadorSintomasAdapter
    private lateinit var etBusca: EditText

    private val listaSintomasOriginal = mutableListOf<SintomaCheckbox>()
    // Mapa: "L" -> "Sistema Musculoesquelético"
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

        // 1. Carrega Dicionário de Capítulos
        mapCapitulos = dbHelper.getMapLetrasCiap2()

        // 2. Carrega Sintomas do Banco
        val dadosBanco = dbHelper.getSintomasPuros()

        // 3. Filtra Lixo: Só aceita se a letra do código (ex: 'L') existir no mapa
        val dadosValidos = dadosBanco.filter { item ->
            val letraCodigo = item.codigo.firstOrNull()?.toString()?.uppercase()
            mapCapitulos.containsKey(letraCodigo)
        }

        // Ordena por nome do sintoma
        listaSintomasOriginal.addAll(
            dadosValidos.map { SintomaCheckbox(it) }.sortedBy { it.item.nome }
        )

        // 4. Configura Adapter
        adapter = VerificadorSintomasAdapter(emptyList()) { headerClicado ->
            toggleSection(headerClicado.letter)
        }

        rvSintomas.layoutManager = LinearLayoutManager(this)
        rvSintomas.adapter = adapter

        // 5. Gera a lista visual inicial
        atualizarListaVisual("")

        // Listeners
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

        // 1. Filtra itens
        val itensFiltrados = if (isBuscando) {
            listaSintomasOriginal.filter { it.item.nome?.lowercase(Locale.getDefault())?.contains(termo) == true }
        } else {
            listaSintomasOriginal
        }

        // 2. Agrupa por NOME DO CAPÍTULO (Não mais só pela letra)
        val mapaAgrupado = itensFiltrados.groupBy { sintoma ->
            val letraCodigo = sintoma.item.codigo.firstOrNull()?.toString()?.uppercase() ?: ""
            mapCapitulos[letraCodigo] ?: "Outros"
        }.toSortedMap()

        // 3. Monta a lista visual
        val listaDisplay = mutableListOf<SintomaListItem>()

        for ((nomeCapitulo, listaItens) in mapaAgrupado) {
            // Se buscando, abre tudo. Se não, respeita o clique.
            val isExpandido = isBuscando || letrasExpandidas.contains(nomeCapitulo)

            // Descobre qual é a letra desse grupo (pega do primeiro item) para achar o ícone
            val letraCodigo = listaItens.firstOrNull()?.item?.codigo?.firstOrNull()?.toString()?.uppercase() ?: ""

            // Pega o ícone do mapa, ou usa o genérico
            val iconId = CIAP_ICON_MAP[letraCodigo] ?: R.drawable.ic_categoria_gerais

            // Cria o separador visual
            listaDisplay.add(SintomaListItem.Separator(nomeCapitulo, isExpandido, iconId))

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