package com.uescbd2.protegeplus

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DetalheItemActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvSubSintomas: RecyclerView
    private lateinit var subSintomaAdapter: SubSintomaAdapter

    // Referências para os TextViews de detalhes
    private lateinit var tvItemNomeTitulo: TextView
    private lateinit var tvSubSintomasLabel: TextView
    private lateinit var tvEmptySubSintomas: TextView
    private lateinit var tvSintomasInclusosLabel: TextView
    private lateinit var tvSintomasInclusos: TextView
    private lateinit var tvSintomasExclusaoLabel: TextView
    private lateinit var tvSintomasExclusao: TextView
    private lateinit var tvOutrosSintomasLabel: TextView
    private lateinit var tvOutrosSintomas: TextView
    private lateinit var tvCid10Label: TextView
    private lateinit var tvCid10: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_item)

        // Recebe os dados da Tela 2 (ListaItensActivity)
        val itemCodigo = intent.getStringExtra("ITEM_CODIGO")
        val itemNome = intent.getStringExtra("ITEM_NOME") ?: "Detalhes"

        if (itemCodigo == null) {
            Toast.makeText(this, "Erro: Código do item não encontrado.", Toast.LENGTH_LONG).show()
            finish() // Fecha a activity se não houver código
            return
        }

        dbHelper = DatabaseHelper(this)

        // Inicializa todas as Views
        inicializarViews()

        // Define o título da tela
        tvItemNomeTitulo.text = "$itemNome ($itemCodigo)"

        // 1. Buscar os Detalhes da tb_ciap
        val detalhes = dbHelper.getDetalhesItemCiap(itemCodigo)
        preencherDetalhes(detalhes)

        // 2. Buscar a Lista de Sub-Sintomas da tb_sintomas
        val listaSubSintomas = dbHelper.getSubSintomas(itemCodigo)
        preencherSubSintomas(listaSubSintomas)

        // Configura o logout
        findViewById<LinearLayout>(R.id.llLogout).setOnClickListener {
            Toast.makeText(this, "Logout clicado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun inicializarViews() {
        tvItemNomeTitulo = findViewById(R.id.tvItemNomeTitulo)
        rvSubSintomas = findViewById(R.id.rvSubSintomas)
        tvSubSintomasLabel = findViewById(R.id.tvSubSintomasLabel)
        tvEmptySubSintomas = findViewById(R.id.tvEmptySubSintomas)

        tvSintomasInclusosLabel = findViewById(R.id.tvSintomasInclusosLabel)
        tvSintomasInclusos = findViewById(R.id.tvSintomasInclusos)
        tvSintomasExclusaoLabel = findViewById(R.id.tvSintomasExclusaoLabel)
        tvSintomasExclusao = findViewById(R.id.tvSintomasExclusao)
        tvOutrosSintomasLabel = findViewById(R.id.tvOutrosSintomasLabel)
        tvOutrosSintomas = findViewById(R.id.tvOutrosSintomas)
        tvCid10Label = findViewById(R.id.tvCid10Label)
        tvCid10 = findViewById(R.id.tvCid10)
    }

    // Função auxiliar para preencher os dados da tb_ciap
    private fun preencherDetalhes(detalhe: DetalheCiap?) {
        if (detalhe == null) {
            // Esconde os campos se não houver detalhes
            tvSintomasInclusosLabel.visibility = View.GONE
            tvSintomasInclusos.visibility = View.GONE
            tvSintomasExclusaoLabel.visibility = View.GONE
            tvSintomasExclusao.visibility = View.GONE
            tvOutrosSintomasLabel.visibility = View.GONE
            tvOutrosSintomas.visibility = View.GONE
            tvCid10Label.visibility = View.GONE
            tvCid10.visibility = View.GONE
            return
        }

        // Função "inteligente" para exibir um campo ou escondê-lo se for nulo/vazio
        exibirOuEsconderCampo(tvSintomasInclusosLabel, tvSintomasInclusos, detalhe.sintomasInclusos)
        exibirOuEsconderCampo(tvSintomasExclusaoLabel, tvSintomasExclusao, detalhe.sintomasExclusao)
        exibirOuEsconderCampo(tvOutrosSintomasLabel, tvOutrosSintomas, detalhe.outrosSintomas)
        exibirOuEsconderCampo(tvCid10Label, tvCid10, detalhe.possiveisCid10)
    }

    // Função "inteligente" para preencher a lista de sub-sintomas
    private fun preencherSubSintomas(lista: List<SubSintoma>) {
        if (lista.isEmpty()) {
            // Mostra o texto de "lista vazia" e esconde a lista
            tvSubSintomasLabel.visibility = View.GONE
            rvSubSintomas.visibility = View.GONE
            tvEmptySubSintomas.visibility = View.VISIBLE
        } else {
            // Mostra a lista e esconde o texto de "lista vazia"
            tvSubSintomasLabel.visibility = View.VISIBLE
            rvSubSintomas.visibility = View.VISIBLE
            tvEmptySubSintomas.visibility = View.GONE

            // Configura o adapter
            rvSubSintomas.layoutManager = LinearLayoutManager(this)
            rvSubSintomas.adapter = SubSintomaAdapter(lista)
            // Desativa a rolagem da lista interna, pois a tela inteira já rola
            rvSubSintomas.isNestedScrollingEnabled = false
        }
    }

    // Função auxiliar para não mostrar campos nulos
    private fun exibirOuEsconderCampo(label: TextView, campo: TextView, valor: String?) {
        if (valor.isNullOrBlank()) {
            label.visibility = View.GONE
            campo.visibility = View.GONE
        } else {
            label.visibility = View.VISIBLE
            campo.visibility = View.VISIBLE
            campo.text = valor
        }
    }
}