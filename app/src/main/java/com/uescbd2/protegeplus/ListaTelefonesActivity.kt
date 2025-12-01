package com.uescbd2.protegeplus

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListaTelefonesActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvTelefones: RecyclerView
    private lateinit var fabAdd: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Reutilizando layout genérico de lista (se tiver activity_lista_pessoas.xml, pode usar)
        // Se não, crie um activity_lista_telefones.xml igual
        setContentView(R.layout.activity_lista_pessoas)

        // Ajustando textos do layout genérico via código
        findViewById<android.widget.TextView>(R.id.tvTitulo).text = "Telefones de Emergência"
        findViewById<android.widget.TextView>(R.id.tvSubtitulo).text = "Toque para editar ou segure para excluir."

        dbHelper = DatabaseHelper(this)
        rvTelefones = findViewById(R.id.rvPessoas) // ID reutilizado do layout de pessoas
        fabAdd = findViewById(R.id.fabAddPessoa)   // ID reutilizado

        rvTelefones.layoutManager = LinearLayoutManager(this)

        fabAdd.setOnClickListener {
            // Abre cadastro limpo
            startActivity(Intent(this, NovoTelefoneUtilActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        carregarLista()
    }

    private fun carregarLista() {
        val lista = dbHelper.getTelefonesUteis()

        rvTelefones.adapter = TelefoneAdapter(this, lista,
            onClick = { tel ->
                // Clique Curto -> Editar
                val intent = Intent(this, NovoTelefoneUtilActivity::class.java)
                intent.putExtra("ID_TELEFONE", tel.id)
                startActivity(intent)
            },
            onLongClick = { tel ->
                // Clique Longo -> Deletar
                confirmarDelecao(tel)
            }
        )
    }

    private fun confirmarDelecao(tel: TelefoneUtil) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Telefone")
            .setMessage("Deseja apagar ${tel.unidadeSaude}?")
            .setPositiveButton("Sim") { _, _ ->
                if (dbHelper.deletarTelefoneUtil(tel.id)) {
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