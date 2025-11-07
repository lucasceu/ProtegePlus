package com.uescbd2.protegeplus

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_itens)

        // Recebe os dados da Tela 1 (GrupoCiapActivity)
        val grupoId = intent.getIntExtra("GRUPO_ID", -1)
        val grupoNome = intent.getStringExtra("GRUPO_NOME") ?: "Itens"

        dbHelper = DatabaseHelper(this)
        rvListaItens = findViewById(R.id.rvListaItens)
        tvHeader = findViewById(R.id.tvHeader)

        // Atualiza o título da tela
        tvHeader.text = grupoNome

        // --- A LÓGICA "INTELIGENTE" ---
        // Decide de qual tabela buscar os dados, baseado no ID do grupo

        val listaDeItens: List<ItemCiap>

        // ID 1 ("Sintomas") e 7 ("Doenças") vêm da 'tb_ciap'
        if (grupoId == 1 || grupoId == 7) {
            listaDeItens = dbHelper.getItensFromTbCiap(grupoId)
        }
        // ID 2 ("Procedimentos") vêm da 'procedimento_clinico'
        else if (grupoId == 2) {
            listaDeItens = dbHelper.getItensFromProcedimentoClinico(grupoId)
        }
        // Outros casos (não deve acontecer com nosso filtro)
        else {
            listaDeItens = emptyList()
            Toast.makeText(this, "Grupo não reconhecido: $grupoId", Toast.LENGTH_SHORT).show()
        }
        // --- FIM DA LÓGICA ---

        // Configurar o Adapter
        rvListaItens.layoutManager = LinearLayoutManager(this)
        adapter = ItemCiapAdapter(this, listaDeItens) { itemClicado ->

            // --- Ação de clique ---
            // Se for Sintoma (1) ou Doença (7), abre a Tela 3 de Detalhes
            if (itemClicado.idGrupo == 1 || itemClicado.idGrupo == 7) {
                val intent = Intent(this, DetalheItemActivity::class.java)
                intent.putExtra("ITEM_CODIGO", itemClicado.codigo)
                intent.putExtra("ITEM_NOME", itemClicado.nome)
                startActivity(intent)
            }
            // Se for Procedimento (2), não faz nada (ou mostra um Toast)
            else if (itemClicado.idGrupo == 2) {
                Toast.makeText(
                    this,
                    "Nenhum detalhe adicional para procedimentos.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            // TODO: Criar a DetalheItemActivity e passar o CÓDIGO
            // val intent = Intent(this, DetalheItemActivity::class.java)
            // intent.putExtra("ITEM_CODIGO", itemClicado.codigo)
            // intent.putExtra("ITEM_NOME", itemClicado.nome)
            // startActivity(intent)
        }
        rvListaItens.adapter = adapter

        // Configura o logout
        findViewById<LinearLayout>(R.id.llLogout).setOnClickListener {
            Toast.makeText(this, "Logout clicado", Toast.LENGTH_SHORT).show()
        }
    }
}