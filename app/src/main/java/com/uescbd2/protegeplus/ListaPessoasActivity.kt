package com.uescbd2.protegeplus

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListaPessoasActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvPessoas: RecyclerView
    private lateinit var fabAdd: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_pessoas)

        dbHelper = DatabaseHelper(this)
        rvPessoas = findViewById(R.id.rvPessoas)
        fabAdd = findViewById(R.id.fabAddPessoa)

        rvPessoas.layoutManager = LinearLayoutManager(this)

        fabAdd.setOnClickListener {
            // Abre cadastro (reutilizando Register ou criando uma nova Activity limpa se preferir)
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        carregarLista()
    }

    private fun carregarLista() {
        val lista = dbHelper.getTodasPessoas()
        rvPessoas.adapter = PessoaAdapter(lista,
            onClick = { pessoa ->
                // Ao clicar, vai para Edição
                val intent = Intent(this, EditarPessoaActivity::class.java)
                intent.putExtra("ID_PESSOA", pessoa.id)
                startActivity(intent)
            },
            onLongClick = { pessoa ->
                // Clique longo deleta
                confirmarDelecao(pessoa)
            }
        )
    }

    private fun confirmarDelecao(pessoa: Usuario) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Usuário")
            .setMessage("Tem certeza que deseja excluir ${pessoa.nome}?")
            .setPositiveButton("Sim") { _, _ ->
                if (dbHelper.deletarPessoa(pessoa.id)) {
                    Toast.makeText(this, "Excluído com sucesso!", Toast.LENGTH_SHORT).show()
                    carregarLista()
                } else {
                    Toast.makeText(this, "Erro ao excluir.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Não", null)
            .show()
    }
}