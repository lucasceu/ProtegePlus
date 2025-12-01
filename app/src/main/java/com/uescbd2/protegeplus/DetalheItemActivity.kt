package com.uescbd2.protegeplus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DetalheItemActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    // Views de Texto
    private lateinit var tvItemNomeTitulo: TextView
    private lateinit var tvSintomasInclusos: TextView
    private lateinit var tvSintomasExclusao: TextView
    private lateinit var tvOutrosSintomas: TextView
    private lateinit var tvEmptySubSintomas: TextView

    // Cards (Para esconder o bloco inteiro se estiver vazio)
    private lateinit var cardInclusos: CardView
    private lateinit var cardExclusao: CardView
    private lateinit var cardOutros: CardView
    private lateinit var cardSubSintomas: CardView

    // Lista
    private lateinit var rvSubSintomas: RecyclerView

    // Botões de Login/Logout (Padrão das outras telas)
    private lateinit var buttonLogin: LinearLayout
    private lateinit var buttonLogout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_item)

        // --- Configuração da Toolbar ---
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

        // --- Recebendo Dados ---
        val itemCodigo = intent.getStringExtra("ITEM_CODIGO")
        // O nome já vem do Intent, mas se quiser garantir, pode buscar do banco também.
        val itemNome = intent.getStringExtra("ITEM_NOME") ?: "Detalhes"

        if (itemCodigo == null) {
            Toast.makeText(this, "Erro: Código do item não encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        dbHelper = DatabaseHelper(this)
        inicializarViews()

        // Título Limpo (Sem código entre parênteses, conforme pedido)
        tvItemNomeTitulo.text = itemNome

        // --- Buscando Dados ---
        val detalhes = dbHelper.getDetalhesItemCiap(itemCodigo)
        preencherDetalhes(detalhes)

        val listaSubSintomas = dbHelper.getSubSintomas(itemCodigo)
        preencherSubSintomas(listaSubSintomas)
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

    private fun inicializarViews() {
        tvItemNomeTitulo = findViewById(R.id.tvItemNomeTitulo)

        // Cards
        cardInclusos = findViewById(R.id.cardInclusos)
        cardExclusao = findViewById(R.id.cardExclusao)
        cardOutros = findViewById(R.id.cardOutros)
        cardSubSintomas = findViewById(R.id.cardSubSintomas)

        // Conteúdo dos Cards
        tvSintomasInclusos = findViewById(R.id.tvSintomasInclusos)
        tvSintomasExclusao = findViewById(R.id.tvSintomasExclusao)
        tvOutrosSintomas = findViewById(R.id.tvOutrosSintomas)

        // Lista
        rvSubSintomas = findViewById(R.id.rvSubSintomas)
        tvEmptySubSintomas = findViewById(R.id.tvEmptySubSintomas)
    }

    private fun preencherDetalhes(detalhe: DetalheCiap?) {
        if (detalhe == null) {
            // Se não achou nada, esconde tudo para não ficar tela em branco
            cardInclusos.visibility = View.GONE
            cardExclusao.visibility = View.GONE
            cardOutros.visibility = View.GONE
            return
        }

        // Lógica inteligente: Só mostra o Card se o texto não for vazio
        exibirOuEsconderCard(cardInclusos, tvSintomasInclusos, detalhe.sintomasInclusos)
        exibirOuEsconderCard(cardExclusao, tvSintomasExclusao, detalhe.sintomasExclusao)
        exibirOuEsconderCard(cardOutros, tvOutrosSintomas, detalhe.outrosSintomas)

        // Nota: Removemos CID-10 propositalmente conforme solicitado
    }

    private fun preencherSubSintomas(lista: List<SubSintoma>) {
        if (lista.isEmpty()) {
            // Se não tem sub-sintomas, esconde o card inteiro
            cardSubSintomas.visibility = View.GONE
        } else {
            cardSubSintomas.visibility = View.VISIBLE
            tvEmptySubSintomas.visibility = View.GONE

            rvSubSintomas.visibility = View.VISIBLE
            rvSubSintomas.layoutManager = LinearLayoutManager(this)
            rvSubSintomas.adapter = SubSintomaAdapter(lista)
            // Desabilita scroll interno do RV para fluir com a tela
            rvSubSintomas.isNestedScrollingEnabled = false
        }
    }

    private fun exibirOuEsconderCard(card: View, campoTexto: TextView, valor: String?) {
        if (valor.isNullOrBlank() || valor.equals("null", ignoreCase = true)) {
            card.visibility = View.GONE
        } else {
            card.visibility = View.VISIBLE
            campoTexto.text = valor
        }
    }
}