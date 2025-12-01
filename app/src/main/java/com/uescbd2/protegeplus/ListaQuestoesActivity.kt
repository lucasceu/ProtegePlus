package com.uescbd2.protegeplus

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListaQuestoesActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvQuestoes: RecyclerView
    private lateinit var fabAdd: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Reutilizando layout da lista de pessoas (são idênticos visualmente)
        setContentView(R.layout.activity_lista_pessoas)

        // Ajuste do Título via código
        findViewById<android.widget.TextView>(R.id.tvTitulo).text = "Banco de Questões"
        findViewById<android.widget.TextView>(R.id.tvSubtitulo).text = "Toque para ver alternativas ou segure para excluir."

        dbHelper = DatabaseHelper(this)
        rvQuestoes = findViewById(R.id.rvPessoas) // ID do layout reutilizado
        fabAdd = findViewById(R.id.fabAddPessoa)

        rvQuestoes.layoutManager = LinearLayoutManager(this)

        fabAdd.setOnClickListener {
            val intent = Intent(this, EditarQuestaoActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        carregarLista()
    }

    private fun carregarLista() {
        val lista = dbHelper.getTodasQuestoes()

        // Passando os argumentos POR POSIÇÃO (sem escrever "onClick =")
        rvQuestoes.adapter = QuestaoAdapter(lista,
            { questao ->
                // Primeiro lambda: Clique Curto (Editar)
                val intent = Intent(this, EditarQuestaoActivity::class.java)
                intent.putExtra("ID_QUESTAO", questao.id)
                startActivity(intent)
            },
            { questao ->
                // Segundo lambda: Clique Longo (Deletar)
                confirmarDelecao(questao)
            }
        )
    }

    private fun confirmarDelecao(q: Questao) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Questão")
            .setMessage("Deseja excluir esta questão e todas as suas alternativas?")
            .setPositiveButton("Sim") { _, _ ->
                if (dbHelper.deletarQuestao(q.id)) {
                    Toast.makeText(this, "Excluído!", Toast.LENGTH_SHORT).show()
                    carregarLista()
                } else {
                    Toast.makeText(this, "Erro ao excluir.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Não", null)
            .show()
    }
}